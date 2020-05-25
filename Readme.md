## Starter
___

[![codecov](https://codecov.io/gh/mateuszkwiecinski/project-starter/branch/master/graph/badge.svg)](https://codecov.io/gh/mateuszkwiecinski/project-starter)
&nbsp;[![build](https://github.com/mateuszkwiecinski/project-starter/workflows/Build%20project/badge.svg)](https://github.com/mateuszkwiecinski/project-starter/actions)
&nbsp;[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

[![version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/project/starter/plugins/maven-metadata.xml?label=gradle)](https://plugins.gradle.org/search?term=com.starter) 
[![Bintray](https://img.shields.io/bintray/v/mateuszkwiecinski/maven/com.project.starter?label=jcenter)](https://bintray.com/mateuszkwiecinski/maven/com.project.starter)


## Motivation

Maintaining multiple multi-module Android project often requires **copying project configuration across different projects**.
Even when project reaches more advanced stage it is still required to put non minimal effort to maintain its configuration.  
Starting a new project, from the scratch, **takes more than a day** to configure every tool you usually want to use.
Sometimes people create template project or another way of keeping your project configuration in a good shape is using `buildSrc` plugins.
Less code written, ease of sharing between projects but still some part of the code needed to be copied.

This project goes further and addresses that issue by **exposing set of plugins** useful when approaching multi-module setup with _Gradle_ build system.

## Content

Repository consists of several plugins that makes initial project configuration effortless and easily extensible.
Each module consists of configuration code most commonly used in Android project configuration.

### Getting started

#### Add buildscript dependency

Add to your **root** project `build.gradle`:
``` groovy
buildscript {
    repositories {
        gradlePluginPortal()
        google()
    }
    
    dependencies {
        classpath 'com.project.starter:plugins:${version}'
    }
}
```

### Plugins Configuration
#### Kotlin Library Plugin
Plugin configures automated [code style tasks](#quality-plugin), hooks for [common tasks](#day-to-day-use), 
sets coverage reports generation or manages [versioning](#versioning-plugin) of the artifact
    
Apply plugin to project level `build.gradle`

``` groovy
apply plugin: 'com.starter.library.kotlin'

// optional config with default values
projectConfig {
    javaFilesAllowed false
}
```

- `javaFilesAllowed` - defines if the project can contain java files, fails the build otherwise

#### Android Application/Library Plugin
In addition to customizations made to [Kotlin Library Plugin](#kotlin-library-plugin) Android plugins 
tweak default Android Gradle Plugin setup by disabling _BuildConfig_ file generation 
or recognizing `src/main/kotlin` (and similar) path as a valid source set. 

Android Library plugin requires adding to project level `build.gradle`:

``` groovy
apply plugin: 'com.starter.library.android' // or 'com.starter.application.android'

// optional config with default values
projectConfig {
    javaFilesAllowed false
    generateBuildConfig false // for library plugin only 
    defaultVariants ["debug"]
    coverageExclusions [""]
}

// overridden settings for single project
android {
    defaultConfig {
        minSdkVersion 21
    }
}
```

- `javaFilesAllowed` - defines if the project can contain java files, fails the build otherwise.  
(Useful in large projects where you want to enforce new code written in new modules to be written in Java.)
- `generateBuildConfig` - defines if `BuildConfig.java` class should be generated or not.  
General suggestion is to prefer Dependency Injection over Android's flavor setup for libraries
- `defaultVariants` - defines build variants used as a dependency for common `projectXXX` tasks.  
for example setting `fullDebug` as default variant would make `testFullDebugUnitTest.` as a dependency for `projectTest` task.
`["freeRelease", "fullRelease"]` would make add `testFreeReleaseUnitTest` to `projectTest` and `testFreeReleaseLint` to `projectLint`.
- `coverageExclusions` - defines jacoco coverage exclusions for specific module

#### Quality Plugin

Quality plugin is applied automatically by a _Module plugin_, but there is a possibility to use it as a standalone plugin.
Apply plugin to project level `build.gradle`
```
 apply plugin: 'com.starter.quality'
```
which applies and configures code style tasks for the project automatically.  
To execute run: `./gradlew projectCodeStyle`

##### Generating baselines

When integrating code style checks into large projects it is almost forbidden to introduce large sets of changes.  
It is possible to generate baseline for every quality tool available in the project.
- `Android Lint`  
    Type `rm **/lint-*.xml ; ./gradlew projectLint -PrefreshBaseline --continue` into console
- `Detekt`  
    Create baseline using [provided configuration](https://github.com/arturbosch/detekt/blob/master/docs/pages/baseline.md)
- `Checkstyle`  
    Execute `./gradlew generateCheckstyleBaseline` task.
- `ktlint`  
    Unfortunately it is not possible to generate `ktlint` baseline.
    Proper code style may be achieved by using `./gradlew formatKotlin` task.

#### Versioning Plugin

Applied automatically.
Uses tag-based versioning backed by the [allegro/axion-release-plugin](https://github.com/allegro/axion-release-plugin) (view a [full documentation](https://github.com/allegro/axion-release-plugin))

To enable it as a standalone plugin, apply plugin to root project `build.gradle`
```
 apply plugin: 'com.starter.versioning'
```
Can be disabled using [Global Configuration](#global-configuration)

Regular flow relies on calling
- `./gradlew cV` or `./gradlew currentVersion`
- `./gradlew markNextVersion -Prelease.version=1.0.0`
- `./gradlew release` (which pushes proper tags to remote server)

#### Global configuration

Additional default configuration can be applied by adding to **root project** `build.gradle`.
All submodules will use this config as default

``` groovy
apply plugin: 'com.starter.config'
 
commonConfig {
    javaVersion JavaVersion.VERSION_1_8
    javaFilesAllowed true
    androidPlugin {
        compileSdkVersion 29
        minSdkVersion 23
        targetSdkVersion 29
    }
    qualityPlugin {
        enabled true
        formatOnCompile false
    }
    versioningPlugin {
        enabled true
    }
}
```

- `javaVersion` - defines which java version source code is compatible with
- `javaFilesAllowed` - defines if the project can contain java files, fails the build otherwise.
- `androidPlugin`:
    - contains values passed to _Android Gradle Plugin_
- `qualityPlugin`:
    - `enabled` - enables/disables [Quality Plugin](#quality-plugin)
    - `formatOnCompile` - defines if ktlint should format source code on every compilation
- `versioningPlugin`:
    - `enabled` - enables/disables [Versioning Plugin](#versioning-plugin)

### Day-to-day use
After applying library/application plugin there are appropriate tasks added:
- `./gradlew projectTest`  
Runs tests for all modules using either predefined tasks (i.e. `test` for kotlin modules or `testDebugUnitTest` for android libraries) or use customized values.
- `./gradlew projectLint`  
Runs Android lint checks against all modules (if custom lint checks are applied then for Kotlin modules too)
- `./gradlew projectCodeStyle`  
Verifies if code style matches modern standards using tools such as [`ktlint`](https://github.com/pinterest/ktlint), [`Detekt`](https://github.com/arturbosch/detekt) or [`Checkstyle`](https://checkstyle.org/) with predefined config.
- `./gradlew projectCoverage`  
Automatically generates test coverage reports for all modules using [`Jacoco`](https://github.com/jacoco/jacoco)

Those tasks allows you to run tests efficiently for all modules by typing just a single task.
That solves an issue when for example `test` task unnecessarily executes tests for all build variants and more strict `testDebug` skips executing tests in kotlin only modules.

## Sample project
Sample [Github Browser](https://github.com/mateuszkwiecinski/github_browser) project - a customized, `buildSrc` based plugin application.

## License
The library is available under [MIT License](/LICENSE) and highly benefits from binary dependencies:
- `Kotlinter Gradle` - [License](https://github.com/jeremymailen/kotlinter-gradle/blob/master/LICENSE)
- `axion-relese-plugin` - [License](https://github.com/allegro/axion-release-plugin/blob/master/LICENSE)
- `Kotlin Gradle Plugin` - [License](https://github.com/JetBrains/kotlin#license)
- `Android Gradle Plugin` - [License](https://developer.android.com/license)
- `Detekt` - [License](https://github.com/arturbosch/detekt/blob/master/LICENSE)
