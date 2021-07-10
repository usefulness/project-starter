package com.project.starter.modules

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class ConfigurationCacheTest : WithGradleProjectTest() {

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
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
            resolve("src/test/java/ValidJavaTest2.java") {
                writeText(javaClass("ValidJavaTest2"))
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
