package com.project.starter

import org.intellij.lang.annotations.Language

@Language("java")
fun javaClass(className: String) =
    """
    public class $className {
    
    }
        
    """.trimIndent()

@Language("kotlin")
fun kotlinClass(className: String) =
    """
    object $className

    """.trimIndent()

@Language("kotlin")
fun kotlinTestClass(className: String) =
    """
    class $className {
    
        @org.junit.Test
        fun test${className.toLowerCase()}() = Unit
    }

    """.trimIndent()
