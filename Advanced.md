## Advanced usage

### Global configuration
Additional default configuration can be applied by adding to the **root project**'s `build.gradle`.
All submodules will use this config as default

``` groovy
plugins {
  id("com.starter.config") version("x.y.z")
}
 
commonConfig {
    javaVersion JavaVersion.VERSION_17
    androidPlugin {
        compileSdkVersion 36
        minSdkVersion 29
        targetSdkVersion 36
    }
    qualityPlugin {
        formatOnCompile = false
    }
}
```

- `javaVersion` - defines which java version source code is compatible with
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
- `ktlint`  
  Unfortunately it is not possible to generate `ktlint` baseline.
  Proper code style may be achieved by using `./gradlew formatKotlin` task.
