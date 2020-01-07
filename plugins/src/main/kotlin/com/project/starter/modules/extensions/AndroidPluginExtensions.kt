package com.project.starter.modules.extensions

abstract class AndroidExtension {
    var javaFilesAllowed: Boolean? = null
    var defaultVariants: List<String> = emptyList()
    var coverageExclusions: List<String> = emptyList()
}

open class AndroidLibraryConfigExtension : AndroidExtension() {
    var generateBuildConfig: Boolean = false
}

open class AndroidApplicationConfigExtension : AndroidExtension()
