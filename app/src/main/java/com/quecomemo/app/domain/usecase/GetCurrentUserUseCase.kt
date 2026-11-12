package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.model.AppUser
import com.quecomemo.app.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): AppUser? = authRepository.getCurrentUser()
}
