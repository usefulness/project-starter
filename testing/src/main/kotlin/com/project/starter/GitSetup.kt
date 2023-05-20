package com.project.starter

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_BRANCH_SECTION
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_KEY_MERGE
import org.eclipse.jgit.lib.Constants

fun WithGradleProjectTest.setupGit(): Git {
    val git = Git.init()
        .apply { setDirectory(rootDirectory) }
        .call()
    git.repository.config
        .apply {
            val branchName = "master"
            setString(CONFIG_BRANCH_SECTION, branchName, CONFIG_KEY_MERGE, Constants.R_HEADS + branchName)
        }
        .save()
    rootDirectory.resolve(".gitignore").writeText(
        """
        .gradle/
        **/build/
        
        # Due jacoco-testkit integration
        gradle.properties
        """.trimIndent(),
    )
    git.commit("init")

    return git
}

fun Git.checkout(refName: String) {
    checkout()
        .apply { setName(refName) }
        .call()
}

fun Git.commit(commitMessage: String) {
    repository.directory.resolve("File.txt").appendText(
        """
            | Text
        """.trimMargin(),
    )
    add()
        .apply { addFilepattern(".") }
        .call()
    commit()
        .apply {
            setAllowEmpty(true)
            setSign(false)
            message = commitMessage
        }
        .call()
}

fun Git.tag(tagName: String) {
    tag()
        .apply {
            name = tagName
            isAnnotated = false
            isSigned = false
        }
        .call()
}
