package com.project.starter.config

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

inline fun <reified T : Any> ExtensionContainer.getByType(): T = getByType(T::class.java)

inline fun <reified T : Any> ExtensionContainer.findByType() = findByType(T::class.java)

inline fun <reified T : Any> Project.withExtension(crossinline action: Project.(T) -> Unit) = afterEvaluate {
    action(extensions.getByType())
}
