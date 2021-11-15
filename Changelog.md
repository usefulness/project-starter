# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
## [0.39.0] - 2021-11-15
## Changed
- Update Kotlin plugin to _1.6.0_, see [release notes](https://github.com/JetBrains/kotlin/releases/tag/v1.6.0)

## [0.38.0] - 2021-10-04
## Changed
- Update Kotlin plugin to _1.5.31_, see [release notes](https://github.com/JetBrains/kotlin/releases/tag/v1.5.31)
- Update `compileSdkVersion` to 31

## [0.37.0] - 2021-09-12
## Fixed
- Do not control JavaCompile tasks output in Android projects

## [0.36.0] - 2021-09-07
## Changed
- Update Kotlin plugin to _1.5.30_, see [release notes](https://github.com/JetBrains/kotlin/releases/tag/v1.5.30)
- Set default Java compatibility version to Java 11
- Set minimum supported Gradle version to 7.0
## Fixed
- Add support for `FAIL_ON_PROJECT_REPOS` option

## [0.35.0] - 2021-09-01
## Changed
- Update _Android Gradle Plugin_ to _7.0.2_, see [release notes](https://developer.android.com/studio/releases/gr

## [0.34.0] - 2021-08-24
## Changed
- Update Kotlin plugin to _1.5.30_, see [release notes](https://github.com/JetBrains/kotlin/releases/tag/v1.5.30)adle-plugin)

## [0.33.0] - 2021-08-21
## Changed
- Update _Android Gradle Plugin_ to _7.0.1_, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)

## Fixed
- Support back Java 8

## [0.32.0] - 2021-08-12
## Changed
- Update _kotlinter-gradle_ to version _3.5.0_, see [release notes](https://github.com/jeremymailen/kotlinter-gradle/releases/tag/3.5.0)
- Update _Detekt_ to version _1.18.0_ see [release notes](https://github.com/detekt/detekt/releases/tag/v1.18.0)

## [0.31.0] - 2021-07-28
## Changed
- Update _Android Gradle Plugin_ to _7.0.0_, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)

## [0.30.0] - 2021-07-28
## Changed
- Update Kotlin plugin to _1.5.21_, see [release notes](https://github.com/JetBrains/kotlin/releases/tag/v1.5.21)

## [0.29.0] - 2021-07-10
## Changed
- Update Kotlin plugin to _1.5.20_, see [release notes](https://github.com/JetBrains/kotlin/releases/tag/v1.5.20)
- Update _Android Gradle Plugin_ to _4.2.2_, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)
- Update _kotlinter-gradle_ to version _3.4.5_, see [release notes](https://github.com/jeremymailen/kotlinter-gradle/releases/tag/3.4.5)

## [0.28.2] - 2021-06-07
## Fixed
-  Plugin will now depend on Kotlin Gradle plugin 1.5.10

## [0.28.1] - 2021-06-06
## Changed
- Update _Detekt_ config. Disable `LongParametersList`, `NestedBlockDepth` and `TooManyFunctions`

## [0.28.0] - 2021-06-05
## Changed
- Update Kotlin plugin to [1.5.10](https://github.com/JetBrains/kotlin/releases/tag/v1.5.10)
- Update _Detekt_ to version _1.17.1_ see [release notes](https://github.com/detekt/detekt/releases/tag/v1.17.1)

## [0.27.2] - 2021-05-05
## Fixed
- Support back Java 8

## [0.27.1] - 2021-05-05
## Changed
- Disabled `experimental:argument-list-wrapping` rule

## [0.27.0] - 2021-05-05
## Changed
- Update _Android Gradle Plugin_ to 4.2.0, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)
- Bump minimum supported Gradle version to 6.8.3
- Update Jacoco version to `0.8.7`
- Update _kotlinter-gradle_ to version _3.4.3_, see [release notes](https://github.com/jeremymailen/kotlinter-gradle/releases/tag/3.4.3)

## [0.26.3] - 2021-05-01
## Changed
- Restore backwards Gradle compatibility down to 6.5.1

## [0.26.2] - 2021-05-01
## Fixed
- Again, fix setting up google repository

## [0.26.1] - 2021-05-01
## Fixed
- Fix missing dependencies avaibable on `google()` repository

## [0.26.0] - 2021-05-01
## Fixed
- Fix lazy Test task configuration

## Changed
- Require using Kotlin 1.4 for all library consumers. Bump min supported Gradle version to 6.8.3
- Update Kotlin plugin to [1.5.0](https://github.com/JetBrains/kotlin/releases/tag/v1.5.0)
- Update Jacoco version to `0.8.7-SNAPSHOT` to [support Kotlin 1.5](https://github.com/detekt/detekt/pull/3718/files) 

## [0.25.0] - 2021-04-15
## Added
- New `com.starter.library.multiplatform` plugin for KMM projects

## [0.24.0] - 2021-04-11
## Changed
- The `issuechecker` dependency has been moved from `jcenter()` to `mavenCentral()`
- `kapt` is not enabled automatically by default
- Update Kotlin plugin to [1.4.32](https://github.com/JetBrains/kotlin/releases/tag/v1.4.32)
- Divide plugins into multiple functional groups to avoid adding unnecessary dependencies
- Update _Detekt_ to version _1.16.0_ see [release notes](https://github.com/detekt/detekt/releases/tag/v1.16.0)
- Update _kotlinter-gradle_ to version _3.4.0_, see [release notes](https://github.com/jeremymailen/kotlinter-gradle/releases/tag/3.4.0)
- **Breaking:** Quality plugin can't be disabled anymore. The dependency has been added either way, so the plugin will now always create quality realated tasks.
- **Breaking:** Changed default release tag format for new projects. This change follows `axion-release-plugin` convention.
- **Breaking:** The plugin has been now divided into multiple smaller artifacts. The change is breaking only if the legacy plugin application is used.   
`com.project.starter:plugins` can now be replaced with `com.project.starter:jvm` or `com.project.starter:android` or just `com.project.starter:quality`.  
  Applying just `jvm` project doesn't require adding `google()` repository dependency anymore.
- The plugin is now compatible with Gradle 7.0  

#### Known bugs
- It is not possible to override `com.starter.versionin` plugin config. The workaround is to set the project version manually, after updating its configuration. 

## [0.23.0] - 2021-02-06
## Changed
- Update Jacoco to version `0.8.6`
- Update Kotlin plugin to [1.4.30](https://github.com/JetBrains/kotlin/releases/tag/v1.4.30)

## [0.22.0] - 2021-01-15
## Changed
- Codestyle: Update _Detekt_ to version _1.15.0_ see [release notes](https://github.com/detekt/detekt/releases/tag/v1.15.0)
- Update _kotlinter-gradle_ to version _3.3.0_, see [release notes](https://github.com/jeremymailen/kotlinter-gradle/releases/tag/3.3.0)
- Update Kotlin plugin to [1.4.21](https://github.com/JetBrains/kotlin/releases/tag/v1.4.21)
- Update _Android Gradle Plugin_ to 4.1.1, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)

## Fixed
- Fix issueChecker:cli publishing on Github

## [0.21.1] - 2020-10-13
## Fixed
- Allow using plugin with different AGP versions by not using internal AGP apis

## [0.21.0] - 2020-10-13
## Changed
- Update _Android Gradle Plugin_ to 4.1.0, see [release notes](https://developer.android.com/studio/releases/gradle-plugin)

## [0.20.0] - 2020-09-27
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

[Unreleased]: https://github.com/usefulness/project-starter/compare/release/0.31.0...HEAD
[0.40.0]: https://github.com/usefulness/project-starter/compare/release/0.39.0.../release/0.40.0
[0.39.0]: https://github.com/usefulness/project-starter/compare/release/0.38.0.../release/0.39.0
[0.38.0]: https://github.com/usefulness/project-starter/compare/release/0.37.0.../release/0.38.0
[0.37.0]: https://github.com/usefulness/project-starter/compare/release/0.37.0.../release/0.37.0
[0.37.0]: https://github.com/usefulness/project-starter/compare/release/0.36.0.../release/0.37.0
[0.36.0]: https://github.com/usefulness/project-starter/compare/release/0.35.0.../release/0.36.0
[0.35.0]: https://github.com/usefulness/project-starter/compare/release/0.34.0.../release/0.35.0
[0.34.0]: https://github.com/usefulness/project-starter/compare/release/0.33.0.../release/0.34.0
[0.33.0]: https://github.com/usefulness/project-starter/compare/release/0.32.0.../release/0.33.0
[0.32.0]: https://github.com/usefulness/project-starter/compare/release/0.31.0.../release/0.32.0
[0.31.0]: https://github.com/usefulness/project-starter/compare/release/0.30.0.../release/0.31.0
[0.30.0]: https://github.com/usefulness/project-starter/compare/release/0.29.0.../release/0.30.0
[0.29.0]: https://github.com/usefulness/project-starter/compare/release/0.28.2.../release/0.29.0
[0.28.2]: https://github.com/usefulness/project-starter/compare/release/0.28.1.../release/0.28.2
[0.28.1]: https://github.com/usefulness/project-starter/compare/release/0.28.0.../release/0.28.1
[0.28.0]: https://github.com/usefulness/project-starter/compare/release/0.27.0.../release/0.28.0
[0.27.2]: https://github.com/usefulness/project-starter/compare/release/0.27.1.../release/0.27.2
[0.27.1]: https://github.com/usefulness/project-starter/compare/release/0.27.0.../release/0.27.1
[0.27.0]: https://github.com/usefulness/project-starter/compare/release/0.26.3.../release/0.27.0
[0.26.3]: https://github.com/usefulness/project-starter/compare/release/0.26.2.../release/0.26.3
[0.26.2]: https://github.com/usefulness/project-starter/compare/release/0.26.1.../release/0.26.2
[0.26.1]: https://github.com/usefulness/project-starter/compare/release/0.26.0.../release/0.26.1
[0.26.0]: https://github.com/usefulness/project-starter/compare/release/0.25.0.../release/0.26.0
[0.25.0]: https://github.com/usefulness/project-starter/compare/release/0.24.0.../release/0.25.0
[0.24.0]: https://github.com/usefulness/project-starter/compare/release/0.23.0.../release/0.24.0
[0.23.0]: https://github.com/usefulness/project-starter/compare/release/0.22.0.../release/0.23.0
[0.22.0]: https://github.com/usefulness/project-starter/compare/release/0.21.1.../release/0.22.0
[0.21.1]: https://github.com/usefulness/project-starter/compare/release/0.21.0.../release/0.21.1
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
