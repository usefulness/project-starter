package com.project.starter

fun javaClass(className: String) =
    // language=java
    """
    package com.example;
    
    public class $className {
    
    }
        
    """.trimIndent()

fun kotlinClass(className: String) =
    // language=kotlin
    """
    package com.example

    object $className

    """.trimIndent()

fun kotlinTestClass(className: String) =
    // language=kotlin
    """
    package com.example

    class $className {
    
        @org.junit.Test
        fun test${className.lowercase()}() = Unit
    }

    """.trimIndent()

fun kotlinMultiplatformTestClass(className: String) =
    // language=kotlin
    """
    package com.example

    class $className {
    
        @kotlin.test.Test
        fun test${className.lowercase()}() = Unit
    }

    """.trimIndent()
