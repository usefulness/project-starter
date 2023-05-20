package com.project.starter.versioning.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.project.starter.config.getByType
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import javax.inject.Inject

class VersioningPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        if (this != rootProject) throw GradleException("Versioning plugin can be applied to the root project only")

        val gitVersionProvider = providers.of(GitVersionValueSource::class.java) {}

        allprojects { project ->
            val get = gitVersionProvider.get()
            project.version = get.decorated
            project.setupAndroidVersioning(gitVersionProvider)
        }

        tasks.register("currentVersion", CurrentVersionTask::class.java) { task ->
            task.gitVersion.set(gitVersionProvider)
        }
    }

    private fun Project.setupAndroidVersioning(gitVersionProvider: Provider<GitVersion>) {
        pluginManager.withPlugin("com.android.application") {
            extensions.getByType<ApplicationExtension>().defaultConfig {
                val gitVersion = gitVersionProvider.get()
                val major = gitVersion.major
                val minor = gitVersion.minor
                val patch = gitVersion.patch

                versionCode = major * MAJOR_MULTIPLIER + minor * MINOR_MULTIPLIER + patch
                versionName = gitVersion.undecorated
            }
        }
    }

    abstract class GitVersionValueSource @Inject constructor(
        private val execOperations: ExecOperations,
    ) : ValueSource<GitVersion, ValueSourceParameters.None> {

        override fun obtain(): GitVersion {
            fun defaultVersion() = GitVersion(
                major = 0,
                minor = 1,
                patch = 0,
                isSnapshot = true,
            )

            val status = runGit("status", "--porcelain")
            val currentTag = runGit("tag", "--points-at").takeIf(String::isNotBlank)?.split("\n")?.maxOrNull()

            val lastTag = if (currentTag == null) {
                val lastGitTag = runGit("describe", "--tags", "--abbrev=0")
                val lastReleaseCommit = runGit("rev-parse", lastGitTag)
                val lastTags = runGit("tag", "--contains", lastReleaseCommit).split("\n")
                lastTags.maxOrNull()
            } else {
                currentTag
            } ?: return defaultVersion()

            val isOnTag = currentTag != null
            val isDirty = status.isNotBlank()

            val isSnapshot = !isOnTag || isDirty

            val versionRegex = "([0-9]+)\\.([0-9]+)\\.([0-9]+)".toRegex()
            val result = versionRegex.find(lastTag) ?: return defaultVersion()

            val major = result.groups[1]?.value?.toIntOrNull() ?: return defaultVersion()
            val minor = result.groups[2]?.value?.toIntOrNull() ?: return defaultVersion()
            val patch = result.groups[3]?.value?.toIntOrNull() ?: return defaultVersion()

            return GitVersion(
                major = major,
                minor = if (isSnapshot) minor + 1 else minor,
                patch = if (isSnapshot) 0 else patch,
                isSnapshot = isSnapshot,
            )
        }

        private fun runGit(vararg args: String) = ByteArrayOutputStream().use { output ->
            execOperations.exec {
                it.executable("git")
                it.args(args.toList())
                it.standardOutput = output
            }

            output.toString(Charset.defaultCharset()).trim()
        }
    }

    abstract class CurrentVersionTask @Inject constructor(
        objectFactory: ObjectFactory,
    ) : DefaultTask() {

        @Input
        val gitVersion = objectFactory.property(GitVersion::class.java)

        @TaskAction
        fun run() {
            logger.quiet("Current version: ${gitVersion.get().decorated}")
        }
    }

    data class GitVersion(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val isSnapshot: Boolean,
    ) {

        val undecorated = "$major.$minor.$patch"
        val decorated = "$major.$minor.$patch${if (isSnapshot) "-SNAPSHOT" else ""}"
    }

    companion object {
        private const val MAJOR_MULTIPLIER = 1_000_000
        private const val MINOR_MULTIPLIER = 1_000
    }
}
