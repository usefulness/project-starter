package com.project.starter.quality.internal

import java.util.Properties

internal val versionProperties by lazy(::VersionProperties)

internal class VersionProperties : Properties() {
    init {
        load(this.javaClass.getResourceAsStream("/starter-quality-gradle-plugin.properties"))
    }

    fun ktlintVersion(): String = getProperty("ktlint_version")
}
