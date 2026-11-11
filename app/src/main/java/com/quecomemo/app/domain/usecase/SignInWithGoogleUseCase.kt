package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.model.AppUser
import com.quecomemo.app.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<AppUser> {
        return authRepository.signInWithGoogleIdToken(idToken)
    }
}
