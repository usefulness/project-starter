package com.project.starter

import java.io.File
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_BRANCH_SECTION
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_KEY_MERGE
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_KEY_REMOTE
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.transport.URIish

internal fun WithGradleProjectTest.setupGit(origin: File): Git {
    Git.init().setDirectory(origin).call()
    val git = Git.init().apply {
        setDirectory(rootDirectory)
    }.call()
    git.remoteAdd().apply {
        setName("origin")
        setUri(URIish(origin.toURI().toURL()))
    }.call()
    git.repository.config.apply {
        val branchName = "master"
        val remoteName = "origin"
        setString(CONFIG_BRANCH_SECTION, branchName, CONFIG_KEY_REMOTE, remoteName)
        setString(CONFIG_BRANCH_SECTION, branchName, CONFIG_KEY_MERGE, Constants.R_HEADS + branchName)
    }.save()
    rootDirectory.resolve(".gitignore").writeText(
        """
        .gradle
        **/build/
        """.trimIndent()
    )
    git.commit("init")
    git.push().apply {
        remote = "origin"
        setPushTags()
        setPushAll()
    }.call()

    return git
}

internal fun Git.checkout(branchName: String) {
    checkout().apply {
        setName(branchName)
    }.call()
    repository.config.apply {
        val remoteName = "origin"
        setString(CONFIG_BRANCH_SECTION, branchName, CONFIG_KEY_REMOTE, remoteName)
        setString(CONFIG_BRANCH_SECTION, branchName, CONFIG_KEY_MERGE, Constants.R_HEADS + branchName)
    }.save()
}

internal fun Git.commit(commitMessage: String) {
    repository.directory.resolve("File.txt").appendText(
        """
            | Text
            """.trimMargin()
    )
    add().apply {
        addFilepattern(".")
    }.call()
    commit().apply {
        setAll(true)
        setSign(false)
        message = commitMessage
    }.call()
}

internal fun Git.tag(tagName: String) {
    tag().apply {
        name = tagName
        isAnnotated = false
        isSigned = false
    }.call()
}
