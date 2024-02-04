package com.project.starter.config

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

inline fun <reified T> ExtensionContainer.getByType(): T = getByType(T::class.java)

inline fun <reified T> ExtensionContainer.findByType() = findByType(T::class.java)

inline fun <reified T> Project.withExtension(crossinline action: Project.(T) -> Unit) = afterEvaluate {
    action(extensions.getByType())
}
