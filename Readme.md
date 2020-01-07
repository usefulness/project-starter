## Starter
___

[![codecov](https://codecov.io/gh/mateuszkwiecinski/project-starter/branch/master/graph/badge.svg)](https://codecov.io/gh/mateuszkwiecinski/project-starter)
&nbsp;[![codecov](https://github.com/mateuszkwiecinski/project-starter/workflows/Build%20project/badge.svg)](https://github.com/mateuszkwiecinski/project-starter/actions)
&nbsp;[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

## Motivation

Multi-Module projects. Copied code. BuildSrc. Use it in your own setup.

## Content

Repository consists of several plugins group: Modules, Quality, ~Versioning~ and ~Publishing~.
Each module consists of configuration code most commonly used in Android projects.

### Getting started

#### Add buildscript dependency

Add root project `build.gradle`:
``` groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
}
```

### Plugins Configuration
1. Kotlin Library Plugin  
Apply plugin to project level `build.gradle`

``` groovy
apply plugin: 'com.starter.library.kotlin'

// optional config with default values
projectConfig {
    javaFilesAllowed = false
}
```

- `javaFilesAllowed` - defines if the project can contain java files, fails the build otherwise

1. Android Application/Library Plugin
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

// overriden settings for single project
android {
    defaultConfig {
        minSdkVersion 21
    }
}
```

- `javaFilesAllowed` - defines if the project can contain java files, fails the build otherwise
- `generateBuildConfig` - defines if `BuildConfig.java` class will be generated
- `defaultVariants` - defines build variants used for common `projectXXX` tasks.  
for example setting `fullDebug` as default varian would make `testFullDebugUnitTest.` as a dependency for `projectTest` task.
- `coverageExclusions` - defines jacoco coverage exclusions for specific module

2. Quality Plugin

Apply plugin to project level `build.gradle`
```
 apply plugin: 'com.starter.quality'
```
which applies and configures `ktlint` and `detekt` tasks automatically.  
To execute run: `./gradlew projectCodeStyle`

3. Global configuration

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

- `javaVersion` - defines which java version source code is compatible to
- `javaFilesAllowed` - defines if the project can contain java files, fails the build otherwise
- `androidPlugin`:
    - contains values passed to Android Gradle Plugin
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
