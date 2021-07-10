package com.project.starter

fun javaClass(className: String) =
    // language=java
    """
    public class $className {
    
    }
        
    """.trimIndent()

fun kotlinClass(className: String) =
    // language=kotlin
    """
    object $className

    """.trimIndent()

fun kotlinTestClass(className: String) =
    // language=kotlin
    """
    class $className {
    
        @org.junit.Test
        fun test${className.toLowerCase()}() = Unit
    }

    """.trimIndent()
