package data.network

import application.model.User
import java.util.UUID

interface IAccountSummaryProvider {
    suspend fun getAccountSummary(userId: UUID): User.AccountSummary
}