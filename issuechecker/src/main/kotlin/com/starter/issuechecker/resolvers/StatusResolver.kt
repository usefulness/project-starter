package com.starter.issuechecker.resolvers

import com.starter.issuechecker.IssueStatus
import java.net.URL

internal interface StatusResolver {

    suspend fun resolve(url: URL): IssueStatus
}
