package com.starter.issuechecker

sealed class CheckResult {

    data class Success(
        val issueUrl: String,
        val issueStatus: IssueStatus
    ) : CheckResult()

    data class Error(
        val issueUrl: String,
        val throwable: Throwable
    ) : CheckResult()
}

enum class IssueStatus {
    Open,
    Closed
}
