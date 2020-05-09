package com.starter.issuechecker

internal fun Any.readJson(name: String): String =
    this::class.java.classLoader.getResource(name)?.readText().orEmpty()
