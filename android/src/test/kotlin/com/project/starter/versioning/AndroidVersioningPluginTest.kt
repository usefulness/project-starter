package com.project.starter.versioning

import com.project.starter.WithGradleProjectTest
import com.project.starter.commit
import com.project.starter.setupGit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AndroidVersioningPluginTest : WithGradleProjectTest() {

    private lateinit var androidAppRoot: File

    @TempDir
    lateinit var origin: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":androidApp"""")

            androidAppRoot = resolve("androidApp") {
                resolve("src/main/AndroidManifest.xml") {
                    writeText(
                        """
                        <manifest package="com.example.module1" />
                        
                        """.trimIndent(),
                    )
                }
                resolve("build.gradle") {
                    // language=groovy
                    val buildscript =
                        """
                        plugins {
                            id 'com.starter.application.android'
                        }
                        
                        tasks.register("printVersion") {
                            doLast {
                                println("version_code=" + android.defaultConfig.versionCode)
                                println("version_name=" + android.defaultConfig.versionName)
                           }
                        }
                        """.trimIndent()
                    writeText(buildscript)
                }
            }
        }
    }

    @Test
    internal fun `sets android application version`() {
        rootDirectory.resolve("build.gradle").writeText(
            // language=groovy
            """
            plugins {
                id 'com.starter.versioning'
            }
            """.trimIndent(),
        )
        val git = setupGit(origin)
        runTask("markNextVersion", "-Prelease.version=1.2.3")
        git.commit("contains 1.2.3 features")

        val result = runTask("printVersion")

        assertThat(result.output).contains("version_name=1.2.3")
        assertThat(result.output).contains("version_code=1002003")
    }
}
