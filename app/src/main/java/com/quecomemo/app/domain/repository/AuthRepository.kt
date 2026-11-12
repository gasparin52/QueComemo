package com.quecomemo.app.domain.repository

import com.quecomemo.app.domain.model.AppUser

interface AuthRepository {
    fun getCurrentUser(): AppUser?
    suspend fun signInWithGoogleIdToken(idToken: String): Result<AppUser>
    suspend fun signOut()
}
