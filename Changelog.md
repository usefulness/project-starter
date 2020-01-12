# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
- Add _Checkstyle_ checks for _Java_ source files
- Enable Quality Plugin by default for all Module plugins
- Add _Checkstyle_ baseline generation using `./gradlew generateCheckstyleBaseline` task
- Add support for pre-AndroidX dependencies
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
