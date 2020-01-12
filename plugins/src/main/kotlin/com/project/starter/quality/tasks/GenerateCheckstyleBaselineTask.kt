package com.project.starter.quality.tasks

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Node
import org.w3c.dom.NodeList

internal open class GenerateCheckstyleBaselineTask : DefaultTask() {

    init {
        project.tasks.withType(Checkstyle::class.java).forEach {
            it.isIgnoreFailures = true
            dependsOn(it)
        }
    }

    @TaskAction
    fun run() {
        val violations = project.extensions.getByType(CheckstyleExtension::class.java).reportsDir.listFiles().orEmpty()
        val all = violations.flatMap { readViolationFile(it) }

        val baseline = project.file("checkstyle-baseline.xml")
        if (all.isEmpty()) {
            baseline.delete()
        } else {
            baseline.writeText("""
            |<?xml version="1.0"?>
            |<!DOCTYPE suppressions PUBLIC
            |    "-//Checkstyle//DTD SuppressionFilter Configuration 1.2//EN"
            |    "https://checkstyle.org/dtds/suppressions_1_2.dtd">
            |<suppressions>
            |${all.joinToString(separator = "") { "\t$it\n" }}
            |</suppressions>
            |
        """.trimMargin())
        }
    }

    private fun readViolationFile(source: File): List<String> {
        val factory = DocumentBuilderFactory.newInstance()
        val documentBuilder = factory.newDocumentBuilder()
        val parsed = documentBuilder.parse(source)
        val files = parsed.getElementsByTagName("file").asSequence()
        val withErrors = files.filter { node ->
            node.childNodes.asSequence().any { it.hasAttributes() }
        }
        return withErrors.flatMap { handleNode(it) }.toList()
    }

    private fun handleNode(node: Node): Sequence<String> {
        val file = node.attributes.getNamedItem("name").nodeValue.substringAfterLast("/")
        val errors = node.childNodes.asSequence().filter { it.hasAttributes() }

        return errors.map {
            val check = it.attributes.getNamedItem("source").nodeValue.substringAfterLast(".")
            val line = it.attributes.getNamedItem("line").nodeValue.toInt()
            "<suppress checks=\"$check\" files=\"$file\" lines=\"$line\"/>"
        }
    }

    private fun NodeList.asSequence() =
        sequence {
            (0 until length).forEach {
                yield(item(it))
            }
        }

    private data class Violation(
        val issue: String,
        val file: String,
        val line: String
    )

    companion object {

        const val TASK_NAME = "generateCheckstyleBaseline"

        fun Project.addGenerateCheckstyleBaselineTask(action: (GenerateCheckstyleBaselineTask) -> Unit = {}) {
            tasks.register(TASK_NAME, GenerateCheckstyleBaselineTask::class.java, action)
        }
    }
}
