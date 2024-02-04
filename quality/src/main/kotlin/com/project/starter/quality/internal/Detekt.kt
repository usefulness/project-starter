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

    extensions.configure(DetektExtension::class.java) {
        onMultiplatform {
            sourceSets.configureEach {
                source.from(kotlin.srcDirs)
            }
        }

        config.setFrom(loadFromResources("detekt-config.yml"))
    }
    tasks.named("detekt", Detekt::class.java) {
        exclude(".*/resources/.*", ".*/build/.*")
    }
    tasks.named(ProjectCodeStyleTask.TASK_NAME) {
        dependsOn("detekt")
    }
    tasks.withType(Detekt::class.java).configureEach {
        jvmTarget = rootConfig.javaVersion.toString()
        reports {
            html.required.set(false)
            xml.required.set(false)
            txt.required.set(false)
        }
    }
}
