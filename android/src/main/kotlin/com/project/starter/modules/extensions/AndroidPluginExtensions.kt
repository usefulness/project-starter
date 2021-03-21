package com.project.starter.modules.extensions

import com.project.starter.quality.extensions.JavaSourcesAware

@Suppress("UnnecessaryAbstractClass")
abstract class AndroidExtension : JavaSourcesAware {
    override var javaFilesAllowed: Boolean? = null
    var defaultVariants: List<String> = emptyList()
    var coverageExclusions: List<String> = emptyList()
}

open class AndroidLibraryConfigExtension : AndroidExtension() {
    var generateBuildConfig: Boolean = false
}

open class AndroidApplicationConfigExtension : AndroidExtension()
