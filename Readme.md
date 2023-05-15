## Starter
___

[![codecov](https://codecov.io/gh/usefulness/project-starter/branch/master/graph/badge.svg)](https://codecov.io/gh/usefulness/project-starter)
&nbsp;[![build](https://github.com/usefulness/project-starter/workflows/Build%20project/badge.svg)](https://github.com/usefulness/project-starter/actions)
&nbsp;[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

[![version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/project/starter/jvm/maven-metadata.xml?label=gradle)](https://plugins.gradle.org/search?term=com.starter) 


## Motivation

Maintaining multiple multi-module Android project often requires **copying project configuration across different projects**.
Even when project reaches more advanced stage it is still required to put non-minimal effort to maintain its configuration.  
Starting a new project, from the scratch, **takes more than a day** to configure every tool you usually want to use.
Sometimes people create template project or another way of keeping your project configuration in a good shape is using `buildSrc` plugins.
Less code written, ease of sharing between projects but still some part of the code needed to be copied.

This project goes further and addresses that issue by **exposing set of plugins** useful when approaching multi-module setup with _Gradle_ build system.

## Content

Repository consists of several plugins that makes initial project configuration effortless and easily extensible.
Each module consists of configuration code most commonly used in Android project configuration.

### Module plugins
#### Kotlin Library Plugin
Plugin configures [code style tasks](#quality-plugin), hooks for [common tasks](#day-to-day-use), 
sets coverage reports generation and manages [versioning](#versioning-plugin) of the artifact
    
Apply plugin to **project** level `build.gradle`

``` groovy
plugins {
    id("com.starter.library.kotlin") version("x.y.z")
}

// optional config with default values
projectConfig {
    javaFilesAllowed false
}
```

- `javaFilesAllowed` - defines if the project can contain java files, fails the build otherwise

#### Multiplatform Library Plugin
For kotlin multiplatform libraries apply plugin to **project** level `build.gradle`

``` groovy
plugins {
    id("com.starter.library.multiplatform") version("x.y.z")
}
```

#### Android Application/Library Plugin
In addition to customizations made to [Kotlin Library Plugin](#kotlin-library-plugin) Android plugins 
tweaks default Android Gradle Plugin setup by disabling _BuildConfig_ file generation 
or recognizing `src/main/kotlin` (and similar) path as a valid source set. 

Android Library plugin requires adding to **project** level `build.gradle`:

``` groovy
plugins {
    id("com.starter.library.android") version("x.y.z") 
    // or id("com.starter.application.android") version("x.y.z") 
}

// optional config with default values
projectConfig {
    javaFilesAllowed false
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
- `coverageExclusions` - defines jacoco coverage exclusions for specific module

##### Day-to-day use
After applying _Library_/_Application_ plugin following tasks become available:
- `./gradlew projectTest`  
  Runs tests for all modules using either predefined tasks (i.e. `test` for kotlin modules or `testDebugUnitTest` for android libraries) or use customized values.
- `./gradlew projectLint`  
  Runs Android lint checks against all modules (if custom lint checks are applied then for Kotlin modules too)
- `./gradlew projectCodeStyle`  
  Verifies if code style matches modern standards using tools such as [`ktlint`](https://github.com/pinterest/ktlint), [`Detekt`](https://github.com/arturbosch/detekt) with predefined config.
- `./gradlew projectCoverage`  
  Automatically generates test coverage reports for all modules using [`Jacoco`](https://github.com/jacoco/jacoco)

Those tasks allows you to run tests efficiently for all modules by typing just a single task.

### Standalone plugins
#### Quality Plugin
To only configure codestyle tools apply plugin to **project** level `build.gradle`
```
plugins {
    id("com.starter.quality") version("x.y.z") 
}
```
which applies and configures code style tasks for the project automatically.  

Tasks available:
- `./gradlew projectCodeStyle` - checks codestyle using all tools 
- `./gradlew issueLinksReport` - finds and check state of all issuetracker links linked in code comments  

Quality Plugin gets applied automatically when using any of module _Application_/_Library_ plugins above.

#### Versioning Plugin

Uses simple tag-based versioning, in a Configuration Cache compatible way.

To enable it as a standalone plugin, apply plugin to root project `build.gradle`
```
 apply plugin: 'com.starter.versioning'
```
Versioning plugin gets applied automatically when using any of module _Application_/_Library_ plugins above and can be disabled using [Global Configuration](Advanced.md#global-configuration)

### Advanced usage
See [Advanced usage](Advanced.md)

## Sample project
Sample [Github Browser](https://github.com/mateuszkwiecinski/github_browser) project - a customized, `buildSrc` based plugin application.

## License
The library is available under [MIT License](/LICENSE) and highly benefits from binary dependencies:
- `Kotlinter Gradle` - [License](https://github.com/jeremymailen/kotlinter-gradle/blob/master/LICENSE)
- `axion-relese-plugin` - [License](https://github.com/allegro/axion-release-plugin/blob/master/LICENSE)
- `Kotlin Gradle Plugin` - [License](https://github.com/JetBrains/kotlin#license)
- `Android Gradle Plugin` - [License](https://developer.android.com/license)
- `Detekt` - [License](https://github.com/arturbosch/detekt/blob/master/LICENSE)
