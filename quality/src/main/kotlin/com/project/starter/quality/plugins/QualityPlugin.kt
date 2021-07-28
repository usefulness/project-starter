package com.project.starter.quality.plugins

import com.android.build.gradle.BaseExtension
import com.project.starter.config.plugins.rootConfig
import com.project.starter.quality.internal.configureCheckstyle
import com.project.starter.quality.internal.configureDetekt
import com.project.starter.quality.internal.configureKtlint
import com.project.starter.quality.tasks.IssueLinksTask.Companion.registerIssueCheckerTask
import com.project.starter.quality.tasks.ProjectCodeStyleTask.Companion.addProjectCodeStyleTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class QualityPlugin : Plugin<Project> {

    private val SourceSet.kotlin
        get() = ((getConvention("kotlin") ?: getConvention("kotlin2js")) as? KotlinSourceSet)?.kotlin

    override fun apply(project: Project) = with(project) {
        repositories.mavenCentral()
        addProjectCodeStyleTask()
        configureKtlint()
        configureDetekt()
        configureCheckstyle()
        configureIssueCheckerTask()
        configureFormatOnRecompile()
    }

    private fun Project.configureIssueCheckerTask() {
        registerIssueCheckerTask {
            onAndroid {
                sourceSets.configureEach { sourceSet ->
                    source += sourceSet.java.srcDirs
                        .map { dir -> project.fileTree(dir) }
                        .reduce { merged: FileTree, tree: FileTree -> merged + tree }
                }
            }
            onMultiplatform {
                sourceSets.configureEach { sourceSet ->
                    source += sourceSet.kotlin.sourceDirectories.asFileTree
                }
            }
            onJvm {
                sourceSets.configureEach { sourceSet ->
                    source += sourceSet.java
                    val kotlin = sourceSet.kotlin ?: return@configureEach
                    source += kotlin.sourceDirectories.asFileTree
                }
            }
            report.set(buildDir.resolve("reports/issue_comments.txt"))
            githubToken.set(provider<String?> { properties["GITHUB_TOKEN"]?.toString() })
        }
    }

    private fun Project.configureFormatOnRecompile() {
        pluginManager.withPlugin("kotlin") {
            tasks.named("compileKotlin") {
                if (rootConfig.quality.formatOnCompile) {
                    it.dependsOn("$path:formatKotlin")
                }
            }
        }
        pluginManager.withPlugin("com.android.library") {
            tasks.named("preBuild") {
                if (rootConfig.quality.formatOnCompile) {
                    it.dependsOn("$path:formatKotlin")
                }
            }
        }
        pluginManager.withPlugin("com.android.application") {
            tasks.named("preBuild") {
                if (rootConfig.quality.formatOnCompile) {
                    it.dependsOn("$path:formatKotlin")
                }
            }
        }
    }

    private fun SourceSet.getConvention(name: String) =
        (this as HasConvention).convention.plugins[name]
}

internal inline fun Project.onAndroid(crossinline function: BaseExtension.() -> Unit) {
    project.extensions.findByName("android")?.let { (it as? BaseExtension)?.function() }
}

internal inline fun Project.onMultiplatform(crossinline function: KotlinMultiplatformExtension.() -> Unit) {
    project.extensions.findByName("kotlin")?.let { (it as? KotlinMultiplatformExtension)?.function() }
}

internal inline fun Project.onJvm(crossinline function: JavaPluginConvention.() -> Unit) {
    project.convention.findPlugin(JavaPluginConvention::class.java)?.let(function)
}
