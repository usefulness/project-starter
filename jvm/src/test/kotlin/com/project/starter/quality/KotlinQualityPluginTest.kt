package com.project.starter.quality

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import com.project.starter.kotlinClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KotlinQualityPluginTest : WithGradleProjectTest() {

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("build.gradle") {
                writeText(
                    // language=groovy
                    """
                    plugins {
                        id('com.starter.library.kotlin')
                    }
                    
                    """.trimIndent(),
                )
            }
            resolve("src/main/kotlin/ValidKotlinFile1.kt") {
                writeText(kotlinClass("ValidKotlinFile1"))
            }
            resolve("src/main/java/ValidJava1.java") {
                writeText(javaClass("ValidJava1"))
            }
            resolve("src/debug/java/DebugJava.java") {
                writeText(javaClass("DebugJava"))
            }
            resolve("src/test/kotlin/ValidKotlinTest1.kt") {
                writeText(kotlinClass("ValidKotlinTest1"))
            }
            resolve("src/test/java/ValidJavaTest1.java") {
                writeText(javaClass("ValidJavaTest1"))
            }
        }
    }

    @Test
    fun `projectCodeStyle runs Detekt`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCodeStyle runs ktlint`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":lintKotlinMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":lintKotlinTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCodeStyle fails if Checkstyle violation found`() {
        rootDirectory.resolve("build.gradle") {
            appendText(
                // language=groovy
                """
                projectConfig {
                    javaFilesAllowed = true
                }
                """.trimIndent(),
            )
        }
        rootDirectory.resolve("src/test/java/JavaFileWithCheckstyleIssues.java") {
            // language=java
            val javaClass =
                """
                import java.io.IOException;
                
                public class JavaFileWithCheckstyleIssues {
    
                    int test() throws IOException {
                        int variable = System.in.read();
                        if(variable % 2 == 1){
                            return 2;
                        } else {
                            return 3;
                        }
                    }
                }
                
                """.trimIndent()
            writeText(javaClass)
        }

        val result = runTask("projectCodeStyle", shouldFail = true)

        assertThat(result.task(":checkstyleTest")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output)
            .contains("WhitespaceAround: 'if' is not followed by whitespace.")
            .contains("WhitespaceAround: '{' is not preceded with whitespace")
    }
}
