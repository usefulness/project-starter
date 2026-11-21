## Advanced usage

### Global configuration
Additional default configuration can be applied in `settings.gradle`.
`com.starter.config` is a **settings plugin**, so that no project needs to read another project's state (compatible with [Isolated Projects](https://docs.gradle.org/current/userguide/isolated_projects.html)).
All projects will use this config as default

``` groovy
// settings.gradle
plugins {
  id("com.starter.config") version("x.y.z")
}
 
commonConfig {
    javaVersion JavaVersion.VERSION_17
    androidPlugin {
        compileSdkVersion 37
        compileSdkMinorVersion 1
        minSdkVersion 29
        targetSdkVersion 37
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
  
### Generating baselines
It is possible to generate baseline for every quality tool available in the project.
- `Android Lint`  
  Type `rm **/lint-*.xml ; ./gradlew projectLint -PrefreshBaseline --continue` into console
- `ktlint`  
  Unfortunately it is not possible to generate `ktlint` baseline.
  Proper code style may be achieved by using `./gradlew formatKotlin` task.
