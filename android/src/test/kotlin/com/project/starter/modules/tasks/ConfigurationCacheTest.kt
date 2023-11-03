package com.project.starter.modules.tasks

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class ConfigurationCacheTest : WithGradleProjectTest() {

    lateinit var androidModuleRoot: File
    lateinit var kotlinModuleRoot: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ':module1', ':module2' """)

            resolve("build.gradle") {
                writeText(
                    """
                    plugins {
                        id('com.starter.config')
                    }
                    
                    commonConfig {
                        javaVersion = JavaVersion.VERSION_1_8 // workaround for http://issuetracker.google.com/issues/294137077
                    }
                    """.trimIndent(),
                )
            }
            androidModuleRoot = resolve("module1") {
                // language=groovy
                val script =
                    """
                    plugins {
                        id 'com.starter.library.android' 
                    }

                    android {
                        namespace "com.example.module1"
                    }

                    """.trimIndent()
                resolve("build.gradle") {
                    writeText(script)
                }
                resolve("src/main/java/ValidJava2.java") {
                    writeText(javaClass("ValidJava2"))
                }
                resolve("src/test/java/com/example/ValidJavaTest2.java") {
                    writeText(javaClass("ValidJavaTest2"))
                }
            }
            kotlinModuleRoot = resolve("module2") {
                // language=groovy
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
                resolve("src/test/java/com/example/ValidJavaTest2.java") {
                    writeText(javaClass("ValidJavaTest2"))
                }
            }
        }
    }

    /**
     * https://youtrack.jetbrains.com/issue/KT-38498
     * https://issuetracker.google.com/issues/156552742
     */
    @Test
    fun `does not fail with configuration cache`() {
        runTask("assemble", "-m", configurationCacheEnabled = true)
    }
}
