package com.starter.quality.internal

import com.starter.quality.tasks.ProjectCodeStyleTask
import org.gradle.api.Project
import org.jmailen.gradle.kotlinter.KotlinterExtension
import org.jmailen.gradle.kotlinter.KotlinterPlugin
import org.jmailen.gradle.kotlinter.tasks.LintTask

internal fun Project.configureKtlint() {
    pluginManager.apply(KotlinterPlugin::class.java)

    extensions.configure(KotlinterExtension::class.java) {
        it.ignoreFailures = false
        it.indentSize = 4
        it.continuationIndentSize = 4
        it.experimentalRules = true
        it.reporters = arrayOf("checkstyle", "plain")
        it.disabledRules = arrayOf("import-ordering")
    }

    tasks.whenTaskAdded { task ->
        if (task is LintTask) {
            tasks.named(ProjectCodeStyleTask.TASK_NAME) {
                it.dependsOn(task)
            }
        }
    }
}
