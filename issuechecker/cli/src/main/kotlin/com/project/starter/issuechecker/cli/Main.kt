@file:JvmName("Main")

package com.project.starter.issuechecker.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import com.starter.issuechecker.CheckResult
import com.starter.issuechecker.IssueChecker
import com.starter.issuechecker.IssueStatus
import java.io.File
import java.nio.file.FileSystems

fun main(args: Array<String>) {
    IssueCheckerCli().main(args)
}

class IssueCheckerCli : CliktCommand() {
    private val source by option(
        "--src",
        "-s",
        "--source",
        help = "Source file filter, i.e. `--source **.java` to find all java files"
    ).default("**")
    private val githubToken by option(help = "Github token to check private issues")
    private val debug by option("--debug", "-d", help = "Enabled additional logging").flag()
    private val stacktrace by option("--stacktrace", help = "Shows additional stacktrace in case of failure").flag()
    private val dryRun by option("--dry-run", help = "Only finds all links, without checking them").flag()

    private val checker by lazy {
        IssueChecker(config = IssueChecker.Config(githubToken = githubToken))
    }

    override fun run() {
        val workingDir = File("").absoluteFile
        if (debug) {
            println("Working dir: $workingDir")
        }

        val pathMatcher = FileSystems.getDefault().getPathMatcher("glob:$source")
        workingDir.walkTopDown()
            .filter { it.isFile }
            .filter { pathMatcher.matches(it.toPath()) }
            .forEach {
                if (debug) {
                    println("Visiting file ${it.path}")
                }
                if (dryRun) {
                    findLinks(it)
                } else {
                    checkFile(it)
                }
            }

        if (debug) {
            println("Completed")
        }
    }

    private fun findLinks(file: File) {
        checker.findAllLinksBlocking(file.readText()).forEach { links ->
            println(file.path)
            links.forEach {
                println("-> $it")
            }
        }
    }

    private fun checkFile(source: File) {
        val messages = checker.reportBlocking(source.readText()).map { result ->
            when (result) {
                is CheckResult.Success -> {
                    when (result.issueStatus) {
                        IssueStatus.Open -> "✅ ${result.issueUrl} (Opened)"
                        IssueStatus.Closed -> "👉 ${result.issueUrl} (Closed)"
                    }.let { { println("-> $it") } }
                }
                is CheckResult.Error -> {
                    if (debug || stacktrace) {
                        result.throwable.printStackTrace()
                    }
                    "Couldn't check url ${result.issueUrl}".let { { System.err.println("-> $it") } }
                }
            }
        }
        if (messages.isNotEmpty()) {
            println(source.path)
            messages.forEach { it.invoke() }
        }
    }
}
