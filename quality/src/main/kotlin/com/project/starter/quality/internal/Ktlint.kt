package com.project.starter.quality.internal

import com.project.starter.quality.tasks.ProjectCodeStyleTask
import io.github.usefulness.KtlintGradleExtension
import io.github.usefulness.KtlintGradlePlugin
import org.gradle.api.Project

internal fun Project.configureKtlint() {
    pluginManager.apply(KtlintGradlePlugin::class.java)

    extensions.configure(KtlintGradleExtension::class.java) {
        it.experimentalRules.set(true)
        it.disabledRules.set(
            it.disabledRules.get() + listOf(
                "import-ordering",
                "filename",
                "experimental:function-signature",
                "experimental:property-naming",
            )
        )
    }

    tasks.named(ProjectCodeStyleTask.TASK_NAME) {
        it.dependsOn("$path:lintKotlin")
    }
}
