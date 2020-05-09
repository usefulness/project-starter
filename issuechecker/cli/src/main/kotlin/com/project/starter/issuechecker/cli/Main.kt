@file:JvmName("Main")

package com.project.starter.issuechecker.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import com.starter.issuechecker.CheckResult
import com.starter.issuechecker.IssueChecker
import com.starter.issuechecker.IssueStatus
import java.io.File

fun main(args: Array<String>) {
    IssueCheckerCli().main(args)
}

class IssueCheckerCli : CliktCommand() {
    val source by option("--source", "-s", help = "Source files").file(mustExist = true).required()
    val githubToken by option(help = "Github token to check private issues")
    val debug by option("--debug", "-d", help = "Enabled additional logging").flag()
    val stacktrace by option("--stacktrace", help = "Shows additional stacktrace in case of failure").flag()

    val checker by lazy {
        IssueChecker(
            config = IssueChecker.Config(
                githubToken = githubToken
            )
        )
    }

    override fun run() {
        if (source.isDirectory) {
            source.walkTopDown().filter { it.isFile }.forEach {
                checkFile(it)
            }
        } else {
            checkFile(source)
        }
        if (debug) {
            println("Done")
        }
    }

    private fun checkFile(source: File) {
        if (debug) {
            println("Checking file ${source.path}")
        }
        checker.reportBlocking(source.readText()).forEach { result ->
            when (result) {
                is CheckResult.Success -> {
                    val message = when (result.issueStatus) {
                        IssueStatus.Open -> "✅ ${result.issueUrl} (Opened)"
                        IssueStatus.Closed -> "👉 ${result.issueUrl} (Closed)"
                    }
                    println(message)
                }
                is CheckResult.Error -> {
                    if (debug || stacktrace) {
                        result.throwable.printStackTrace()
                    }
                    System.err.println("Couldn't check url ${result.issueUrl}")
                }
            }.let { }
        }
    }
}
