package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.model.StartDestination
import com.quecomemo.app.domain.repository.SessionRepository
import javax.inject.Inject

class GetStartDestinationUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): StartDestination = sessionRepository.getStartDestination()
}
