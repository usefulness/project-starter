package com.project.starter.quality.plugins

import com.android.build.api.variant.AndroidComponentsExtension
import com.project.starter.config.findByType
import com.project.starter.config.plugins.rootConfig
import com.project.starter.quality.internal.configureDetekt
import com.project.starter.quality.internal.configureKtlint
import com.project.starter.quality.tasks.IssueLinksTask.Companion.registerIssueCheckerTask
import com.project.starter.quality.tasks.ProjectCodeStyleTask.Companion.addProjectCodeStyleTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

class QualityPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        runCatching { repositories.mavenCentral() }
        addProjectCodeStyleTask()
        configureKtlint()
        configureDetekt()
        configureIssueCheckerTask()
        configureFormatOnRecompile()
    }

    private fun Project.configureIssueCheckerTask() {
        val issueCheckerTask = registerIssueCheckerTask {
            onMultiplatform {
                sourceSets.configureEach { sourceSet ->
                    source += sourceSet.kotlin.sourceDirectories.asFileTree
                }
            }
            onJvm {
                this.configureEach { sourceSet ->
                    source += sourceSet.allSource
                }
            }
            report.set(layout.buildDirectory.map { it.file("reports/issue_comments.txt") })
            githubToken.set(provider<String?> { properties["GITHUB_TOKEN"]?.toString() })
        }
        onAndroid {
            onVariants { variant ->
                val variantSources = listOfNotNull(
                    variant.sources.kotlin,
                    variant.sources.java,
                )
                    .map { it.all }
                issueCheckerTask.configure { it.source(variantSources) }
            }
        }
    }

    private fun Project.configureFormatOnRecompile() {
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
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
}

internal inline fun Project.onAndroid(crossinline function: AndroidComponentsExtension<*, *, *>.() -> Unit) {
    project.extensions.findByName("androidComponents")?.let { (it as? AndroidComponentsExtension<*, *, *>)?.function() }
}

internal inline fun Project.onMultiplatform(crossinline function: KotlinSourceSetContainer.() -> Unit) {
    project.extensions.findByName("kotlin")?.let { (it as? KotlinSourceSetContainer)?.function() }
}

internal inline fun Project.onJvm(crossinline function: SourceSetContainer.() -> Unit) {
    project.extensions.findByType<SourceSetContainer>()?.let(function)
}
