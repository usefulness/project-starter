package com.project.starter.quality.internal

import com.project.starter.quality.tasks.ProjectCodeStyleTask
import org.gradle.api.Project
import org.jmailen.gradle.kotlinter.KotlinterExtension
import org.jmailen.gradle.kotlinter.KotlinterPlugin

internal fun Project.configureKtlint() {
    pluginManager.apply(KotlinterPlugin::class.java)

    extensions.configure(KotlinterExtension::class.java) {
        it.experimentalRules = true
        it.reporters = emptyArray()
        it.disabledRules += arrayOf("import-ordering", "filename", "experimental:function-signature")
    }

    tasks.named(ProjectCodeStyleTask.TASK_NAME) {
        it.dependsOn("$path:lintKotlin")
    }
}
