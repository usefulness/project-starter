package com.project.starter

import org.intellij.lang.annotations.Language

@Language("java")
internal fun javaClass(className: String) =
    """
    public class $className {
    
    }
        
    """.trimIndent()

@Language("kotlin")
internal fun kotlinClass(className: String) =
    """
    object $className

    """.trimIndent()

@Language("kotlin")
internal fun kotlinTestClass(className: String) =
    """
    class $className {
    
        @org.junit.Test
        fun ${className.toLowerCase()}() = Unit
    }

    """.trimIndent()
