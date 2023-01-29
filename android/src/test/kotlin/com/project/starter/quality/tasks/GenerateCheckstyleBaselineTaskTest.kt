package com.project.starter.quality.tasks

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class GenerateCheckstyleBaselineTaskTest : WithGradleProjectTest() {

    private lateinit var moduleRoot: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ':javaModule' """)

            resolve("build.gradle").writeText(
                // language=groovy
                """
                plugins {
                        id('com.starter.config')
                    }
                    
                    commonConfig {
                        javaFilesAllowed = true
                    }
                """.trimIndent(),
            )
            moduleRoot = resolve("javaModule") {
                // language=groovy
                val script =
                    """
                    plugins {
                        id('com.starter.quality')
                        id('com.android.library')
                        id('kotlin-android')
                    }
                    
                    repositories {
                        google()
                    }
                    
                    android {
                        namespace "com.example.module2"
                        compileSdkVersion 33
                        defaultConfig {
                            minSdkVersion 26
                        }
                    }
                    
                    """.trimIndent()
                resolve("build.gradle") {
                    writeText(script)
                }
                resolve("src/main/AndroidManifest.xml") {
                    writeText(
                        """
                        <manifest />
                        """.trimIndent(),
                    )
                }
                resolve("src/main/java/ValidJava2.java") {
                    writeText(javaClass("ValidJava2"))
                }
                resolve("src/test/java/com/example/ValidJavaTest2.java") {
                    writeText(javaClass("ValidJavaTest2"))
                }
            }
        }
    }

    @Test
    fun `generating baseline makes build to pass on old code, but fail on new one`() {
        moduleRoot.resolve("src/test/java/OldCode.java") {
            // language=java
            val javaClass =
                """
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
            // language=java
            val javaClass =
                """
                public class NewCode {
                    
                    void test(){
                        System.out.println("");
                    }
                }
                """.trimIndent()
            writeText(javaClass)
        }

        val checkstyleNewCode = runTask("checkstyle", shouldFail = true)

        assertThat(checkstyleNewCode.task(":javaModule:checkstyleDebugUnitTest")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(checkstyleNewCode.output).contains("""NewCode.java:3:16: WhitespaceAround: '{' is not preceded with whitespace""")
    }
}
