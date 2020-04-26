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

    @Suppress("MagicNumber")
    override suspend fun resolve(url: URL): IssueStatus {
        val result = pattern.find(url.toString())?.groups ?: throw IllegalArgumentException("Couldn't parse $url")
        val owner = result[1]?.value ?: throw IllegalArgumentException("Couldn't get owner from $url")
        val repo = result[2]?.value ?: throw IllegalArgumentException("Couldn't get repo from$url")
        val issueId = result[3]?.value ?: throw IllegalArgumentException("Couldn't get issueId from $url")
        val status = service.getIssue(owner, repo, issueId)

        return if (status.closedAt == null) {
            IssueStatus.Open
        } else {
            IssueStatus.Closed
        }
    }

    companion object {

        private val pattern by lazy {
            "github.com/([^/]*)/([^/]*)/issues/([^/]*)/?".toRegex()
        }
    }
}

interface GithubService {
    @GET("repos/{owner}/{repo}/issues/{issueId}")
    suspend fun getIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issueId") issueId: String
    ): GithubIssue
}

@JsonClass(generateAdapter = true)
data class GithubIssue(
    @Json(name = "title") val title: String,
    @Json(name = "closed_at") val closedAt: String?
)
