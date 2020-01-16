package com.project.starter.quality.tasks

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test

internal class GenerateCheckstyleBaselineTaskTest : WithGradleProjectTest() {

    private lateinit var moduleRoot: File

    @Before
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ':javaModule' """)

            resolve("build.gradle").writeText("")
            moduleRoot = resolve("javaModule") {
                @Language("groovy")
                val script = """
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
                    
                """.trimIndent()
                resolve("build.gradle") {
                    writeText(script)
                }
                resolve("src/main/AndroidManifest.xml") {
                    writeText("""
                         <manifest package="com.example.module2" />
                    """.trimIndent())
                }
                resolve("src/main/java/ValidJava2.java") {
                    writeText(javaClass("ValidJava2"))
                }
                resolve("src/test/java/ValidJavaTest2.java") {
                    writeText(javaClass("ValidJavaTest2"))
                }
            }
        }
    }

    @Test
    fun `generating baseline makes build to pass on old code, but fail on new one`() {
        moduleRoot.resolve("src/test/java/OldCode.java") {
            @Language("java")
            val javaClass = """
                public class OldCode {
                    
                    void test(){
                        System.out.println("");
                    }
                }
            """.trimIndent()
            writeText(javaClass)
        }

        val baselineResult = runTask("generateCheckstyleBaseline")
        val checkStyleOldCode = runTask("checkstyle")

        assertThat(baselineResult.task(":javaModule:generateCheckstyleBaseline")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(checkStyleOldCode.task(":javaModule:checkstyle")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        moduleRoot.resolve("src/test/java/NewCode.java") {
            @Language("java")
            val javaClass = """
                public class NewCode {
                    
                    void test(){
                        System.out.println("");
                    }
                }
            """.trimIndent()
            writeText(javaClass)
        }

        val checkstyleNewCode = runTask("checkstyle", shouldFail = true)

        assertThat(checkstyleNewCode.task(":javaModule:checkstyleTest")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(checkstyleNewCode.output).contains("""NewCode.java:3:16: WhitespaceAround: '{' is not preceded with whitespace""")
    }
}
