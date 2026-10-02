package data.network

import application.model.User

interface IAccountSummaryProvider {
    suspend fun getAccountSummary(): User.AccountSummary
}