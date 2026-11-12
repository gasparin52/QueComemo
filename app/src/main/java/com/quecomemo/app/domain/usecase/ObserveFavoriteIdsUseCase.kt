package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.repository.FavoriteRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteIdsUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(): Flow<List<String>> = favoriteRepository.observeFavoriteIds()
}
