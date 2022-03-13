package com.project.starter.quality.internal

import com.project.starter.config.plugins.rootConfig
import com.project.starter.quality.plugins.onMultiplatform
import com.project.starter.quality.tasks.ProjectCodeStyleTask
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project

internal fun Project.configureDetekt() {
    pluginManager.apply(DetektPlugin::class.java)

    extensions.configure(DetektExtension::class.java) { detekt ->
        onMultiplatform {
            sourceSets.configureEach {
                detekt.source.from(it.kotlin.srcDirs)
            }
        }

        detekt.buildUponDefaultConfig = true
    }
    tasks.named("detekt", Detekt::class.java) {
        it.exclude(".*/resources/.*", ".*/build/.*")
    }
    tasks.named(ProjectCodeStyleTask.TASK_NAME) {
        it.dependsOn("$path:detekt")
    }
    tasks.withType(Detekt::class.java).configureEach {
        it.jvmTarget = rootConfig.javaVersion.toString()
        it.reports.apply {
            html.required.set(false)
            xml.required.set(false)
            txt.required.set(false)
        }
    }
}
