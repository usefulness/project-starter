package com.project.starter.modules.internal

import com.android.build.gradle.internal.dsl.LintOptions
import org.gradle.api.Project

internal fun Project.configureAndroidLint(lintOptions: LintOptions) {
    lintOptions.enable("UnknownNullness", "KotlinPropertyAccess", "LambdaLast", "NoHardKeywords")
    lintOptions.disable("ObsoleteLintCustomCheck", "UseSparseArrays")

    lintOptions.severityOverrides.putAll(
        mapOf(
            "UnknownNullness" to LintOptions.SEVERITY_ERROR,
            "KotlinPropertyAccess" to LintOptions.SEVERITY_ERROR,
            "LambdaLast" to LintOptions.SEVERITY_ERROR,
            "NoHardKeywords" to LintOptions.SEVERITY_ERROR,
        )
    )

    val baseline = file("lint-baseline.xml")
    if (baseline.exists() || hasProperty("refreshBaseline")) {
        lintOptions.baselineFile = baseline
    }
}
