package com.project.starter.quality

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import com.project.starter.kotlinClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class AndroidQualityPluginTest : WithGradleProjectTest() {

    private lateinit var module1Root: File
    private lateinit var module2Root: File

    @BeforeEach
    @Suppress("LongMethod")
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            resolve("build.gradle")
            module1Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        // language=groovy
                        """
                        plugins {
                            id('com.starter.quality')
                            id('kotlin')
                        }
                        
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
            module2Root = resolve("module2").apply {
                mkdirs()
                val script =
                    // language=groovy
                    """
                    plugins {
                        id('com.starter.quality')
                        id('com.android.library')
                        id('kotlin-android')
                    }
                    
                    repositories {
                        google()
                        mavenCentral()
                    }
                    
                    android {
                        compileSdkVersion 29
                        defaultConfig {
                            minSdkVersion 23
                        }
                    }
                    
                    """.trimIndent()
                resolve("build.gradle").writeText(script)
                resolve("src/main/AndroidManifest.xml") {
                    writeText(
                        """
                        <manifest package="com.example.module2" />
                        """.trimIndent()
                    )
                }
                resolve("src/main/java/ValidKotlinFile2.kt") {
                    writeText(kotlinClass("ValidKotlinFile2"))
                }
                resolve("src/main/java/ValidJava2.java") {
                    writeText(javaClass("ValidJava2"))
                }
                resolve("src/test/java/ValidKotlinTest2.kt") {
                    writeText(kotlinClass("ValidKotlinTest2"))
                }
                resolve("src/test/java/ValidJavaTest2.java") {
                    writeText(javaClass("ValidJavaTest2"))
                }
            }
        }
    }

    @Test
    fun `projectCodeStyle runs Detekt`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":module1:detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCodeStyle runs ktlint`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":module1:lintKotlinMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module1:lintKotlinTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:lintKotlinMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:lintKotlinTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `formatOnCompile option enables failing builds if code style errors found`() {
        val enableFormatOnCompile = {
            val buildscript =
                // language=groovy
                """
                plugins {
                    id('com.starter.config')
                }
                
                commonConfig {
                    qualityPlugin {
                        formatOnCompile = true
                    }
                }
                """.trimIndent()
            rootDirectory.resolve("build.gradle").writeText(buildscript)
        }

        module1Root.resolve("src/main//kotlin/WrongFileName.kt") {
            writeText(kotlinClass("DifferentClassName"))
        }

        val formatOnCompileOff = runTask("assemble")

        assertThat(formatOnCompileOff.task(":module1:formatKotlin")?.outcome).isNull()
        assertThat(formatOnCompileOff.task(":module2:formatKotlin")?.outcome).isNull()

        enableFormatOnCompile()
        val formatOnCompileOn = runTask("assemble")

        assertThat(formatOnCompileOn.task(":module1:formatKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(formatOnCompileOn.task(":module2:formatKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCodeStyle fails if Checkstyle violation found`() {
        module2Root.resolve("src/test/java/JavaFileWithCheckstyleIssues.java") {
            val javaClass =
                // language=groovy
                """
                public class JavaFileWithCheckstyleIssues {
    
                    int test() {
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

        assertThat(result.task(":module1:checkstyle")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:checkstyleTest")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output)
            .contains("WhitespaceAround: 'if' is not followed by whitespace.")
            .contains("WhitespaceAround: '{' is not preceded with whitespace")
    }

    @Test
    fun `projectCodeStyle is not present if java files are not allowed`() {
        val buildscript =
            // language=groovy
            """
            plugins {
                id('com.starter.config')
            }
            
            commonConfig {
                javaFilesAllowed = false
            }
            """.trimIndent()
        rootDirectory.resolve("build.gradle").writeText(buildscript)

        val result = runTask("projectCodeStyle")

        assertThat(result.task(":module1:checkstyle")).isNull()
        assertThat(result.task(":module2:checkstyle")).isNull()
    }

    @Test
    fun `detekt fails on magic number`() {
        module2Root.resolve("src/main/kotlin/MagicNumber.kt") {
            val kotlinClass =
                // language=groovy
                """
                class MagicNumber {
                    var value: Int = 16
                }
                
                """.trimIndent()
            writeText(kotlinClass)
        }

        val result = runTask("projectCodeStyle", shouldFail = true)

        assertThat(result.task(":module2:detekt")?.outcome).isEqualTo(TaskOutcome.FAILED)
    }
}
