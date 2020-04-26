package com.project.starter.quality.plugins

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.project.starter.config.plugins.rootConfig
import com.project.starter.quality.internal.IssueLinksCheckerTask.Companion.registerIssueCheckerTask
import com.project.starter.quality.internal.configureCheckstyle
import com.project.starter.quality.internal.configureDetekt
import com.project.starter.quality.internal.configureKtlint
import com.project.starter.quality.tasks.ProjectCodeStyleTask.Companion.addProjectCodeStyleTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.plugins.JavaPluginConvention

class QualityPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        repositories.jcenter()
        addProjectCodeStyleTask()
        configureKtlint()
        configureDetekt(rootConfig)
        configureCheckstyle(rootConfig)
        configureIssueCheckerTask()

        val config = rootConfig.quality
        if (config.formatOnCompile) {
            applyFormatOnRecompile()
        }
    }

    private fun Project.configureIssueCheckerTask() {
        registerIssueCheckerTask {
            if (project.hasProperty("android")) {
                val extension = project.extensions.getByType(BaseExtension::class.java)
                extension.sourceSets.configureEach { sourceSet ->
                    source += sourceSet.java.srcDirs
                        .map { dir -> project.fileTree(dir) }
                        .reduce { merged: FileTree, tree: FileTree -> merged + tree }
                }
            } else {
                val plugin = project.convention.getPlugin(JavaPluginConvention::class.java)
                plugin.sourceSets.configureEach {
                    source += it.java
                }
            }
            report.set(buildDir.resolve("reports/issue_comments.txt"))
            githubToken.set(provider<String?> { properties["GITHUB_TOKEN"]?.toString() })
        }
    }

    private fun Project.applyFormatOnRecompile() {
        pluginManager.withPlugin("kotlin") {
            tasks.named("compileKotlin").dependsOn("$path:formatKotlin")
        }
        pluginManager.withPlugin("com.android.library") {
            tasks.named("preBuild").dependsOn("$path:formatKotlin")
        }
        pluginManager.withPlugin("com.android.application") {
            tasks.named("preBuild").dependsOn("$path:formatKotlin")
        }
    }
}
