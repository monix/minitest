## Version 2.3.2 (Jan 18, 2019)

- Changed default value for `useSbtLogging` to `false`
- Fix output of test names

## Version 2.3.0 (Jan 17, 2019)

- Updated Scala to 2.12.8, SBT to 1.2.8, ScalaJS to 0.6.26, Scala Native to 0.3.8
- Change logging to use `System.out.println` directly
- Refactored the Scala Native integration

## Version 2.1.1 (Feb 17, 2018)

- Enable Scala 2.13.0-M3 builds
- Disable automatic publishing

## Version 2.1.0 (Feb 9, 2018)

- Add `setupSuite` and `tearDownSuite` for per suite setup and teardown logic
- Update Scala and Scala.js to latest stable versions

## Version 2.0.0 (Nov 10, 2017)

- Simplify API, make it safer with the `Void` for synchronous tests
  and with `testAsync` for `Future`-enabled tests
- Enable automatic builds
- Upgrade Scala and Scala.js versions

## Version 1.1.0 (Apr 30, 2016)

- Update to Scala 2.11.11 and 2.12.2
- Update ScalaCheck to 1.13.5
- Update Scala.js to 0.6.16
- Make all `Asserts` depend on implicit `SourceLocation`

## Version 1.0.1 (Mar 27, 2016)

- Fix dependencies, adding scala-reflect

## Version 1.0.0 (Mar 27, 2016)

- Update Scala to 2.12.1
- Update Scala.js to 0.6.15
- Update ScalaCheck to 1.13.5

## Version 0.27 (Nov 2, 2016)

- Update Scala to 2.12.0 (final)
- Update ScalaCheck to 1.13.4

## Version 0.26 (Oct 23, 2016)

- Update Scala to 2.12.0-RC2

## Version 0.25 (Oct 8, 2016)

- Fix failure messages (reversing "received" with "expected" :)

## Version 0.24 (Sep 9, 2016)

- Update Scala to 2.12.0-RC1
- Update Scala.js to 0.6.12

## Version 0.23

- Update Scala.js version to 0.6.11
- Update ScalaCheck version to 1.13.2
- Drop dependency on Discipline
- Add support for Scala 2.12-M5

## Version 0.22

- Add support for Scala 2.12-M4
- Upgrade Scala.js to 0.6.9

