package com.project.starter.modules.extensions

open class AndroidLibraryConfigExtension(
    var javaFilesAllowed: Boolean? = null,
    var generateBuildConfig: Boolean = false,
    var defaultVariants: List<String> = emptyList(),
    var coverageExclusions: List<String> = emptyList()
)
