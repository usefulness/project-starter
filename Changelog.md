# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
## Changed
- Kotlin Gradle plugin becomes available for library consumers

## Fixed
- Fix detekt task failure when calling `./gradlew clean build` by providing Detekt config lazily
 
## [0.8.0] - 2020-03-14
## Changed
- Update _Android Gradle Plugin_ to 3.6.1, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)
- Update _Kotlin Gradle Plugin_ to 1.3.70, see [release notes](https://github.com/JetBrains/kotlin/releases/tag/v1.3.70)
- Update _kotlinter-gradle_ to version _2.3.2_
- Update _Detekt_ to version _1.6.0_

## [0.7.0] - 2020-02-24
## Added:
- Add automatic versioning of Android apps and libraries

### Changed
- Add coverage exclusions of dagger generated classes for Kotlin modules 
- Change default release tag format to `release-X.X.X` to avoid conflicts with release branch names
- Update _Android Gradle Plugin_ to 3.6.0, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)

## Fixed:
- Restore ability to override Axion Release Plugin config
- Track newly created release branch

## [0.6.0] - 2020-02-01

### Changed
- Update _kotlinter-gradle_ to version _2.3.0_
- Update _Detekt_ to version _1.5.0_

## [0.5.0] - 2020-01-23

### Changed
- Disable _ktlint_ reports by default
- Add less strict coverage exclusions

## Fixed
- Fix setting `/src/*/kotlin/` source sets.

## [0.4.0] - 2020-01-12

### Added
- Add _Checkstyle_ checks for _Java_ source files
- Add _Checkstyle_ baseline generation using `./gradlew generateCheckstyleBaseline` task
- Add support for pre-AndroidX dependencies
- Add Versioning Plugin `com.starter.versioning`

### Changed
- Enable Quality Plugin by default for all Module plugins
- Update `Detekt` version to _1.4.0_

### Fixed
- Fix Robolectric support for `./gradlew projectCoverage` task.

## [0.3.0] - 2020-01-07

### Added
- Add Android Application plugin - `com.starter.application.android`
- Add _Gradle Plugin Portal_ publication

## [0.2.0] - 2020-01-06

### Added
- Add Kotlin Library Plugin - `com.starter.library.kotlin`
- Add Android Library Plugin - `com.starter.library.android`
- Add Common Configuration Plugin - `com.starter.config`
- Add _Github Packages Registry_ publication

## [0.1.0] - 2019-09-27

### Added
- Add Quality Plugin - `com.starter.quality`

[Unreleased]: https://github.com/mateuszkwiecinski/project-starter/compare/release/0.8.0...HEAD
[0.10.0]: https://github.com/mateuszkwiecinski/project-starter/compare/release/0.9.0.../release/0.10.0
[0.9.0]: https://github.com/mateuszkwiecinski/project-starter/compare/release/0.8.0.../release/0.9.0
[0.8.0]: https://github.com/mateuszkwiecinski/project-starter/compare/release/0.7.0.../release/0.8.0
[0.7.0]: https://github.com/mateuszkwiecinski/project-starter/compare/release/0.6.0.../release/0.7.0
[0.6.0]: https://github.com/mateuszkwiecinski/project-starter/compare/release/0.5.0.../release/0.6.0
[0.5.0]: https://github.com/mateuszkwiecinski/project-starter/compare/release/0.4.0.../release/0.5.0
[0.4.0]: https://github.com/mateuszkwiecinski/project-starter/compare/release/0.3.0.../release/0.4.0
[0.3.0]: https://github.com/mateuszkwiecinski/project-starter/compare/release/0.2.0.../release/0.3.0
[0.2.0]: https://github.com/mateuszkwiecinski/project-starter/compare/release/0.1.0.../release/0.2.0
[0.1.0]: https://github.com/mateuszkwiecinski/project-starter/releases/tag/release/0.1.0
