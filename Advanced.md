## Advanced usage

### Global configuration
Additional default configuration can be applied by adding to the **root project**'s `build.gradle`.
All submodules will use this config as default

``` groovy
plugins {
  id("com.starter.config") version("x.y.z)
}
 
commonConfig {
    javaVersion JavaVersion.VERSION_1_8
    javaFilesAllowed = true
    androidPlugin {
        compileSdkVersion 29
        minSdkVersion 23
        targetSdkVersion 29
    }
    qualityPlugin {
        formatOnCompile = false
    }
    versioningPlugin {
        enabled = true
    }
}
```

- `javaVersion` - defines which java version source code is compatible with
- `javaFilesAllowed` - defines if the project can contain java files, fails the build otherwise.
- `androidPlugin`:
  - contains values passed to _Android Gradle Plugin_
- `qualityPlugin`:
  - `formatOnCompile` - defines if ktlint should format source code on every compilation
- `versioningPlugin`:
  - `enabled` - enables/disables [Versioning Plugin](..#versioning-plugin)
  
### Generating baselines
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
