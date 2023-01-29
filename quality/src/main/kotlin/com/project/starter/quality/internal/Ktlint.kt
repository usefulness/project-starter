package com.project.starter.quality.internal

import com.project.starter.quality.tasks.ProjectCodeStyleTask
import io.github.usefulness.KtlintGradleExtension
import io.github.usefulness.KtlintGradlePlugin
import org.gradle.api.Project

internal fun Project.configureKtlint() {
    pluginManager.apply(KtlintGradlePlugin::class.java)

    extensions.configure(KtlintGradleExtension::class.java) {
        it.experimentalRules = true
        it.disabledRules += arrayOf("import-ordering", "filename", "experimental:function-signature")
    }

    tasks.named(ProjectCodeStyleTask.TASK_NAME) {
        it.dependsOn("$path:lintKotlin")
    }
}
