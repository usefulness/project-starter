package com.project.starter.modules.extensions

import com.project.starter.quality.extensions.JavaSourcesAware

abstract class AndroidExtension : JavaSourcesAware {
    override var javaFilesAllowed: Boolean? = null
    var coverageExclusions: List<String> = emptyList()
}

open class AndroidLibraryConfigExtension : AndroidExtension()

open class AndroidApplicationConfigExtension : AndroidExtension()
