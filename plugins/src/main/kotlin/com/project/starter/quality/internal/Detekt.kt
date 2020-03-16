package com.project.starter.quality.internal

import com.project.starter.config.extensions.RootConfigExtension
import com.project.starter.quality.tasks.ProjectCodeStyleTask
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project

internal fun Project.configureDetekt(config: RootConfigExtension) {
    pluginManager.apply(DetektPlugin::class.java)

    extensions.configure(DetektExtension::class.java) { detekt ->
        detekt.reports.apply {
            html.enabled = false
            xml.enabled = false
            txt.enabled = false
        }

        detekt.config.setFrom(loadFromResources("detekt-config.yml"))
    }
    tasks.named("detekt", Detekt::class.java) {
        it.exclude(".*/resources/.*", ".*/build/.*")
    }
    tasks.named(ProjectCodeStyleTask.TASK_NAME) {
        it.dependsOn("$path:detekt")
    }
    tasks.withType(Detekt::class.java) {
        it.jvmTarget = config.javaVersion.toString()
    }
}
