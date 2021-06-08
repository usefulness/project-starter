package com.project.starter.versioning.plugins

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.project.starter.config.getByType
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.ReleasePlugin
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer
import pl.allegro.tech.build.axion.release.domain.VersionConfig

class VersioningPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        if (this != rootProject) throw GradleException("Versioning plugin can be applied to the root project only")
        pluginManager.apply(ReleasePlugin::class.java)

        val scmConfig = extensions.getByType<VersionConfig>().apply {
            versionIncrementer = PredefinedVersionIncrementer.versionIncrementerFor(
                "incrementMinorIfNotOnRelease",
                mapOf("releaseBranchPattern" to "^release/.*\$"),
            )
        }

        allprojects { project ->
            project.version = scmConfig.version
        }
        allprojects { project ->
            project.setupAndroidVersioning(scmConfig)
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
            extensions.getByType<LibraryExtension>().configureVersion(scmConfig.undecoratedVersion)
        }
        pluginManager.withPlugin("com.android.application") {
            extensions.getByType<AppExtension>().configureVersion(scmConfig.undecoratedVersion)
        }
    }

    companion object {
        private const val MINOR_MULTIPLIER = 1_000_000
        private const val MAJOR_MULTIPLIER = 1_000
    }
}
