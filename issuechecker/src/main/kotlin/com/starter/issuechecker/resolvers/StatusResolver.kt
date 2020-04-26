package com.starter.issuechecker.resolvers

import com.starter.issuechecker.IssueStatus
import java.net.URL

interface StatusResolver {

    suspend fun resolve(url: URL): IssueStatus
}
