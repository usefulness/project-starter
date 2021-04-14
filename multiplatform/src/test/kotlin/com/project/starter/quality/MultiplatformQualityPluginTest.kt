package com.project.starter.quality

import com.project.starter.WithGradleProjectTest
import com.project.starter.kotlinClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MultiplatformQualityPluginTest : WithGradleProjectTest() {

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            //language=groovy
            val script =
                """
                    plugins {
                        id('com.starter.library.multiplatform')
                    }
                    
                    kotlin {
                        jvm()
                        ios()
                    }
                """.trimIndent()
            resolve("build.gradle") {
                writeText(script)
            }
            resolve("src/commonMain/kotlin/ValidKotlinFile1.kt") {
                writeText(kotlinClass("ValidKotlinFile1"))
            }
            resolve("src/commonTest/kotlin/ValidKotlinTest1.kt") {
                writeText(kotlinClass("ValidKotlinTest1"))
            }
            resolve("src/jvmMain/kotlin/ValidKotlinJvmFile1.kt") {
                writeText(kotlinClass("ValidKotlinJvmFile1"))
            }
            resolve("src/jvmTest/kotlin/ValidJvmKotlinTest1.kt") {
                writeText(kotlinClass("ValidJvmKotlinTest1"))
            }
            resolve("src/iosMain/kotlin/ValidKotlinIosFile1.kt") {
                writeText(kotlinClass("ValidKotlinIosFile1"))
            }
            resolve("src/iosTest/kotlin/ValidIosKotlinTest1.kt") {
                writeText(kotlinClass("ValidIosKotlinTest1"))
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

        assertThat(result.task(":lintKotlinIosMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":lintKotlinJvmMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":lintKotlinCommonMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        assertThat(result.task(":lintKotlinIosTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":lintKotlinJvmTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":lintKotlinCommonTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
