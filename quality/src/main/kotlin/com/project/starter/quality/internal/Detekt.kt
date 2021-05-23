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
        detekt.reports.apply {
            html.enabled = false
            xml.enabled = false
            txt.enabled = false
        }

        onMultiplatform {
            sourceSets.configureEach {
                detekt.input.from(it.kotlin.srcDirs)
            }
        }

        detekt.config.setFrom(loadFromResources("detekt-config.yml"))
    }
    tasks.named("detekt", Detekt::class.java) {
        it.exclude(".*/resources/.*", ".*/build/.*")
    }
    tasks.named(ProjectCodeStyleTask.TASK_NAME) {
        it.dependsOn("$path:detekt")
    }
    tasks.withType(Detekt::class.java).configureEach {
        it.jvmTarget = rootConfig.javaVersion.toString()
    }
}
