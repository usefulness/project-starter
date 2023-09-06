package com.project.starter

// language=java
fun javaClass(className: String) = """
    package com.example;
    
    public class $className {
    
    }
    
""".trimIndent()

// language=kotlin
fun kotlinClass(className: String) = """
    package com.example
    
    object $className

""".trimIndent()

// language=kotlin
fun kotlinTestClass(className: String) = """
    package com.example
    
    class $className {
        @org.junit.Test
        fun test${className.lowercase()}() = Unit
    }

""".trimIndent()

// language=kotlin
fun kotlinMultiplatformTestClass(className: String) = """
    package com.example
    
    class $className {
        @kotlin.test.Test
        fun test${className.lowercase()}() = Unit
    }

""".trimIndent()
