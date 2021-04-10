package com.project.starter.config

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer

inline fun <reified T> ExtensionContainer.getByType() =
    getByType(T::class.java)

inline fun <reified T> Project.withExtension(crossinline action: Project.(T) -> Unit) =
    afterEvaluate {
        it.action(it.extensions.getByType())
    }

inline fun <reified T> ObjectFactory.property(value: T?) = property(T::class.java).apply {
    value(value)
}
