package com.project.starter.modules.tasks

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

internal class ConfigurationCacheTest : WithGradleProjectTest() {

    lateinit var androidModuleRoot: File
    lateinit var kotlinModuleRoot: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ':module1', ':module2' """)

            resolve("build.gradle").writeText("")
            androidModuleRoot = resolve("module1") {
                @Language("groovy")
                val script =
                    """
                    plugins {
                        id 'com.starter.library.android' 
                    }

                    """.trimIndent()
                resolve("build.gradle") {
                    writeText(script)
                }
                resolve("src/main/AndroidManifest.xml") {
                    writeText(
                        """
                        <manifest package="com.example.module1" />
                        """.trimIndent(),
                    )
                }
                resolve("src/main/java/ValidJava2.java") {
                    writeText(javaClass("ValidJava2"))
                }
                resolve("src/test/java/ValidJavaTest2.java") {
                    writeText(javaClass("ValidJavaTest2"))
                }
            }
            kotlinModuleRoot = resolve("module2") {
                @Language("groovy")
                val script =
                    """
                    plugins {
                        id 'com.starter.library.kotlin' 
                    }

                    """.trimIndent()
                resolve("build.gradle") {
                    writeText(script)
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

    /**
     * https://youtrack.jetbrains.com/issue/KT-38498
     * https://issuetracker.google.com/issues/156552742
     */
    @Disabled("Configuration cache is not yet supported")
    @Test
    fun `does not fail with configuration cache`() {
        runTask("--configuration-cache")
    }
}
