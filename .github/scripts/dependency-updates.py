#!/usr/bin/env python3
"""
Dependency Updates Report Generator for Scala/sbt Projects.

This script:
1. Extracts supported Scala versions from the project's sbt build
2. Runs sbt-updates' dependencyUpdatesReport for each Scala version
3. Runs dependencyUpdatesReport in the plugins context
4. Checks for sbt updates via Maven Central
5. Produces a consolidated, deduplicated report

Usage:
    python3 .github/scripts/dependency-updates.py
"""

import json
import os
import re
import subprocess
import sys
import urllib.request
import xml.etree.ElementTree as ET
from collections import defaultdict
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List, Optional, Set, Tuple


@dataclass
class DependencyUpdate:
    """Represents a single dependency update."""
    group_id: str
    artifact_id: str
    config: Optional[str]
    current_version: str
    updates: List[str]
    source: str  # e.g., "scala-2.12", "plugins", "sbt"
    project_path: Optional[str] = None

    @property
    def full_name(self) -> str:
        if self.config:
            return f"{self.group_id}:{self.artifact_id}:{self.config}"
        return f"{self.group_id}:{self.artifact_id}"

    @property
    def latest_update(self) -> Optional[str]:
        return self.updates[-1] if self.updates else None

    def first_update_matching(self, prefix: str) -> Optional[str]:
        """Return the first update that starts with the given prefix."""
        for u in self.updates:
            if u.startswith(prefix):
                return u
        return None


def run_sbt(args: str, cwd: Path = Path.cwd()) -> str:
    """Run an sbt command and return its stdout."""
    cmd = ["sbt", "-batch", args]
    print(f"[cmd] sbt -batch {args}", file=sys.stderr)
    result = subprocess.run(
        cmd,
        cwd=cwd,
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        print(f"sbt command failed: {args}", file=sys.stderr)
        print(result.stderr, file=sys.stderr)
        # Don't raise - some commands might still have produced output
    return result.stdout + result.stderr


def extract_cross_scala_versions(project_dir: Path) -> List[str]:
    """Extract crossScalaVersions from the sbt build."""
    output = run_sbt("show crossScalaVersions", cwd=project_dir)
    lines = output.splitlines()
    versions = []

    # Find the unscoped `crossScalaVersions` line (not "project / crossScalaVersions")
    # and grab the List(...) from the next line.
    for i, line in enumerate(lines):
        stripped = line.strip()
        # sbt prefixes lines with [info], so handle "[info] crossScalaVersions"
        if stripped == "crossScalaVersions" or stripped.endswith(" crossScalaVersions"):
            # Make sure it's not a scoped line like "minitestJVM / crossScalaVersions"
            if "/" not in stripped:
                if i + 1 < len(lines):
                    next_line = lines[i + 1]
                    # next_line looks like: [info] \tList(2.12.14, 2.13.7, 3.0.2)
                    list_str = next_line.strip()
                    # Remove [info] prefix if present
                    if list_str.startswith("[info]"):
                        list_str = list_str[len("[info]"):].strip()
                    if list_str.startswith("List(") and list_str.endswith(")"):
                        inner = list_str[5:-1]  # strip "List(" and ")"
                        versions = [
                            v.strip().strip('"')
                            for v in inner.split(",")
                            if v.strip()
                        ]
                break

    # If we couldn't extract any, fall back to parsing build.sbt
    if not versions:
        build_sbt = project_dir / "build.sbt"
        if build_sbt.exists():
            text = build_sbt.read_text()
            match = re.search(r"crossScalaVersions\s*:=\s*Seq\(([^)]+)\)", text)
            if match:
                versions = []
                for v in match.group(1).split(","):
                    v = v.strip().strip('"')
                    # Skip variable names like Scala212, only keep version strings
                    if v and re.match(r"\d+\.\d+", v):
                        versions.append(v)
    return versions


def parse_dependency_updates_txt(content: str, source_label: str) -> List[DependencyUpdate]:
    """Parse the content of a dependency-updates.txt file."""
    updates = []
    lines = content.splitlines()
    current_project = None

    for line in lines:
        line = line.rstrip()
        # Track project name from header lines
        project_match = re.match(r"Found \d+ dependency updates for (.+)", line)
        if project_match:
            current_project = project_match.group(1).strip()
            continue
        no_updates_match = re.match(r"No dependency updates found for (.+)", line)
        if no_updates_match:
            current_project = no_updates_match.group(1).strip()
            continue

        # Parse update lines like:
        #   org.scala-lang:scala-library              : 2.12.14 -> 2.12.21 -> 2.13.18 -> 3.8.3
        #   org.scala-lang:scala-reflect:compile      : 2.12.14 -> 2.12.21 -> 2.13.18
        update_match = re.match(
            r"^\s+([^:]+):([^\s:]+)(?::(\S+))?\s+:\s+(.+)$",
            line
        )
        if update_match:
            group_id = update_match.group(1).strip()
            artifact_id = update_match.group(2).strip()
            config = update_match.group(3)
            versions_str = update_match.group(4).strip()

            # Split versions by "->"
            all_versions = [v.strip() for v in versions_str.split("->")]
            current_version = all_versions[0]
            available = all_versions[1:]

            updates.append(DependencyUpdate(
                group_id=group_id,
                artifact_id=artifact_id,
                config=config,
                current_version=current_version,
                updates=available,
                source=source_label,
                project_path=current_project,
            ))

    return updates


def find_and_parse_reports(project_dir: Path, source_label: str) -> List[DependencyUpdate]:
    """Find all dependency-updates.txt files and parse them."""
    all_updates = []
    for txt_file in project_dir.rglob("dependency-updates.txt"):
        # Skip the meta-build's own report if we're looking for main reports
        if source_label != "plugins" and "project/target" in str(txt_file):
            continue
        content = txt_file.read_text()
        updates = parse_dependency_updates_txt(content, source_label)
        for u in updates:
            u.project_path = str(txt_file.relative_to(project_dir))
        all_updates.extend(updates)
    return all_updates


def get_major_scala_prefix(version: str) -> str:
    """Return the major version prefix, e.g., '2.12.' or '3.'"""
    parts = version.split(".")
    if len(parts) >= 2:
        if parts[0] == "2":
            return f"{parts[0]}.{parts[1]}."
        else:
            return f"{parts[0]}."
    return f"{version}."


def get_latest_scala3_version() -> Optional[str]:
    """Query Maven Central for the latest stable scala3-library_3 version."""
    url = "https://repo1.maven.org/maven2/org/scala-lang/scala3-library_3/maven-metadata.xml"
    try:
        with urllib.request.urlopen(url, timeout=30) as response:
            tree = ET.parse(response)
            root = tree.getroot()
            versions = []
            for version_elem in root.findall(".//version"):
                v = version_elem.text
                if v and not re.search(r"(?i)(rc|m|beta|alpha|snapshot|nightly|bin-)", v):
                    versions.append(v)
            if versions:
                # Sort by version - simple semver sort
                def version_key(v: str) -> Tuple:
                    parts = re.split(r"[.-]", v)
                    result = []
                    for p in parts:
                        try:
                            result.append((0, int(p)))
                        except ValueError:
                            result.append((1, p))
                    return tuple(result)
                versions.sort(key=version_key)
                return versions[-1]
    except Exception as e:
        print(f"Warning: could not fetch latest Scala 3 version: {e}", file=sys.stderr)
    return None


def get_latest_sbt_version() -> Optional[str]:
    """Query Maven Central for the latest stable sbt version."""
    url = "https://repo1.maven.org/maven2/org/scala-sbt/sbt/maven-metadata.xml"
    try:
        with urllib.request.urlopen(url, timeout=30) as response:
            tree = ET.parse(response)
            root = tree.getroot()
            versions = []
            for version_elem in root.findall(".//version"):
                v = version_elem.text
                if v and not re.search(r"(?i)(rc|m|beta|alpha|snapshot|nightly)", v):
                    versions.append(v)
            if versions:
                def version_key(v: str) -> Tuple:
                    parts = re.split(r"[.-]", v)
                    result = []
                    for p in parts:
                        try:
                            result.append((0, int(p)))
                        except ValueError:
                            result.append((1, p))
                    return tuple(result)
                versions.sort(key=version_key)
                return versions[-1]
    except Exception as e:
        print(f"Warning: could not fetch latest sbt version: {e}", file=sys.stderr)
    return None


def read_current_sbt_version(project_dir: Path) -> Optional[str]:
    """Read the current sbt version from project/build.properties."""
    props_file = project_dir / "project" / "build.properties"
    if props_file.exists():
        for line in props_file.read_text().splitlines():
            match = re.match(r"sbt\.version\s*=\s*(.+)", line)
            if match:
                return match.group(1).strip()
    return None


def deduplicate_updates(updates: List[DependencyUpdate]) -> Dict[str, List[DependencyUpdate]]:
    """
    Group updates by dependency name, keeping track of which sources
    they appeared in.
    """
    grouped = defaultdict(list)
    for u in updates:
        key = u.full_name
        grouped[key].append(u)
    return dict(grouped)


def generate_report(
    project_dir: Path,
    scala_versions: List[str],
    scala_updates: Dict[str, Optional[str]],
    all_updates: List[DependencyUpdate],
    plugin_updates: List[DependencyUpdate],
    current_sbt: Optional[str],
    latest_sbt: Optional[str],
) -> str:
    """Generate the final Markdown report."""
    lines = []
    lines.append("# Dependency Updates Report")
    lines.append("")
    lines.append(f"**Project:** `{project_dir.name}`")
    lines.append("")

    # Scala versions section
    lines.append("## Scala Version Updates")
    lines.append("")
    for sv in scala_versions:
        prefix = get_major_scala_prefix(sv)
        update = scala_updates.get(sv)
        if update:
            lines.append(f"- **{sv}** -> **{update}**")
        else:
            lines.append(f"- **{sv}** -> (no updates found)")
    lines.append("")

    # sbt version section
    lines.append("## sbt Version")
    lines.append("")
    if current_sbt:
        if latest_sbt and latest_sbt != current_sbt:
            lines.append(f"- **{current_sbt}** -> **{latest_sbt}**")
        else:
            lines.append(f"- **{current_sbt}** (up to date)")
    else:
        lines.append("- Could not determine current sbt version")
    lines.append("")

    # Library dependencies grouped by Scala version
    lines.append("## Library Dependencies")
    lines.append("")

    # Group main updates by Scala version source
    updates_by_source = defaultdict(list)
    for u in all_updates:
        # Skip scala-library entries - handled separately above
        if u.group_id == "org.scala-lang" and u.artifact_id == "scala-library":
            continue
        updates_by_source[u.source].append(u)

    for sv in scala_versions:
        source_key = f"scala-{sv}"
        updates = updates_by_source.get(source_key, [])
        if not updates:
            continue

        lines.append(f"### Updates detected with Scala {sv}")
        lines.append("")
        lines.append("| Dependency | Current | Updates | Source |")
        lines.append("|------------|---------|---------|--------|")
        deduped = deduplicate_updates(updates)
        for dep_name, dep_updates in sorted(deduped.items()):
            # Use the first update's info; if multiple projects report the same,
            # take the union of updates
            all_avail = []
            for du in dep_updates:
                all_avail.extend(du.updates)
            unique_avail = sorted(set(all_avail), key=lambda v: all_avail.index(v))
            current = dep_updates[0].current_version
            source_paths = ", ".join(sorted(set(
                u.project_path or "unknown" for u in dep_updates
            )))
            lines.append(
                f"| {dep_name} | {current} | {' -> '.join(unique_avail)} | {source_paths} |"
            )
        lines.append("")

    # Cross-version updates (same dependency, same updates across all Scala versions)
    lines.append("### Cross-Version Updates (consistent across Scala versions)")
    lines.append("")
    cross_version = []
    all_deps = set()
    for updates in updates_by_source.values():
        for u in updates:
            all_deps.add(u.full_name)

    for dep_name in sorted(all_deps):
        sources = {source: [] for source in updates_by_source}
        found_in_all = True
        for source, updates in updates_by_source.items():
            matches = [u for u in updates if u.full_name == dep_name]
            if not matches:
                found_in_all = False
                break
            sources[source] = matches

        if found_in_all:
            # Check if updates are the same across all versions
            all_updates_for_dep = []
            for src_ups in sources.values():
                all_updates_for_dep.extend(src_ups)

            first = all_updates_for_dep[0]
            all_same = all(
                u.current_version == first.current_version and u.updates == first.updates
                for u in all_updates_for_dep
            )
            if all_same:
                cross_version.append((dep_name, first))

    if cross_version:
        lines.append("| Dependency | Current | Updates |")
        lines.append("|------------|---------|---------|")
        for dep_name, update in cross_version:
            lines.append(f"| {dep_name} | {update.current_version} | {' -> '.join(update.updates)} |")
    else:
        lines.append("_No dependencies with identical updates across all Scala versions._")
    lines.append("")

    # Plugin updates
    lines.append("## Plugin Dependencies")
    lines.append("")
    if plugin_updates:
        lines.append("| Dependency | Current | Updates |")
        lines.append("|------------|---------|---------|")
        deduped = deduplicate_updates(plugin_updates)
        for dep_name, dep_updates in sorted(deduped.items()):
            all_avail = []
            for du in dep_updates:
                all_avail.extend(du.updates)
            unique_avail = sorted(set(all_avail), key=lambda v: all_avail.index(v))
            current = dep_updates[0].current_version
            lines.append(
                f"| {dep_name} | {current} | {' -> '.join(unique_avail)} |"
            )
    else:
        lines.append("_No plugin updates found._")
    lines.append("")

    # Raw data as JSON for machine parsing
    lines.append("## Raw Data (JSON)")
    lines.append("")
    lines.append("```json")
    report_data = {
        "scala_versions": {
            sv: {"current": sv, "update": scala_updates.get(sv)}
            for sv in scala_versions
        },
        "sbt": {
            "current": current_sbt,
            "latest": latest_sbt,
        },
        "libraries": {},
        "plugins": {},
    }

    # Add libraries
    for source, updates in updates_by_source.items():
        for u in updates:
            key = u.full_name
            if key not in report_data["libraries"]:
                report_data["libraries"][key] = {
                    "group_id": u.group_id,
                    "artifact_id": u.artifact_id,
                    "config": u.config,
                    "current": u.current_version,
                    "updates": u.updates,
                    "sources": [],
                }
            report_data["libraries"][key]["sources"].append({
                "scala_version": source.replace("scala-", ""),
                "project": u.project_path,
            })

    # Add plugins
    for u in plugin_updates:
        key = u.full_name
        if key not in report_data["plugins"]:
            report_data["plugins"][key] = {
                "group_id": u.group_id,
                "artifact_id": u.artifact_id,
                "config": u.config,
                "current": u.current_version,
                "updates": u.updates,
                "sources": [],
            }
        report_data["plugins"][key]["sources"].append({
            "project": u.project_path,
        })

    lines.append(json.dumps(report_data, indent=2))
    lines.append("```")
    lines.append("")

    return "\n".join(lines)


def main() -> int:
    project_dir = Path.cwd()

    # Ensure sbt-updates plugin is installed globally
    sbt_plugins_dir = Path.home() / ".sbt" / "1.0" / "plugins"
    sbt_plugins_dir.mkdir(parents=True, exist_ok=True)
    updates_plugin_file = sbt_plugins_dir / "sbt-updates.sbt"
    plugin_line = 'addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")\n'
    if not updates_plugin_file.exists():
        updates_plugin_file.write_text(plugin_line)
    else:
        content = updates_plugin_file.read_text()
        if 'sbt-updates' not in content:
            with open(updates_plugin_file, "a") as f:
                f.write("\n" + plugin_line)

    # Step 1: Extract Scala versions
    print("=== Extracting Scala versions ===", file=sys.stderr)
    scala_versions = extract_cross_scala_versions(project_dir)
    if not scala_versions:
        print("ERROR: Could not extract crossScalaVersions", file=sys.stderr)
        return 1
    print(f"Found Scala versions: {scala_versions}", file=sys.stderr)

    # Step 2: Run dependencyUpdatesReport for each Scala version
    all_updates: List[DependencyUpdate] = []
    scala_updates: Dict[str, Optional[str]] = {}

    for sv in scala_versions:
        print(f"=== Running dependencyUpdatesReport for Scala {sv} ===", file=sys.stderr)
        run_sbt(f"++{sv}; dependencyUpdatesReport", cwd=project_dir)

        # Parse reports
        updates = find_and_parse_reports(project_dir, f"scala-{sv}")
        all_updates.extend(updates)

        # Extract scala-library update for this specific major version
        scala_lib_updates = [
            u for u in updates
            if u.group_id == "org.scala-lang" and u.artifact_id == "scala-library"
        ]
        if scala_lib_updates:
            # Use the first one found (they should all be the same)
            u = scala_lib_updates[0]
            prefix = get_major_scala_prefix(sv)
            matched = u.first_update_matching(prefix)
            scala_updates[sv] = matched
            print(f"  Scala {sv}: found update -> {matched}", file=sys.stderr)
        else:
            print(f"  Scala {sv}: no scala-library update found in reports", file=sys.stderr)

    # If Scala 3 is supported but we didn't find a scala-library update,
    # query Maven Central for the latest scala3-library version
    scala3_versions = [sv for sv in scala_versions if sv.startswith("3.")]
    for sv in scala3_versions:
        if sv not in scala_updates or scala_updates[sv] is None:
            print(f"=== Querying Maven Central for latest Scala 3 version ===", file=sys.stderr)
            latest = get_latest_scala3_version()
            if latest and latest != sv:
                scala_updates[sv] = latest
                print(f"  Scala {sv}: latest from Maven Central -> {latest}", file=sys.stderr)

    # Step 3: Run plugin dependency updates
    print("=== Running plugin dependencyUpdatesReport ===", file=sys.stderr)
    run_sbt("reload plugins; dependencyUpdatesReport", cwd=project_dir)
    plugin_updates = find_and_parse_reports(project_dir, "plugins")
    # Filter to only the project/target report for plugins
    plugin_updates = [
        u for u in plugin_updates
        if u.project_path and "project/target" in u.project_path
    ]

    # Step 4: Check sbt version
    print("=== Checking sbt version ===", file=sys.stderr)
    current_sbt = read_current_sbt_version(project_dir)
    latest_sbt = get_latest_sbt_version()
    print(f"  Current sbt: {current_sbt}, Latest sbt: {latest_sbt}", file=sys.stderr)

    # Step 5: Generate report
    report = generate_report(
        project_dir=project_dir,
        scala_versions=scala_versions,
        scala_updates=scala_updates,
        all_updates=all_updates,
        plugin_updates=plugin_updates,
        current_sbt=current_sbt,
        latest_sbt=latest_sbt,
    )

    print(report)
    return 0


if __name__ == "__main__":
    sys.exit(main())
