package application.service

import domain.User

interface IAuthenticationService {
    suspend fun currentUser(): User

    // Egyelőre marad a createUser, de szerintem jobb lenne a lentebb lévőkre átállni.
    suspend fun createUser(): Result<User>

    //suspend fun signUp(email: String, password: String)
    //suspend fun authenticate(email: String, password: String)
    //suspend fun sendRecoveryEmail(email: String)
    //suspend fun deleteAccount()
    //suspend fun signOut()
}