package com.project.starter

import org.intellij.lang.annotations.Language

@Language("java")
internal fun javaClass(className: String) =
    """
        public class $className {
        
        }
            
        """.trimIndent()
