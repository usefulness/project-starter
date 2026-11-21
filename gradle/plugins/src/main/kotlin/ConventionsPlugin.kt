import io.github.usefulness.KtlintGradleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverExtension

@OptIn(ExperimentalAbiValidation::class)
class ConventionsPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            val kotlinVersion = getKotlinPluginVersion()
            configurations.configureEach { configuration ->
                // TODO: drop once ktlint runs on a Kotlin version matching KGP. ktlint 1.8.0 bundles
                //  kotlin-compiler-embeddable 2.2.21 and fails to initialize when forced to 2.4.20.
                //  Tracked by https://github.com/usefulness/project-starter/pull/883
                if (configuration.name.startsWith("ktlint")) return@configureEach
                configuration.resolutionStrategy.eachDependency { details ->
                    if (details.requested.group == "org.jetbrains.kotlin" && details.requested.name.startsWith("kotlin")) {
                        details.useVersion(kotlinVersion)
                    }
                }
            }
            extensions.configure(KotlinJvmProjectExtension::class.java) { kotlin ->
                kotlin.jvmToolchain(libs.findVersion("java-compilation").get().requiredVersion.toInt())
            }
            tasks.withType(KotlinCompilationTask::class.java).configureEach { task ->
                task.compilerOptions.freeCompilerArgs.add("-Xlambdas=class")
            }
        }

        pluginManager.withPlugin("java-gradle-plugin") {
            val testRuntimeDependencies = configurations.register("testRuntimeDependencies") { configuration ->
                // KGP publishes multiple variants https://kotlinlang.org/docs/whatsnew17.html#support-for-gradle-plugin-variants
                configuration.attributes { attributes ->
                    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
                    attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
                }
            }

            // Required to put the Kotlin plugin on the classpath for the functional test suite
            tasks.withType(PluginUnderTestMetadata::class.java).configureEach { task ->
                task.pluginClasspath.from(testRuntimeDependencies)
            }
        }

        tasks.withType(Test::class.java).configureEach { test ->
            test.doLast {
                Thread.sleep(2000) // https://github.com/gradle/gradle/issues/16603
            }
        }

        pluginManager.withPlugin("java") {
            if (hasProperty("skipJarVersion")) {
                val projectName = name
                tasks.named("jar", Jar::class.java) { jar ->
                    jar.archiveFileName.set("$projectName.jar")
                }
            }
        }

        pluginManager.withPlugin("com.starter.publishing") {
            extensions.configure(KotlinJvmProjectExtension::class.java) { kotlin ->
                kotlin.abiValidation()
            }
        }

        pluginManager.withPlugin("io.github.usefulness.ktlint-gradle-plugin") {
            extensions.configure(KtlintGradleExtension::class.java) { ktlint ->
                ktlint.ktlintVersion.set(libs.findVersion("maven-ktlint").get().requiredVersion)
            }
        }

        pluginManager.withPlugin(libs.findPlugin("kotlin-samwithreceiver").get().get().pluginId) {
            extensions.configure(SamWithReceiverExtension::class.java) { samWithReceiver ->
                samWithReceiver.annotation("org.gradle.api.HasImplicitReceiver")
            }
        }
    }
}
