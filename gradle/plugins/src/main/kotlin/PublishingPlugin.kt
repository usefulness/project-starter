import com.gradle.publish.PluginBundleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

class PublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("maven-publish")
        pluginManager.apply("com.gradle.plugin-publish")

        extensions.configure<JavaPluginExtension> {
            withSourcesJar()
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
            with(publications) {
                register("mavenJava", MavenPublication::class.java) {
                    it.from(components.getByName("java"))
                }
            }
        }
        extensions.configure<PluginBundleExtension> {
            website = "https://github.com/usefulness/project-starter/"
            vcsUrl = "https://github.com/usefulness/project-starter.git"
            description = "Set of plugins that might be useful for Multi-Module Android projects."
            tags = listOf("android", "kotlin", "quickstart", "codestyle", "library", "baseline")
        }
    }

    private inline fun <reified T> ExtensionContainer.configure(crossinline receiver: T.() -> Unit) {
        configure(T::class.java) { receiver(it) }
    }
}

private fun Project.findConfig(key: String): String {
    return findProperty(key)?.toString() ?: System.getenv(key) ?: ""
}
