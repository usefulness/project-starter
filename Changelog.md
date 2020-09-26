# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
## Changed
- Codestyle: Update _Detekt_ to version _1.14.0_ see [release notes](https://github.com/detekt/detekt/releases/tag/v1.14.0)
- Update _kotlinter-gradle_ to version _3.1.0_, see [release notes](https://github.com/jeremymailen/kotlinter-gradle/releases/tag/3.1.0)

## [0.19.0] - 2020-09-11
## Changed
- Android: Update `compileSdk` to 30
- Codestyle: Enable ignoreDefaultParameters for `LongParameterList` check
- Update Kotlin plugin to [1.4.10](https://github.com/JetBrains/kotlin/releases/tag/v1.4.10)

## [0.18.0] - 2020-08-27
## Changed
- Update _Detekt_ to version _1.12.0_ see [release notes](https://github.com/detekt/detekt/releases/tag/v1.12.0)
- Update _kotlinter-gradle_ to version _3.0.2_, see [release notes](https://github.com/jeremymailen/kotlinter-gradle/releases/tag/3.0.2)

## [0.17.1] - 2020-08-25
## Fixed
- Use the same Kotlin version across runtime JAR files 

## [0.17.0] - 2020-08-25
## Changed
- Update _Kotlin_ plugin to 1.4.0, see [release notes](https://kotlinlang.org/docs/reference/whatsnew14.html#mixing-named-and-positional-arguments)
- Update _kotlinter-gradle_ to version _3.0.0_, see [release notes](https://github.com/jeremymailen/kotlinter-gradle/releases/tag/3.0.0)

## [0.16.0] - 2020-08-24
## Changed
- Update _Detekt_ to version _1.11.2_ see [release notes](https://github.com/detekt/detekt/releases/tag/v1.11.2)

## Fixed
- IssueLinkTask does not recognise kotlin-only modules


## [0.15.0] - 2020-08-04
## Changed
- Update _Detekt_ to version _1.11.0-RC1_ see [release notes](https://github.com/detekt/detekt/releases/tag/v1.11.0-RC1)
- Update _Android Gradle Plugin_ to 4.0.2, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)

## Fixed
- IssueLinkTask cannot be registered in project with android-module parent

## [0.14.0] - 2020-08-01
## Changed
- Update _kotlinter-gradle_ to version _2.4.1_
- Update _Android Gradle Plugin_ to 4.0.0, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)
- Update _Detekt_ to version _1.9.1_ see [release notes](https://github.com/detekt/detekt/releases/tag/v1.9.1)

## [0.13.0] - 2020-05-19
## Fixed 
- Fix `ForbidJavaFilesTask` configuration failure when registered in non-android module with android module parent

## [0.12.0] - 2020-05-16
## Added
- **[New feature]** `issueLinksReport` that lists status of all issues in different public trackers.  
The new feature is available as automatically registered gradle task, but also can be run as a standalone CLI tool 
- Plugins are now available on `jcenter()` repository 

## Changed
- Update _Android Gradle Plugin_ to 3.6.3, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)
- Update _Detekt_ to version _1.9.0_

## [0.11.0] - 2020-04-05
## Changed
- Update _Android Gradle Plugin_ to 3.6.2, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)

## [0.10.0] - 2020-04-05
## Changed
- Update _Detekt_ to version _1.7.3_

## [0.9.0] - 2020-03-17
## Changed
- Kotlin Gradle plugin becomes available for library consumers
- Update _Kotlin Gradle Plugin_ to 1.3.71, see [release notes](https://github.com/JetBrains/kotlin/releases/tag/v1.3.71)


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

[Unreleased]: https://github.com/usefulness/project-starter/compare/release/0.17.1...HEAD
[0.23.0]: https://github.com/usefulness/project-starter/compare/release/0.22.0.../release/0.23.0
[0.22.0]: https://github.com/usefulness/project-starter/compare/release/0.21.0.../release/0.22.0
[0.21.0]: https://github.com/usefulness/project-starter/compare/release/0.20.0.../release/0.21.0
[0.20.0]: https://github.com/usefulness/project-starter/compare/release/0.19.0.../release/0.20.0
[0.19.0]: https://github.com/usefulness/project-starter/compare/release/0.18.0.../release/0.19.0
[0.18.0]: https://github.com/usefulness/project-starter/compare/release/0.17.1.../release/0.18.0
[0.17.1]: https://github.com/usefulness/project-starter/compare/release/0.17.0.../release/0.17.1
[0.17.0]: https://github.com/usefulness/project-starter/compare/release/0.16.0.../release/0.17.0
[0.16.0]: https://github.com/usefulness/project-starter/compare/release/0.15.0.../release/0.16.0
[0.15.0]: https://github.com/usefulness/project-starter/compare/release/0.14.0.../release/0.15.0
[0.14.0]: https://github.com/usefulness/project-starter/compare/release/0.13.0.../release/0.14.0
[0.13.0]: https://github.com/usefulness/project-starter/compare/release/0.12.0.../release/0.13.0
[0.12.0]: https://github.com/usefulness/project-starter/compare/release/0.11.0.../release/0.12.0
[0.11.0]: https://github.com/usefulness/project-starter/compare/release/0.10.0.../release/0.11.0
[0.10.0]: https://github.com/usefulness/project-starter/compare/release/0.9.0.../release/0.10.0
[0.9.0]: https://github.com/usefulness/project-starter/compare/release/0.8.0.../release/0.9.0
[0.8.0]: https://github.com/usefulness/project-starter/compare/release/0.7.0.../release/0.8.0
[0.7.0]: https://github.com/usefulness/project-starter/compare/release/0.6.0.../release/0.7.0
[0.6.0]: https://github.com/usefulness/project-starter/compare/release/0.5.0.../release/0.6.0
[0.5.0]: https://github.com/usefulness/project-starter/compare/release/0.4.0.../release/0.5.0
[0.4.0]: https://github.com/usefulness/project-starter/compare/release/0.3.0.../release/0.4.0
[0.3.0]: https://github.com/usefulness/project-starter/compare/release/0.2.0.../release/0.3.0
[0.2.0]: https://github.com/usefulness/project-starter/compare/release/0.1.0.../release/0.2.0
[0.1.0]: https://github.com/usefulness/project-starter/releases/tag/release/0.1.0
