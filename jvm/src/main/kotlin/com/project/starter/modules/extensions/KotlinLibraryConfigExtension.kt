package com.project.starter.modules.extensions

import com.project.starter.quality.extensions.JavaSourcesAware

open class KotlinLibraryConfigExtension(
    override var javaFilesAllowed: Boolean? = null,
    val useKapt: Boolean? = null,
) : JavaSourcesAware
