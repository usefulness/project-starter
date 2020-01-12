package com.project.starter.quality

import com.project.starter.WithGradleTest
import com.project.starter.javaClass
import java.io.File
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Before

internal class QualityPluginTest : WithGradleTest() {

    private lateinit var module1Root: File
    private lateinit var module2Root: File

    @Before
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            resolve("build.gradle").writeText("""
            """.trimIndent())
            module1Root = resolve("module1").apply {
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
                        object ValidKotlinFile1
                        
                    """.trimIndent())
                }
                resolve("src/main/java/ValidJava1.java").apply {
                    parentFile.mkdirs()
                    writeText(javaClass("ValidJava1"))
                }
                resolve("src/debug/java/DebugJava.java").apply {
                    parentFile.mkdirs()
                    writeText(javaClass("DebugJava"))
                }
                resolve("src/test/kotlin/ValidKotlinTest1.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                        object ValidKotlinTest1
                        
                    """.trimIndent())
                }
                resolve("src/test/java/ValidJavaTest1.java").apply {
                    parentFile.mkdirs()
                    writeText(javaClass("ValidJavaTest1"))
                }
            }
            module2Root = resolve("module2").apply {
                mkdirs()
                resolve("build.gradle").writeText("""
                    plugins {
                        id('com.starter.quality')
                        id('com.android.library')
                        id('kotlin-android')
                    }
                    
                    repositories {
                        google()
                    }
                    
                    android {
                        compileSdkVersion 29
                        defaultConfig {
                            minSdkVersion 23
                        }
                    }
                    
                """.trimIndent())
                resolve("src/main/AndroidManifest.xml").apply {
                    parentFile.mkdirs()
                    writeText("""
                         <manifest package="com.example.module2" />
                    """.trimIndent())
                }
                resolve("src/main/java/ValidKotlinFile2.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                        object ValidKotlinFile2
                        
                    """.trimIndent())
                }
                resolve("src/main/java/ValidJava2.java").apply {
                    parentFile.mkdirs()
                    writeText(javaClass("ValidJava2"))
                }
                resolve("src/test/java/ValidKotlinTest2.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                        object ValidKotlinTest2
                        
                    """.trimIndent())
                }
                resolve("src/test/java/ValidJavaTest2.java").apply {
                    parentFile.mkdirs()
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
        @Language("groovy") val buildscript = """
            plugins {
                id('com.starter.config')
            }
            
            commonConfig {
                qualityPlugin {
                    formatOnCompile = true
                }
            }
        """.trimIndent()
        rootDirectory.resolve("build.gradle").appendText(buildscript)
        module1Root.resolve("src/main//kotlin/WrongFileName.kt").apply {
            parentFile.mkdirs()
            writeText("""
                object DifferentClassName
                
            """.trimIndent())
        }

        val result = runTask("assemble")

        assertThat(result.task(":module1:formatKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:formatKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCodeStyle fails if Checkstyle violation found`() {
        module2Root.resolve("src/test/java/JavaFileWithCheckstyleIssues.java").apply {
            parentFile.mkdirs()
            @Language("java")
            val javaClass = """
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
}
