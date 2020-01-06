package com.project.starter.modules.internal

import org.gradle.api.artifacts.dsl.DependencyHandler

internal fun DependencyHandler.configureCommonDependencies() {
    add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
