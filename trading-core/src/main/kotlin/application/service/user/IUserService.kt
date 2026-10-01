package application.service.user

import application.model.AccountSummary

internal interface IUserService {
    suspend fun getAccountSummary(): AccountSummary
}