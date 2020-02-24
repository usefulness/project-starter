package com.project.starter.versioning.plugins

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.project.starter.modules.internal.withExtension
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
import org.eclipse.jgit.api.Git
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.ReleasePlugin
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHookAction

class VersioningPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        if (this != rootProject) throw GradleException("Versioning plugin can be applied to the root project only")
        pluginManager.apply(ReleasePlugin::class.java)

        val scmConfig = extensions.getByType(VersionConfig::class.java).apply {
            versionIncrementer = PredefinedVersionIncrementer.versionIncrementerFor("incrementMinorIfNotOnRelease")
            hooks.apply {
                preReleaseHooks.add(
                    ReleaseHookAction { context ->
                        Git.open(repository.directory).use {
                            val version = context.releaseVersion
                            val isNonPatchVersion = version.matches("^\\d+\\.\\d\\.0[-*]?$".toRegex())
                            if (isNonPatchVersion) {
                                it.branchCreate().apply {
                                    setUpstreamMode(SetupUpstreamMode.TRACK)
                                    setName("release/$version")
                                }.call()
                            }
                        }
                    }
                )

                postReleaseHooks.add(
                    ReleaseHookAction {
                        try {
                            Git.open(repository.directory)
                                .push()
                                .setPushAll()
                                .setPushTags()
                                .setRemote(repository.remote)
                                .call()
                        } catch (@Suppress("TooGenericExceptionCaught") error: Throwable) {
                            logger.error("Couldn't push. Run `git push --tags --all` manually.", error)
                        }
                    }
                )
            }
        }

        withExtension<VersionConfig> {
            allprojects { project ->
                project.version = scmConfig.version
            }
        }

        allprojects {
            it.setupAndroidVersioning(scmConfig)
        }
    }

    private fun Project.setupAndroidVersioning(scmConfig: VersionConfig) {
        val configureVersion: BaseExtension.(String) -> Unit = { version ->
            val minor = version.split(".")[0].toInt()
            val major = version.split(".")[1].toInt()
            val patch = version.split(".")[2].toInt()
            defaultConfig.versionCode = minor * MINOR_MULTIPLIER + major * MAJOR_MULTIPLIER + patch
            defaultConfig.versionName = "$minor.$major.$patch"
        }
        pluginManager.withPlugin("com.android.library") {
            withExtension<LibraryExtension> {
                it.configureVersion(scmConfig.undecoratedVersion)
            }
        }
        pluginManager.withPlugin("com.android.application") {
            withExtension<AppExtension> {
                it.configureVersion(scmConfig.undecoratedVersion)
            }
        }
    }

    companion object {
        private const val MINOR_MULTIPLIER = 1_000_000
        private const val MAJOR_MULTIPLIER = 1_000
    }
}
