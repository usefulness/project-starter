package com.project.starter.quality.internal

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

internal fun Project.configureAndroidCoverage(variants: DomainObjectSet<out BaseVariant>, coverageExclusions: List<String>) {

    variants.all {
        tasks.register("jacoco${it.name.capitalize()}TestReport")
    }
//    pluginManager.apply("jacoco-android")
//    extensions.getByType(JacocoPluginExtension::class.java).apply {
//        toolVersion = "0.8.4"
//    }
//    extensions.getByType(JacocoAndroidUnitTestReportExtension::class.java).apply {
//        html.enabled(true)
//        xml.enabled(true)
//        excludes = excludes + coverageExclusions + listOf(
//            "**/*_Factory.*",
//            "**/*Component.*",
//            "**/*Module.*",
//            "**/IssuesRegistry.*"
//        )
//    }
//    tasks.withType(Test::class.java) {
//        it.extensions.getByType(JacocoTaskExtension::class.java).apply {
//            isIncludeNoLocationClasses = true
//        }
//    }
}
