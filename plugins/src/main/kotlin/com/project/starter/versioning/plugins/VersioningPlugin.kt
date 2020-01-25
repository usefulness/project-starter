package com.project.starter.versioning.plugins

import org.eclipse.jgit.api.Git
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.ReleasePlugin
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHookAction

class VersioningPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        if (this != rootProject) throw GradleException("Versioning plugin can be applied to the root project only")
        pluginManager.apply(ReleasePlugin::class.java)

        val scmConfig = extensions.getByType(VersionConfig::class.java).apply {
            tag.apply {
                versionSeparator = "/"
                prefix = "release"
            }
            hooks.apply {
                preReleaseHooks.add(
                    ReleaseHookAction { context ->
                        Git.open(repository.directory).use {
                            val version = context.releaseVersion
                            val isNonPatchVersion = version.matches("^\\d+\\.\\d\\.0[-*]?$".toRegex())
                            if (isNonPatchVersion) {
                                it.branchCreate().setName("release/$version").call()
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
                        } catch (error: Throwable) {
                            logger.error("Couldn't push. Run `git push --tags --all` manually.", error)
                        }
                    }
                )
            }
        }

        allprojects {
            it.version = scmConfig.version
        }
    }
}
