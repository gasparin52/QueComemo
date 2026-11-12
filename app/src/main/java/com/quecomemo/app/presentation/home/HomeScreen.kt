package com.quecomemo.app.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    onSignedOut: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            viewModel.consumeSignedOut()
            onSignedOut()
        }
    }

    BackHandler(
        enabled = uiState.detailRecipe != null || uiState.currentSection != HomeSection.MENU,
        onBack = viewModel::navigateBack
    )

    uiState.detailRecipe?.let { recipe ->
        RecipeDetailDialog(
            recipe = recipe,
            isFavorite = recipe.id in uiState.favoriteIds,
            onToggleFavorite = { viewModel.toggleFavorite(recipe.id) },
            onOpenFullDetail = { viewModel.openFullRecipeDetail(recipe.id) },
            onDismiss = viewModel::closeRecipeDetail
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            HomeHeader(
                displayName = uiState.displayName,
                email = uiState.email,
                onSignOut = viewModel::signOut
            )
        }

        item {
            AnimatedContent(
                targetState = "${uiState.isLoadingRecipes}-${uiState.errorMessage != null}-${uiState.currentSection}",
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInHorizontally { it / 6 })
                        .togetherWith(fadeOut(tween(160)) + slideOutHorizontally { -it / 8 })
                },
                label = "home-section-transition"
            ) {
                when {
                    uiState.isLoadingRecipes -> LoadingBlock()
                    uiState.errorMessage != null -> ErrorBlock(
                        message = uiState.errorMessage.orEmpty(),
                        onRetry = viewModel::retryRecipes
                    )
                    uiState.currentSection == HomeSection.MENU -> MainMenu(
                        onRoulette = viewModel::openRoulette,
                        onDelivery = viewModel::openDeliveryRoulette,
                        onFavorites = viewModel::openFavorites
                    )
                    uiState.currentSection == HomeSection.ROULETTE -> RouletteFlow(
                        uiState = uiState,
                        ingredientSuggestions = viewModel.ingredientSuggestions(),
                        onSelectCategoryMode = viewModel::selectCookingCategoryMode,
                        onSelectEffort = viewModel::selectEffort,
                        onIngredientQueryChange = viewModel::updateIngredientQuery,
                        onAddIngredient = viewModel::addIngredient,
                        onRemoveIngredient = viewModel::removeIngredient,
                        onContinueToSpin = viewModel::goToSpinStep,
                        onChangeIngredients = viewModel::goToIngredientsStep,
                        onChangeIngredientMode = viewModel::goToIngredientScopeStep,
                        onSelectIngredientMatchMode = viewModel::selectIngredientMatchMode,
                        onSpin = viewModel::spinRoulette,
                        onOpenRecipe = { recipe -> viewModel.openRecipeDetail(recipe.id) }
                    )
                    uiState.currentSection == HomeSection.FAVORITES -> FavoritesContent(
                        recipes = uiState.favoriteRecipes,
                        favoriteIds = uiState.favoriteIds,
                        onOpenRecipe = { recipe -> viewModel.openRecipeDetail(recipe.id) },
                        onToggleFavorite = { recipe -> viewModel.toggleFavorite(recipe.id) }
                    )
                    uiState.currentSection == HomeSection.DELIVERY -> DeliveryFlow(
                        uiState = uiState,
                        onSelectPeopleMode = viewModel::selectDeliveryPeopleMode,
                        onSelectBudget = viewModel::selectDeliveryBudget,
                        onSpin = viewModel::spinDeliveryRoulette
                    )
                    uiState.currentSection == HomeSection.POPULAR -> PopularContent(
                        recipes = uiState.recipes,
                        favoriteIds = uiState.favoriteIds,
                        onOpenRecipe = { recipe -> viewModel.openRecipeDetail(recipe.id) },
                        onToggleFavorite = { recipe -> viewModel.toggleFavorite(recipe.id) }
                    )
                    uiState.currentSection == HomeSection.RECIPE_DETAIL -> uiState.fullDetailRecipe?.let { recipe ->
                        FullRecipeDetailContent(
                            recipe = recipe,
                            isFavorite = recipe.id in uiState.favoriteIds,
                            isAssistantOpen = uiState.isAssistantOpen,
                            onToggleFavorite = { viewModel.toggleFavorite(recipe.id) },
                            onOpenAssistant = viewModel::openAssistant,
                            onCloseAssistant = viewModel::closeAssistant
                        )
                    }
                }
            }
        }
    }
}
