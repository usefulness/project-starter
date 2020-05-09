package com.starter.issuechecker.resolvers.youtrack

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.starter.issuechecker.IssueStatus
import com.starter.issuechecker.resolvers.StatusResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.Path
import java.net.URL

internal class YoutrackStatusResolver(
    private val service: YoutrackService
) : StatusResolver {

    override fun handles(url: URL): Boolean =
        pattern.containsMatchIn(url.toString())

    override suspend fun resolve(url: URL) = withContext(Dispatchers.IO) {
        val issueId = pattern.find(url.toString())?.groups?.last()?.value
            ?: throw IllegalArgumentException("Couldn't parse $url")
        val response = service.getIssue(issueId = issueId)

        if (response.resolved == null) {
            IssueStatus.Open
        } else {
            IssueStatus.Closed
        }
    }

    companion object {
        private val pattern by lazy {
            "https?://(www.)?youtrack.jetbrains.com/issue/([^/]+)/?".toRegex()
        }
    }
}

internal interface YoutrackService {
    @GET("api/issues/{issueId}?fields=resolved,idReadable,summary")
    suspend fun getIssue(@Path("issueId") issueId: String): YoutrackIssue
}

@JsonClass(generateAdapter = true)
internal data class YoutrackIssue(
    @Json(name = "idReadable") val idReadable: String,
    @Json(name = "summary") val summary: String,
    @Json(name = "resolved") val resolved: String?
)
