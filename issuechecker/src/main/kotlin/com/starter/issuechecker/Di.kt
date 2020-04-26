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

private fun youtrackService(): YoutrackService {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://youtrack.jetbrains.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    return retrofit.create(YoutrackService::class.java)
}

private fun githubService(okHttpClient: OkHttpClient): GithubService {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .client(okHttpClient)
        .build()

    return retrofit.create(GithubService::class.java)
}

private fun createYoutrack() = YoutrackStatusResolver(
    service = youtrackService()
)

private fun createGithub(token: String?) = GithubStatusResolver(
    service = githubService(githubOkHttpClient { token })
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
