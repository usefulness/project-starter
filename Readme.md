## Starter
___

## Motivation

Multimodule projects. Copied code. BuildSrc. Use it in your own setup.

## Content

Modules + Quality + TODO(~Versioning~) + TODO(~Publishing~)

### Getting started

#### Add buildscript dependency

 `/buildSrc/build.gradle`:
``` groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/mateuszkwiecinski/project-starter")
    }
    mavenContent {
        includeGroup("com.project.starter")
    }
}
dependencies {
    implementation 'com.project.starter:plugins:0.2.0'
}
```

### Plugins Configuration
1. Kotlin Library Plugin  
    Apply plugin in project level `build.gradle`

    ``` groovy
    apply plugin: 'plugin-library.kotlin'
    ```

1. Android Application/Library Plugin
    - Minimal setup for Android Library requires adding in project level `build.gradle`:  
    `apply plugin: 'plugin-library.android'`
    or for Android Application
    `apply plugin: 'plugin-application.android'`
    - Advanced setup
        - `javaFilesAllowed` - defines if project can contain java files, `false` by default
        - `generateBuildConfig` - defines if `BuildConfig.java` class will be generated, `false` by default
        - `defaultVariants` - defines build variants used for common `projectXXX` tasks.  
         for example setting `fullDebug` as default varian would make `testFullDebugUnitTest.` as a dependency for `projectTest` task. \["debug"\]` by default
        - `coverageExclusions` - defines jacoco coverage exclusions for specific module, `[]` by default

    ``` groovy
    apply plugin: 'plugin-library.android' // or 'plugin-application.android'

    libraryConfig {
        javaFilesAllowed = false
        generateBuildConfig = false
        defaultVariants = ["fullDebug", "freeDebug"]
        coverageExclusions = ["*_GeneratedFile.*"]
    }
    
    android {
        defaultConfig {
            minSdkVersion 21
        }
    }
    ```
1. Quality Plugin
2. Global configuration

### Daily use
After applying plugins there are appropriate tasks added:
- `projectTest`  
Runs tests for all modules using either predefined tasks (i.e. `test` for kotlin modules or `testDebugUnitTest` for android libraries) or use customized values.
- `projectLint`
Runs Android lint checks against all modules (if custom lint checks are applied then for Kotlin modules too)
- `projectCodeStyle`
Verifies if code style matches modern standards using tools such as [`ktlint`](https://github.com/pinterest/ktlint) and [`Detekt`](https://github.com/arturbosch/detekt) with predefined config.
- ~`projectCoverage`~
Automatically generates test coverage reports for all modules using [`Jacoco`](https://github.com/jacoco/jacoco)

Those tasks allows you to run tests efficiently for all modules typing single task.
That solves an issue when for example `test` task unnecessarily executes tests for all build variants where there is only single variant needed
and from the other side, the `testDebug` skips executing tests in kotlin only modules.

## Sample project
[link]

## License
