package com.starter.issuechecker.resolvers.github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.starter.issuechecker.IssueStatus
import com.starter.issuechecker.resolvers.StatusResolver
import retrofit2.http.GET
import retrofit2.http.Path
import java.net.URL

internal class GithubStatusResolver(
    private val service: GithubService
) : StatusResolver {

    override suspend fun resolve(url: URL): IssueStatus {
        val result = handledPattern.find(url.toString())?.groups ?: throw IllegalArgumentException("Couldn't parse $url")
        val owner = result[OWNER]?.value ?: throw IllegalArgumentException("Couldn't get owner from $url")
        val repo = result[REPO]?.value ?: throw IllegalArgumentException("Couldn't get repo from $url")
        val issueId = result[ISSUE_ID]?.value ?: throw IllegalArgumentException("Couldn't get issueId from $url")
        val status = service.getIssue(owner, repo, issueId)

        return if (status.closedAt == null) {
            IssueStatus.Open
        } else {
            IssueStatus.Closed
        }
    }

    override fun handles(url: URL): Boolean =
        handledPattern.containsMatchIn(url.toString())

    companion object {

        private const val OWNER = 2
        private const val REPO = 3
        private const val ISSUE_ID = 5

        private val handledPattern by lazy {
            "https?://(www.)?github.com/([^/]+)/([^/]+)/(issues|pull)/([^/]+)/?".toRegex()
        }
    }
}

internal interface GithubService {
    @GET("repos/{owner}/{repo}/issues/{issueId}")
    suspend fun getIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issueId") issueId: String
    ): GithubIssue
}

@JsonClass(generateAdapter = true)
internal data class GithubIssue(
    @Json(name = "title") val title: String,
    @Json(name = "closed_at") val closedAt: String?
)
