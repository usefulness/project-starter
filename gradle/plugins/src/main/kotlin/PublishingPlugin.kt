import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaTask

class PublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("maven-publish")
        pluginManager.apply("com.gradle.plugin-publish")
        if (findConfig("SIGNING_PASSWORD").isNotEmpty()) {
            pluginManager.apply("signing")
        }

        extensions.configure<JavaPluginExtension> {
            withSourcesJar()
            withJavadocJar()
        }

        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            pluginManager.apply("org.jetbrains.dokka")

            tasks.withType(DokkaTask::class.java).configureEach { dokkaTask ->
                dokkaTask.notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/1217")
            }
            tasks.named("javadocJar", Jar::class.java) { javadocJar ->
                javadocJar.from(tasks.named("dokkaJavadoc"))
            }
            tasks.named("processResources", ProcessResources::class.java) { processResources ->
                processResources.from(rootProject.file("LICENSE"))
            }
        }

        extensions.configure<PublishingExtension> {
            with(repositories) {
                maven { maven ->
                    maven.name = "github"
                    maven.setUrl("https://maven.pkg.github.com/usefulness/project-starter")
                    with(maven.credentials) {
                        username = "usefulness"
                        password = findConfig("GITHUB_TOKEN")
                    }
                }
            }
        }
        pluginManager.withPlugin("signing") {
            with(extensions.extraProperties) {
                set("signing.keyId", findConfig("SIGNING_KEY_ID"))
                set("signing.password", findConfig("SIGNING_PASSWORD"))
                set("signing.secretKeyRingFile", findConfig("SIGNING_SECRET_KEY_RING_FILE"))
            }

            extensions.configure<SigningExtension>("signing") { signing ->
                if (findConfig("SIGNING_PASSWORD").isNotEmpty()) {
                    signing.sign(extensions.getByType(PublishingExtension::class.java).publications)
                }
            }
        }

        extensions.configure<GradlePluginDevelopmentExtension> {
            website.set("https://github.com/usefulness/project-starter/")
            vcsUrl.set("https://github.com/usefulness/project-starter.git")
            plugins.configureEach { plugin ->
                plugin.tags.set(listOf("android", "kotlin", "quickstart", "codestyle", "library", "baseline"))
                plugin.description = "Set of plugins that might be useful for Multi-Module Android projects."
            }
        }
    }

    private inline fun <reified T: Any> ExtensionContainer.configure(crossinline receiver: T.() -> Unit) {
        configure(T::class.java) { receiver(it) }
    }
}

private fun Project.findConfig(key: String): String {
    return findProperty(key)?.toString() ?: System.getenv(key) ?: ""
}
