## Starter
___

[![codecov](https://codecov.io/gh/mateuszkwiecinski/project-starter/branch/master/graph/badge.svg)](https://codecov.io/gh/mateuszkwiecinski/project-starter)
&nbsp;[![build](https://github.com/mateuszkwiecinski/project-starter/workflows/Build%20project/badge.svg)](https://github.com/mateuszkwiecinski/project-starter/actions)
&nbsp;[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

## Motivation

Maintaining multiple multi-module Android project often requires **copying project configuration across different projects**.
Same custom tasks, same _Android Gradle Plugin_ configuration. Same set of common settings.  
Even when project reached more advanced stage it is still required to put non minimal effort to maintain its configuration.
Things like new _kapt_ optimizations, new _Android Gradle Plugin_ features. New version of code style tools you were using.
Furthermore, starting a new project, from the scratch, **takes more than a day to configure every tool** you usually want to use.  
Some people create project templates, but they are up-to-date only when pulling them at the beginning and are not maintained further.
Another way of keeping your project configuration in a good shape was using `buildSrc` plugins.
Less code written, ease of sharing between projects but still some part of the code needed to be copied.  
This project addresses that issues and **exposes set of plugins** useful when approaching multi-module setup with _Gradle_ build system.  
It behaves as a **facade** for all most commonly used tools in Android development and makes easier to create CI pipelines knowing always which tasks should be executed.

## Content

Repository consists of several plugins that makes initial project configuration effortless and easily extensible.
Each module consists of configuration code most commonly used in Android project configuration.

### Getting started

#### Add buildscript dependency

Add root project `build.gradle`:
``` groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    
    dependencies {
        implementation "com.project.starter:plugins:0.4.0"
    }
}
```

### Plugins Configuration
#### Kotlin Library Plugin
Apply plugin to project level `build.gradle`

``` groovy
apply plugin: 'com.starter.library.kotlin'

// optional config with default values
projectConfig {
    javaFilesAllowed = false
}
```

- `javaFilesAllowed` - defines if the project can contain java files, fails the build otherwise

#### Android Application/Library Plugin
- Android Library plugin requires adding to project level `build.gradle`:

``` groovy
apply plugin: 'com.starter.library.android' // or 'com.starter.application.android'

// optional config with default values
projectConfig {
    javaFilesAllowed = false
    generateBuildConfig = false
    defaultVariants = ["debug"]
    coverageExclusions = [""]
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
General suggestion is to prefer Dependency injection over Android's flavor setup for libraries
- `defaultVariants` - defines build variants used as a dependency for common `projectXXX` tasks.  
for example setting `fullDebug` as default variant would make `testFullDebugUnitTest.` as a dependency for `projectTest` task.
`["freeRelease", "fullRelease"]` would make add `testFreeReleaseUnitTest` to `projectTest` and `testFreeReleaseLint` to `projectLint`.
- `coverageExclusions` - defines jacoco coverage exclusions for specific module

#### Quality Plugin

Apply plugin to project level `build.gradle`
```
 apply plugin: 'com.starter.quality'
```
which applies and configures `ktlint` and `detekt` tasks automatically.  
To execute run: `./gradlew projectCodeStyle`

#### Global configuration

Additional default configuration can be applied by adding to **root project** `build.gradle`.
All submodules will use this config as default

``` groovy
apply plugin: 'com.starter.config'
 
commonConfig {
    javaVersion = JavaVersion.VERSION_1_8
    javaFilesAllowed = true
    androidPlugin {
        compileSdkVersion = 29
        minSdkVersion = 23
        targetSdkVersion = 29
    }
    qualityPlugin {
        formatOnCompile = false
    }
}
```

- `javaVersion` - defines which java version source code is compatible with
- `javaFilesAllowed` - defines if the project can contain java files, fails the build otherwise.
- `androidPlugin`:
    - contains values passed to _Android Gradle Plugin_
- `qualityPlugin`:
    - `formatOnCompile` - defines if ktlint should format source code on every compilation

### Daily basis use
After applying library/application plugin there are appropriate tasks added:
- `./gradlew projectTest`  
Runs tests for all modules using either predefined tasks (i.e. `test` for kotlin modules or `testDebugUnitTest` for android libraries) or use customized values.
- `./gradlew projectLint`  
Runs Android lint checks against all modules (if custom lint checks are applied then for Kotlin modules too)
- `./gradlew projectCodeStyle`  
Verifies if code style matches modern standards using tools such as [`ktlint`](https://github.com/pinterest/ktlint) and [`Detekt`](https://github.com/arturbosch/detekt) with predefined config.
- `./gradlew projectCoverage`  
Automatically generates test coverage reports for all modules using [`Jacoco`](https://github.com/jacoco/jacoco)

Those tasks allows you to run tests efficiently for all modules by typing just a single task.
That solves an issue when for example `test` task unnecessarily executes tests for all build variants and more strict `testDebug` skips executing tests in kotlin only modules.

## Sample project
Sample [Github Browser](https://github.com/mateuszkwiecinski/github_browser) project.

## License
[MIT License](/LICENSE)
