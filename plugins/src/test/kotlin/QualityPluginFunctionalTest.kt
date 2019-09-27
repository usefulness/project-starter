import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class QualityPluginFunctionalTest {

    @get:Rule
    var folder = TemporaryFolder()

    @Test
    fun `projectCodeStyle runs Detekt`() {
        val projectDir = folder.newFolder().apply {
            mkdirs()
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            resolve("build.gradle").writeText("""
                allprojects { 
                    repositories {
                        jcenter()
                    }
                }
            """.trimIndent())
            resolve("module1").apply {
                mkdirs()
                resolve("build.gradle").writeText("""
                    plugins {
                        id('com.starter.quality')
                    }
                """.trimIndent())
                resolve("src/main/kotlin/ValidKotlinFile1.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                        data class ValidKotlinFile1(val name: String)
                        
                    """.trimIndent())
                }
            }
            resolve("module2").apply {
                mkdirs()
                resolve("build.gradle").writeText("""
                    plugins {
                        id('com.starter.quality')
                    }
                """.trimIndent())
                resolve("src/main/kotlin/ValidKotlinFile2.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                        data class ValidKotlinFile2(val name: String)
                        
                    """.trimIndent())
                }
            }
        }

        val result = GradleRunner.create().apply {
            forwardOutput()
            withPluginClasspath()
            withArguments("projectCodeStyle")
            withProjectDir(projectDir)
        }.build()

        assertThat(result.task(":module1:detekt")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:detekt")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCodeStyle runs ktlint`() {
        val projectDir = folder.newFolder().apply {
            mkdirs()
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            resolve("build.gradle").writeText("""
                allprojects { 
                    repositories {
                        jcenter()
                    }
                }
            """.trimIndent())
            resolve("module1").apply {
                mkdirs()
                resolve("build.gradle").writeText("""
                    plugins {
                        id('com.starter.quality')
                        id('kotlin')
                    }
                """.trimIndent())
                resolve("src/main/kotlin/ValidKotlinFile1.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                        data class ValidKotlinFile1(val name: String)
                        
                    """.trimIndent())
                }
            }
            resolve("module2").apply {
                mkdirs()
                resolve("build.gradle").writeText("""
                    plugins {
                        id('com.starter.quality')
                        id('kotlin')
                    }
                """.trimIndent())
                resolve("src/main/kotlin/ValidKotlinFile2.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                        data class ValidKotlinFile2(val name: String)
                        
                    """.trimIndent())
                }
            }
        }

        val result = GradleRunner.create().apply {
            forwardOutput()
            withPluginClasspath()
            withArguments("projectCodeStyle")
            withProjectDir(projectDir)
        }.build()

        assertThat(result.task(":module1:lintKotlinMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module1:lintKotlinTest")?.outcome).isEqualTo(TaskOutcome.NO_SOURCE)
        assertThat(result.task(":module2:lintKotlinMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:lintKotlinTest")?.outcome).isEqualTo(TaskOutcome.NO_SOURCE)
    }
}
