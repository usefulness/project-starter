package com.starter.issuechecker

import com.starter.issuechecker.resolvers.github.GithubService
import com.starter.issuechecker.resolvers.github.GithubStatusResolver
import com.starter.issuechecker.resolvers.youtrack.YoutrackService
import com.starter.issuechecker.resolvers.youtrack.YoutrackStatusResolver
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal fun defaultChecker(
    config: IssueChecker.Config
): DefaultChecker {
    val supportedTrackers = mapOf(
        "github.com" to createGithub(config.githubToken),
        "youtrack.jetbrains.com" to createYoutrack()
    )

    return DefaultChecker(
        supportedTrackers = supportedTrackers,
        dispatcher = Dispatchers.Default
    )
}

internal fun restApi(baseUrl: String, okHttpClient: OkHttpClient = OkHttpClient()) = Retrofit.Builder()
    .baseUrl(baseUrl)
    .addConverterFactory(MoshiConverterFactory.create())
    .client(okHttpClient)
    .build()

private fun createYoutrack() = YoutrackStatusResolver(
    service = restApi(
        baseUrl = "https://youtrack.jetbrains.com/"
    ).create(YoutrackService::class.java)
)

private fun createGithub(token: String?) = GithubStatusResolver(
    service = restApi(
        baseUrl = "https://api.github.com/",
        okHttpClient = githubOkHttpClient { token }
    ).create(GithubService::class.java)
)

private fun githubOkHttpClient(auth: () -> String?) =
    OkHttpClient.Builder()
        .addInterceptor { chain ->
            val newRequest = auth()?.let { token ->
                chain.request().newBuilder()
                    .addHeader("Authorization", "token $token")
                    .build()
            } ?: chain.request()

            chain.proceed(newRequest)
        }
        .build()
