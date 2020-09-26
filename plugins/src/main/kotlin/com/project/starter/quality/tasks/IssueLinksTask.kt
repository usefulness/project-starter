@file:Suppress("UnstableApiUsage")

package com.project.starter.quality.tasks

import com.starter.issuechecker.CheckResult
import com.starter.issuechecker.IssueChecker
import com.starter.issuechecker.IssueStatus
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
abstract class IssueLinksTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : SourceTask() {

    @OutputFile
    val report: RegularFileProperty = project.objects.fileProperty()

    @Input
    @Optional
    val githubToken: Property<String> = project.objects.property(String::class.java)

    init {
        description = "Generates report for issue links in code comments"
        group = "quality"
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    override fun getSource(): FileTree = super.getSource()

    @TaskAction
    fun run() {
        LoggingContext.logger = logger
        source.forEach { chunk ->
            workerExecutor.noIsolation().submit(IssueCheckAction::class.java) {
                it.files.from(chunk)
                it.reportFile.set(report.get())
                it.githubToken.set(githubToken.orNull)
            }
        }

        workerExecutor.await()
    }

    companion object {

        private const val TASK_NAME = "issueLinksReport"

        fun Project.registerIssueCheckerTask(action: IssueLinksTask.() -> Unit = {}) =
            tasks.register(TASK_NAME, IssueLinksTask::class.java, action)
    }
}

interface IssueCheckParameters : WorkParameters {
    val files: ConfigurableFileCollection
    val reportFile: RegularFileProperty
    val githubToken: Property<String>
}

abstract class IssueCheckAction : WorkAction<IssueCheckParameters> {

    override fun execute() {
        val issueChecker = IssueChecker(config = IssueChecker.Config(githubToken = parameters.githubToken.orNull))
        val output = parameters.reportFile.get().asFile
        output.writeText("")
        for (file in parameters.files) {
            val message = issueChecker.reportBlocking(file.readText()).map { result ->
                when (result) {
                    is CheckResult.Success -> when (result.issueStatus) {
                        IssueStatus.Open -> "✅ ${result.issueUrl} (Opened)"
                        IssueStatus.Closed -> "👉 ${result.issueUrl} (Closed)"
                    }
                    is CheckResult.Error -> "❗ ${result.issueUrl} -> error: ${result.throwable.message}"
                }
            }

            output.appendText(message.joinToString(separator = "\n"))
            LoggingContext.logger.info("Found ${message.size} issues in ${file.path}")
            message.forEach {
                LoggingContext.logger.quiet(it)
            }
        }
    }
}

object LoggingContext {
    lateinit var logger: Logger
}
