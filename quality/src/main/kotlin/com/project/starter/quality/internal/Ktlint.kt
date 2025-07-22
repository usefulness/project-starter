package com.project.starter.quality.internal

import com.project.starter.quality.tasks.ProjectCodeStyleTask
import io.github.usefulness.KtlintGradleExtension
import io.github.usefulness.KtlintGradlePlugin
import org.gradle.api.Project

internal fun Project.configureKtlint() {
    pluginManager.apply(KtlintGradlePlugin::class.java)

    extensions.configure(KtlintGradleExtension::class.java) {
        experimentalRules.convention(true)
        disabledRules.convention(
            disabledRules.get() +
                listOf(
                    "import-ordering",
                    "filename",
                    "experimental:function-signature",
                    "experimental:property-naming",
                ),
        )
        ktlintVersion.convention(versionProperties.ktlintVersion())
    }

    tasks.named(ProjectCodeStyleTask.TASK_NAME) {
        dependsOn("lintKotlin")
    }
}
