package com.project.starter.quality

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import com.project.starter.kotlinClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class QualityPluginTest : WithGradleProjectTest() {

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("build.gradle") {
                writeText(
                    """
                        plugins {
                            id('com.starter.quality')
                            id('org.jetbrains.kotlin.jvm')
                        }
                        
                        repositories.jcenter()
                    """.trimIndent()
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
    fun `formatOnCompile option enables failing builds if code style errors found`() {
        val enableFormatOnCompile = {
            @Language("groovy")
            val buildscript =
                """
                plugins {
                    id('com.starter.config')
                    id('com.starter.quality')
                    id('org.jetbrains.kotlin.jvm')
                }
                
                commonConfig {
                    qualityPlugin {
                        formatOnCompile = true
                    }
                }
                """.trimIndent()
            rootDirectory.resolve("build.gradle").writeText(buildscript)
        }

        rootDirectory.resolve("src/main//kotlin/WrongFileName.kt") {
            writeText(kotlinClass("DifferentClassName"))
        }

        val formatOnCompileOff = runTask("assemble")

        assertThat(formatOnCompileOff.task(":formatKotlin")?.outcome).isNull()

        enableFormatOnCompile()
        val formatOnCompileOn = runTask("assemble")

        assertThat(formatOnCompileOn.task(":formatKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCodeStyle fails if Checkstyle violation found`() {
        rootDirectory.resolve("src/test/java/JavaFileWithCheckstyleIssues.java") {
            @Language("java")
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

    @Test
    fun `projectCodeStyle is not present if java files are not allowed`() {
        @Language("groovy")
        val buildscript =
            """
            plugins {
                id('com.starter.config')
                id('com.starter.quality')
                id('org.jetbrains.kotlin.jvm')
            }
            
            commonConfig {
                javaFilesAllowed false
            }
            
            repositories.jcenter()
            """.trimIndent()
        rootDirectory.resolve("build.gradle").writeText(buildscript)

        val result = runTask("projectCodeStyle")

        assertThat(result.task(":checkstyle")).isNull()
    }

    @Test
    fun `detekt fails on magic number`() {
        rootDirectory.resolve("src/main/kotlin/MagicNumber.kt") {
            @Language("kotlin")
            val kotlinClass =
                """
                class MagicNumber {
                    var value: Int = 16
                }
                
                """.trimIndent()
            writeText(kotlinClass)
        }

        val result = runTask("projectCodeStyle", shouldFail = true)

        assertThat(result.task(":detekt")?.outcome).isEqualTo(TaskOutcome.FAILED)
    }
}
