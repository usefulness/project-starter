package com.project.starter.modules.internal

import com.android.build.api.dsl.Lint
import org.gradle.api.Project

internal fun Project.configureAndroidLint(lintOptions: Lint) {
    val additionalErrors = setOf("UnknownNullness", "KotlinPropertyAccess", "LambdaLast", "NoHardKeywords")
    lintOptions.enable += additionalErrors
    lintOptions.error += additionalErrors

    lintOptions.disable += setOf("ObsoleteLintCustomCheck", "UseSparseArrays")

    val baseline = file("lint-baseline.xml")
    if (baseline.exists() || hasProperty("refreshBaseline")) {
        lintOptions.baseline = baseline
    }
}
