Upgrade this project's dependencies.

It's important that you respect the following requirements to the letter!

## How-to find latest versions

To update the project's dependencies, first execute this command:

```sh
./.github/scripts/dependency-updates.py
```

This will output to STDOUT all the dependency upgrades that are available.

These are the files where dependencies are declared (and that will need to be updated):
- `build.sbt`
- `project/plugins.sbt`
- `project/build.properties`

## Hard Requirements

* Only consider stable, minor versions (semver), concern being API compatibility
  * Testing dependencies, build tools or plugins can be upgraded to any stable version
* The Scala project is cross-compield for 3 Scala major versions: 2.12.x, 2.13.x and Scala 3.x.x
  * The tool above reports the minor upgrades available for each particular major versions
* Fix code breakage, but carefully, we MUST NOT break public API compatibility
  * It's fine to undo a certain update if you can't fix the setup or the code
* Work is not over until `sbt ci-all` passes! (acceptance criteria)

## Communication requirements

In case there are updates, create a PR such that:
- Title of PR MUST BE "Upgrade dependencies: <list>"
- Description of PR MUST CONTAIN:
  - the list of dependencies that were upgraded
  - the list of dependencies that couldn't be upgraded
    - runtime/library major versions skipped for API compatibility
  - other details you deem relevant (like code fixes)

In case there are no updates, such that no PR can be created, then a comment with the status on this issue (this is the way you communicate with the project's maintainers): https://github.com/monix/minitest/issues/191
