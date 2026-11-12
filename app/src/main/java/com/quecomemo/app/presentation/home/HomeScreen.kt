package com.quecomemo.app.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quecomemo.app.R
import com.quecomemo.app.domain.model.CookingCategoryMode
import com.quecomemo.app.domain.model.CookingEffort
import com.quecomemo.app.domain.model.CookingMood
import com.quecomemo.app.domain.model.EstimatedCost
import com.quecomemo.app.domain.model.IngredientMatchMode
import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.domain.model.RecipeDifficulty
import com.quecomemo.app.presentation.assistant.AssistantFloatingButton
import com.quecomemo.app.presentation.assistant.RecipeAssistantDialog

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

@Composable
private fun HomeHeader(
    displayName: String?,
    email: String?,
    onSignOut: () -> Unit
) {
    var isUserMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(54.dp)
            .background(Color.White)
            .graphicsLayer {
                shadowElevation = 8f
                shape = RoundedCornerShape(0.dp)
                clip = false
            }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.brand_logo_mark),
            contentDescription = "QueComemo",
            modifier = Modifier.size(36.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.brand_logo_full),
            contentDescription = "QueComemo",
            modifier = Modifier
                .padding(start = 12.dp)
                .height(18.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { isUserMenuOpen = true },
                contentAlignment = Alignment.Center
            ) {
                UserCircleIcon(modifier = Modifier.size(30.dp))
            }
            UserSessionMenu(
                expanded = isUserMenuOpen,
                displayName = displayName,
                email = email,
                onDismiss = { isUserMenuOpen = false },
                onSignOut = {
                    isUserMenuOpen = false
                    onSignOut()
                }
            )
        }
    }
}

@Composable
private fun UserCircleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 2.1.dp.toPx())
        val iconColor = Color(0xFF2A2A2A)
        drawCircle(
            color = iconColor,
            radius = size.minDimension * 0.45f,
            style = stroke
        )
        drawCircle(
            color = iconColor,
            radius = size.minDimension * 0.13f,
            center = center.copy(y = size.height * 0.38f),
            style = stroke
        )
        drawArc(
            color = iconColor,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(
                x = size.width * 0.29f,
                y = size.height * 0.55f
            ),
            size = androidx.compose.ui.geometry.Size(
                width = size.width * 0.42f,
                height = size.height * 0.24f
            ),
            style = stroke
        )
    }
}

@Composable
private fun UserSessionMenu(
    expanded: Boolean,
    displayName: String?,
    email: String?,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(136.dp)
            .background(Color.White)
    ) {
        Text(
            text = displayName?.takeIf { it.isNotBlank() }
                ?: email?.substringBefore("@").orEmpty().ifBlank { "Usuario" },
            modifier = Modifier.padding(start = 13.dp, top = 7.dp, end = 13.dp, bottom = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF2A2A2A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        email?.takeIf { it.isNotBlank() }?.let { userEmail ->
            Text(
                text = userEmail,
                modifier = Modifier.padding(start = 13.dp, end = 13.dp, bottom = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0x99000000),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LogoutIcon(modifier = Modifier.size(13.dp))
                    Text(
                        text = "Cerrar sesi\u00F3n",
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xDE000000)
                    )
                }
            },
            onClick = onSignOut,
            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 0.dp),
            modifier = Modifier.height(34.dp)
        )
    }
}

@Composable
private fun LogoutIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val iconColor = Color(0xFF6F6F6F)
        val strokeWidth = 1.4.dp.toPx()
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.18f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.82f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.18f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.55f, size.height * 0.18f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.82f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.55f, size.height * 0.82f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.43f, size.height * 0.5f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.5f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.3f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.5f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.5f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.7f),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun MainMenu(
    onRoulette: () -> Unit,
    onDelivery: () -> Unit,
    onFavorites: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 31.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(34.dp)
    ) {
        Text(
            text = "\u00BFQu\u00E9 te gustar\u00EDa hacer hoy?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        MenuOptionCard(
            title = "Ruleta de cocina",
            description = "Elegis ganas de cocinar, ingredientes disponibles y la app sortea una receta.",
            enabled = true,
            imageRes = R.drawable.home_card_roulette,
            imageStyle = HomeCardImageStyle.ROULETTE,
            gradient = Brush.horizontalGradient(listOf(Color(0xFFFFF4E5), Color(0xFFFFC078))),
            onClick = onRoulette
        )
        MenuOptionCard(
            title = "Ruleta de delivery",
            description = "Decidi que pedir segun presupuesto y cantidad de personas.",
            enabled = true,
            imageRes = R.drawable.home_card_popular,
            imageStyle = HomeCardImageStyle.DELIVERY,
            gradient = Brush.horizontalGradient(listOf(Color(0xFFEAF8FF), Color(0xFF78D7FF))),
            onClick = onDelivery
        )
        MenuOptionCard(
            title = "Recetas favoritas",
            description = "Acceso rapido a las recetas que guardaste en este celular.",
            enabled = true,
            imageRes = R.drawable.home_card_favorites,
            imageStyle = HomeCardImageStyle.FAVORITES,
            gradient = Brush.horizontalGradient(listOf(Color(0xFFFFF1F1), Color(0xFFFF7878))),
            onClick = onFavorites
        )
    }
}

@Composable
private fun DeliveryFlow(
    uiState: HomeUiState,
    onSelectPeopleMode: (DeliveryPeopleMode) -> Unit,
    onSelectBudget: (DeliveryBudget) -> Unit,
    onSpin: () -> Unit
) {
    AnimatedContent(
        targetState = uiState.deliveryStep,
        transitionSpec = {
            (fadeIn(tween(220)) + slideInHorizontally { it / 5 })
                .togetherWith(fadeOut(tween(150)) + slideOutHorizontally { -it / 6 })
        },
        label = "delivery-step-transition"
    ) { step ->
        when (step) {
            DeliveryStep.PEOPLE -> DeliveryPeopleStep(onSelectPeopleMode = onSelectPeopleMode)
            DeliveryStep.BUDGET -> DeliveryBudgetStep(onSelectBudget = onSelectBudget)
            DeliveryStep.SPIN -> DeliverySpinStep(
                peopleMode = uiState.deliveryPeopleMode,
                budget = uiState.deliveryBudget,
                result = uiState.deliveryResult,
                spinCount = uiState.deliverySpinCount,
                onSpin = onSpin
            )
        }
    }
}

@Composable
private fun DeliveryPeopleStep(
    onSelectPeopleMode: (DeliveryPeopleMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 116.dp, end = 32.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFPara qui\u00E9n\npedimos?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        DeliveryOptionButton(
            title = "Para m\u00ED",
            description = "Opciones simples para una persona.",
            modifier = Modifier.padding(top = 55.dp),
            onClick = { onSelectPeopleMode(DeliveryPeopleMode.SOLO) }
        )
        DeliveryOptionButton(
            title = "Para compartir",
            description = "Opciones que rinden mejor para varios.",
            modifier = Modifier.padding(top = 24.dp),
            onClick = { onSelectPeopleMode(DeliveryPeopleMode.GROUP) }
        )
    }
}

@Composable
private fun DeliveryBudgetStep(
    onSelectBudget: (DeliveryBudget) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 88.dp, end = 32.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFCu\u00E1nto quer\u00E9s\ngastar?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        DeliveryOptionButton(
            title = "Barato",
            description = "Algo tranqui, rendidor y sin romper la billetera.",
            modifier = Modifier.padding(top = 42.dp),
            onClick = { onSelectBudget(DeliveryBudget.LOW) }
        )
        DeliveryOptionButton(
            title = "Medio",
            description = "Un pedido normal, con un poco m\u00E1s de margen.",
            modifier = Modifier.padding(top = 18.dp),
            onClick = { onSelectBudget(DeliveryBudget.MEDIUM) }
        )
        DeliveryOptionButton(
            title = "Caro",
            description = "Antojo fuerte o algo para darse un gusto.",
            modifier = Modifier.padding(top = 18.dp),
            onClick = { onSelectBudget(DeliveryBudget.HIGH) }
        )
    }
}

@Composable
private fun DeliveryOptionButton(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(10.dp),
                clip = false
            )
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFAA48))
            .clickable(onClick = onClick)
            .padding(horizontal = 23.dp, vertical = 17.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = description,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
private fun DeliverySpinStep(
    peopleMode: DeliveryPeopleMode?,
    budget: DeliveryBudget?,
    result: String?,
    spinCount: Int,
    onSpin: () -> Unit
) {
    val wheelRotation by animateFloatAsState(
        targetValue = spinCount * 1080f,
        animationSpec = tween(durationMillis = 900),
        label = "delivery-roulette-rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 116.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ruleta de delivery",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = listOfNotNull(peopleMode?.toLabel(), budget?.toLabel()).joinToString(" · "),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xB3000000),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Image(
            painter = painterResource(id = R.drawable.logo_solo),
            contentDescription = "Ruleta de delivery",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(top = 29.dp)
                .size(238.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(119.dp),
                    clip = false
                )
                .rotate(wheelRotation)
        )
        Box(
            modifier = Modifier
                .padding(top = 45.dp)
                .width(266.dp)
                .height(45.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x33F1F1F1))
                .border(
                    width = 1.dp,
                    color = Color(0x29000000),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = result.orEmpty(),
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandWine,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 57.dp)
                .width(188.dp)
                .height(31.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFF5700))
                .clickable(onClick = onSpin),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Spin",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}


@Composable
private fun FavoritesContent(
    recipes: List<Recipe>,
    favoriteIds: Set<String>,
    onOpenRecipe: (Recipe) -> Unit,
    onToggleFavorite: (Recipe) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var expandedRecipeId by remember { mutableStateOf<String?>(recipes.firstOrNull()?.id) }
    val filteredRecipes = remember(recipes, query) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            recipes
        } else {
            recipes.filter { recipe ->
                recipe.name.contains(cleanQuery, ignoreCase = true) ||
                    recipe.ingredients.any { it.contains(cleanQuery, ignoreCase = true) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 31.dp, top = 45.dp, end = 31.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Favoritos",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        FavoriteSearchField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )

        if (filteredRecipes.isEmpty()) {
            EmptyFavoritesState(
                modifier = Modifier.padding(top = 91.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                filteredRecipes.forEach { recipe ->
                    FavoriteRecipeItem(
                    recipe = recipe,
                    isFavorite = recipe.id in favoriteIds,
                        isExpanded = expandedRecipeId == recipe.id,
                        onClick = {
                            expandedRecipeId = if (expandedRecipeId == recipe.id) null else recipe.id
                        },
                        onOpenRecipe = { onOpenRecipe(recipe) },
                    onToggleFavorite = { onToggleFavorite(recipe) }
                )
                }
            }
        }
    }
}

@Composable
private fun FavoriteSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = Color(0xFF989797),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 34.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = "search",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF989797)
                    )
                }
                innerTextField()
            }
        )
        SearchIcon(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(22.dp)
        )
    }
}

@Composable
private fun FavoriteRecipeItem(
    recipe: Recipe,
    isFavorite: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onOpenRecipe: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(10.dp),
                clip = false
            )
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color(0xFFFFAA48))
                .clickable(onClick = onClick)
                .padding(start = 48.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = recipe.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (isFavorite) "\u2665" else "\u2661",
                modifier = Modifier.clickable(onClick = onToggleFavorite),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp)
                    .padding(start = 24.dp, top = 14.dp, end = 16.dp, bottom = 11.dp)
                    .clickable(onClick = onOpenRecipe)
            ) {
                Text(
                    text = "Tiempo de cocci\u00F3n",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClockIcon(modifier = Modifier.size(14.dp))
                    Text(
                        text = "Listo en ${recipe.totalTimeMinutes} min",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light,
                        color = Color(0xC9000000)
                    )
                }
                Text(
                    text = "Cantidad de ingredientes",
                    modifier = Modifier.padding(top = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BowlIcon(modifier = Modifier.size(17.dp))
                    Text(
                        text = "${recipe.ingredients.size} ingredientes",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light,
                        color = Color(0xC9000000)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0x1F000000))
                    )
                    Text(
                        text = "Dificultad: ${recipe.difficulty.toLabel()}",
                        modifier = Modifier.padding(start = 14.dp, end = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light,
                        color = Color(0xC9000000)
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(recipe.difficulty.toColor())
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CloudOffIcon(modifier = Modifier.size(64.dp))
        Text(
            text = "No se encontraron resultados",
            modifier = Modifier.padding(top = 42.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xB3000000),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun PopularContent(
    recipes: List<Recipe>,
    favoriteIds: Set<String>,
    onOpenRecipe: (Recipe) -> Unit,
    onToggleFavorite: (Recipe) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var expandedRecipeId by remember { mutableStateOf<String?>(recipes.firstOrNull()?.id) }
    val topRecipes = remember(recipes) { recipes.take(10) }
    val filteredRecipes = remember(topRecipes, query) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            topRecipes
        } else {
            topRecipes.filter { recipe ->
                recipe.name.contains(cleanQuery, ignoreCase = true) ||
                    recipe.ingredients.any { it.contains(cleanQuery, ignoreCase = true) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 44.dp, top = 28.dp, end = 44.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "M\u00E1s populares de hoy",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        FavoriteSearchField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 34.dp)
        )

        if (filteredRecipes.isEmpty()) {
            EmptyFavoritesState(
                modifier = Modifier.padding(top = 91.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 38.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                filteredRecipes.forEach { recipe ->
                    val rank = topRecipes.indexOfFirst { it.id == recipe.id }.takeIf { it >= 0 }?.plus(1) ?: 1
                    PopularRecipeItem(
                        rank = rank,
                        recipe = recipe,
                        isFavorite = recipe.id in favoriteIds,
                        isExpanded = expandedRecipeId == recipe.id,
                        popularity = 986 - ((rank - 1) * 7),
                        onClick = {
                            expandedRecipeId = if (expandedRecipeId == recipe.id) null else recipe.id
                        },
                        onOpenRecipe = { onOpenRecipe(recipe) },
                        onToggleFavorite = { onToggleFavorite(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularRecipeItem(
    rank: Int,
    recipe: Recipe,
    isFavorite: Boolean,
    isExpanded: Boolean,
    popularity: Int,
    onClick: () -> Unit,
    onOpenRecipe: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isExpanded) 4.dp else 0.dp,
                shape = RoundedCornerShape(5.dp),
                clip = false
            )
            .clip(RoundedCornerShape(5.dp))
            .background(if (isExpanded) Color.White else Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(41.dp)
                .background(Color(0xFFFFAA48))
                .clickable(onClick = onClick)
                .padding(start = 16.dp, end = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank. ${recipe.name}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\u2665",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Text(
                    text = popularity.toString(),
                    modifier = Modifier.padding(start = 5.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Text(
                text = if (isFavorite) "\u2665" else "\u2661",
                modifier = Modifier
                    .padding(start = 14.dp)
                    .clickable(onClick = onToggleFavorite),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(147.dp)
                    .padding(start = 24.dp, top = 18.dp, end = 16.dp, bottom = 11.dp)
                    .clickable(onClick = onOpenRecipe)
            ) {
                Text(
                    text = "Tiempo de cocci\u00F3n",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClockIcon(modifier = Modifier.size(14.dp))
                    Text(
                        text = "Listo en ${recipe.totalTimeMinutes} min",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light,
                        color = Color(0xC9000000)
                    )
                }
                Text(
                    text = "Cantidad de ingredientes",
                    modifier = Modifier.padding(top = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BowlIcon(modifier = Modifier.size(17.dp))
                    Text(
                        text = "${recipe.ingredients.size} ingredientes",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light,
                        color = Color(0xC9000000)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0x1F000000))
                    )
                    Text(
                        text = "Dificultad: ${recipe.difficulty.toLabel()}",
                        modifier = Modifier.padding(start = 14.dp, end = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light,
                        color = Color(0xC9000000)
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(recipe.difficulty.toColor())
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val iconColor = Color(0xFF989797)
        val stroke = Stroke(width = 2.dp.toPx())
        drawCircle(
            color = iconColor,
            radius = size.minDimension * 0.32f,
            center = center.copy(x = size.width * 0.42f, y = size.height * 0.42f),
            style = stroke
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.66f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.9f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
private fun ClockIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val iconColor = Color(0xFF555555)
        val stroke = Stroke(width = 1.4.dp.toPx())
        drawCircle(color = iconColor, style = stroke)
        drawLine(
            color = iconColor,
            start = center,
            end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.25f),
            strokeWidth = 1.4.dp.toPx()
        )
        drawLine(
            color = iconColor,
            start = center,
            end = androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.55f),
            strokeWidth = 1.4.dp.toPx()
        )
    }
}

@Composable
private fun BowlIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val iconColor = Color(0xFF555555)
        val strokeWidth = 1.4.dp.toPx()
        drawArc(
            color = iconColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.28f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.5f),
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.55f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.55f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.32f, size.height * 0.82f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.68f, size.height * 0.82f),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun MenuOptionCard(
    title: String,
    description: String,
    enabled: Boolean,
    imageRes: Int,
    imageStyle: HomeCardImageStyle,
    gradient: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(153.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 3.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x24000000))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(10.dp))
                .background(gradient)
                .clickable(enabled = enabled, onClick = onClick)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .homeCardImageModifier(imageStyle)
                    .align(Alignment.CenterStart)
                    .graphicsLayer { alpha = if (enabled) 0.78f else 0.62f }
            )
            Text(
                text = title,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .fillMaxWidth(0.46f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (title.contains("delivery")) Color.Black else BrandWine,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun RouletteFlow(
    uiState: HomeUiState,
    ingredientSuggestions: List<String>,
    onSelectCategoryMode: (CookingCategoryMode) -> Unit,
    onSelectEffort: (CookingEffort) -> Unit,
    onIngredientQueryChange: (String) -> Unit,
    onAddIngredient: (String) -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onContinueToSpin: () -> Unit,
    onChangeIngredients: () -> Unit,
    onChangeIngredientMode: () -> Unit,
    onSelectIngredientMatchMode: (IngredientMatchMode) -> Unit,
    onSpin: () -> Unit,
    onOpenRecipe: (Recipe) -> Unit
) {
    AnimatedContent(
        targetState = uiState.rouletteStep,
        transitionSpec = {
            (fadeIn(tween(220)) + slideInHorizontally { it / 5 })
                .togetherWith(fadeOut(tween(150)) + slideOutHorizontally { -it / 6 })
        },
        label = "roulette-step-transition"
    ) { step ->
        when (step) {
            RouletteStep.CATEGORY -> CookingCategoryStep(onSelectCategoryMode = onSelectCategoryMode)
            RouletteStep.EFFORT -> EffortStep(onSelectEffort = onSelectEffort)
            RouletteStep.INGREDIENTS -> IngredientsStep(
                query = uiState.ingredientQuery,
                selectedIngredients = uiState.selectedIngredients,
                suggestions = ingredientSuggestions,
                onQueryChange = onIngredientQueryChange,
                onAddIngredient = onAddIngredient,
                onRemoveIngredient = onRemoveIngredient,
                onContinue = onContinueToSpin
            )
            RouletteStep.INGREDIENT_SCOPE -> IngredientScopeStep(
                onSelectMode = onSelectIngredientMatchMode
            )
            RouletteStep.SPIN -> SpinStep(
                selectedEffort = uiState.selectedEffort,
                selectedIngredients = uiState.selectedIngredients,
                ingredientMatchMode = uiState.ingredientMatchMode,
                result = uiState.rouletteResult,
                spinCount = uiState.rouletteSpinCount,
                onSpin = onSpin,
                onChangeIngredients = onChangeIngredients,
                onChangeIngredientMode = onChangeIngredientMode,
                onOpenRecipe = onOpenRecipe
            )
        }
    }
}

@Composable
private fun CookingCategoryStep(
    onSelectCategoryMode: (CookingCategoryMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 116.dp, end = 32.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFQu\u00E9 quer\u00E9s\npreparar?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IngredientScopeOption(
            title = "Comida",
            description = "Almuerzo, cena, algo rapido o una receta para cocinar en casa.",
            modifier = Modifier.padding(top = 55.dp),
            onClick = { onSelectCategoryMode(CookingCategoryMode.MEAL) }
        )
        IngredientScopeOption(
            title = "Postre",
            description = "Algo dulce para merienda, sobremesa o antojo.",
            modifier = Modifier.padding(top = 24.dp),
            onClick = { onSelectCategoryMode(CookingCategoryMode.DESSERT) }
        )
    }
}

@Composable
private fun EffortStep(
    onSelectEffort: (CookingEffort) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFcu\u00E1ntas ganas tienes\nde cocinar?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        EffortOption(
            title = "Pocas",
            imageRes = R.drawable.effort_low,
            modifier = Modifier.padding(top = 25.dp),
            onClick = { onSelectEffort(CookingEffort.LOW) }
        )
        EffortOption(
            title = "Un poco...",
            imageRes = R.drawable.effort_medium,
            modifier = Modifier.padding(top = 22.dp),
            onClick = { onSelectEffort(CookingEffort.MEDIUM) }
        )
        EffortOption(
            title = "Muchas!",
            imageRes = R.drawable.effort_high,
            modifier = Modifier.padding(top = 22.dp),
            onClick = { onSelectEffort(CookingEffort.HIGH) }
        )
    }
}

@Composable
private fun EffortOption(
    title: String,
    imageRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .width(170.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(142.dp)
                .clip(RoundedCornerShape(100.dp))
        )
        Text(
            text = title,
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun IngredientsStep(
    query: String,
    selectedIngredients: List<String>,
    suggestions: List<String>,
    onQueryChange: (String) -> Unit,
    onAddIngredient: (String) -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 136.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFQu\u00E9 ingredientes\ntienes?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        IngredientSearchField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp)
        )

        if (suggestions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.take(4).forEach { ingredient ->
                    Text(
                        text = ingredient,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = Color(0x33989797),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onAddIngredient(ingredient) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xB8000000),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .height(253.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0x3B000000))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                selectedIngredients.chunked(3).forEach { rowIngredients ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowIngredients.forEach { ingredient ->
                            IngredientChip(
                                text = ingredient,
                                onRemove = { onRemoveIngredient(ingredient) }
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 84.dp)
                .width(188.dp)
                .height(31.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFF5700))
                .clickable(onClick = onContinue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Siguiente",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun IngredientScopeStep(
    onSelectMode: (IngredientMatchMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 126.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFCocin\u00E1s solo\ncon esto?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        IngredientScopeOption(
            title = "Solo tengo esto",
            description = "Busca recetas que puedas hacer con tus ingredientes. Condimentos, agua y aceite no bloquean.",
            modifier = Modifier.padding(top = 54.dp),
            onClick = { onSelectMode(IngredientMatchMode.STRICT) }
        )

        IngredientScopeOption(
            title = "Puedo sumar ingredientes",
            description = "Busca recetas que usen lo que cargaste, aunque pidan otros ingredientes.",
            modifier = Modifier.padding(top = 24.dp),
            onClick = { onSelectMode(IngredientMatchMode.FLEXIBLE) }
        )
    }
}

@Composable
private fun IngredientScopeOption(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(10.dp),
                clip = false
            )
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFAA48))
            .clickable(onClick = onClick)
            .padding(horizontal = 23.dp, vertical = 18.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = description,
            modifier = Modifier.padding(top = 9.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
private fun IngredientSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = Color(0xFF989797),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 34.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xDE000000)),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = "search",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF989797)
                    )
                }
                innerTextField()
            }
        )
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(22.dp)
        ) {
            val iconColor = Color(0xFF989797)
            val stroke = Stroke(width = 2.dp.toPx())
            drawCircle(
                color = iconColor,
                radius = size.minDimension * 0.32f,
                center = center.copy(x = size.width * 0.42f, y = size.height * 0.42f),
                style = stroke
            )
            drawLine(
                color = iconColor,
                start = androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.66f),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.9f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
private fun IngredientChip(
    text: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(104.dp)
            .height(28.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(10.dp),
                clip = false
            )
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0x30000000),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(start = 8.dp, end = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xB8000000),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "x",
            modifier = Modifier.clickable(onClick = onRemove),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8B8B8B)
        )
    }
}

@Composable
private fun SpinStep(
    selectedEffort: CookingEffort?,
    selectedIngredients: List<String>,
    ingredientMatchMode: IngredientMatchMode,
    result: Recipe?,
    spinCount: Int,
    onSpin: () -> Unit,
    onChangeIngredients: () -> Unit,
    onChangeIngredientMode: () -> Unit,
    onOpenRecipe: (Recipe) -> Unit
) {
    val wheelRotation by animateFloatAsState(
        targetValue = spinCount * 1080f,
        animationSpec = tween(durationMillis = 900),
        label = "roulette-logo-rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 136.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ruleta de comida",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Image(
            painter = painterResource(id = R.drawable.logo_solo),
            contentDescription = "Ruleta de comida",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(top = 24.dp)
                .size(261.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(131.dp),
                    clip = false
                )
                .rotate(wheelRotation)
        )

        Box(
            modifier = Modifier
                .padding(top = 48.dp)
                .width(266.dp)
                .height(45.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x33F1F1F1))
                .border(
                    width = 1.dp,
                    color = Color(0x29000000),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(
                    enabled = result != null,
                    onClick = { result?.let(onOpenRecipe) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = result?.name.orEmpty(),
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandWine,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (spinCount > 0 && result == null) {
            Column(
                modifier = Modifier
                    .width(286.dp)
                    .padding(top = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (ingredientMatchMode) {
                        IngredientMatchMode.STRICT -> "No encontramos recetas solo con esos ingredientes. Prob\u00E1 sumar alguno m\u00E1s."
                        IngredientMatchMode.FLEXIBLE -> "No encontramos recetas con esos filtros. Prob\u00E1 sumar m\u00E1s ingredientes."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xB3000000),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RouletteSmallActionButton(
                        text = "Ingredientes",
                        onClick = onChangeIngredients
                    )
                    RouletteSmallActionButton(
                        text = "Modo",
                        onClick = onChangeIngredientMode
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }

        Box(
            modifier = Modifier
                .padding(top = 43.dp)
                .width(188.dp)
                .height(31.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFF5700))
                .clickable(onClick = onSpin),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Spin",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RouletteSmallActionButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(118.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = Color(0xFF989797),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = BrandWine
        )
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    isFavorite: Boolean,
    onOpenRecipe: () -> Unit,
    onToggleFavorite: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecipeChip(text = "${recipe.totalTimeMinutes} min")
                RecipeChip(text = recipe.category)
            }
            Text(
                text = "${recipe.difficulty.toLabel()} - ${recipe.cookingMood.toLabel()} - ${recipe.estimatedCost.toLabel()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onOpenRecipe) {
                    Text(text = "Ver receta")
                }
                onToggleFavorite?.let {
                    OutlinedButton(onClick = it) {
                        Text(text = if (isFavorite) "Quitar favorito" else "Guardar")
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeDetailDialog(
    recipe: Recipe,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenFullDetail: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(180)) + scaleIn(
                animationSpec = tween(220),
                initialScale = 0.92f
            ),
            exit = fadeOut(tween(120)) + scaleOut(tween(120))
        ) {
            Card(
                modifier = Modifier
                    .width(381.dp)
                    .height(517.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0x33000000))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 43.dp, top = 30.dp, end = 33.dp, bottom = 31.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = recipe.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .width(295.dp)
                            .height(196.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(0xFFF1F1F1))
                            .border(
                                width = 1.dp,
                                color = Color(0x29000000),
                                shape = RoundedCornerShape(15.dp)
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(144.dp)
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        recipe.steps.take(2).forEachIndexed { index, step ->
                            Text(
                                text = "${index + 1}. $step",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp
                                ),
                                fontWeight = FontWeight.Light,
                                color = Color(0xBA000000),
                                maxLines = if (index == 0) 6 else 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .width(188.dp)
                            .height(31.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(10.dp),
                                clip = false
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFF5700))
                            .clickable(onClick = onOpenFullDetail),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ver",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullRecipeDetailContent(
    recipe: Recipe,
    isFavorite: Boolean,
    isAssistantOpen: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenAssistant: () -> Unit,
    onCloseAssistant: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 52.dp, top = 48.dp, end = 52.dp, bottom = 96.dp)
        ) {
            Text(
                text = recipe.name,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .padding(top = 36.dp)
                    .width(295.dp)
                    .height(196.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F1F1))
                    .border(
                        width = 1.dp,
                        color = Color(0x22000000),
                        shape = RoundedCornerShape(10.dp)
                    )
            )

            Text(
                text = "Tiempo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xB8000000),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "Listo en ${recipe.totalTimeMinutes} minutos.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 17.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xB8000000),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = "Ingredientes",
                modifier = Modifier.padding(top = 40.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xBA000000)
            )
            Text(
                text = recipe.ingredients.joinToString(separator = "\n"),
                modifier = Modifier.padding(top = 17.dp),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 16.sp),
                color = Color.Black
            )

            Text(
                text = "Instrucciones",
                modifier = Modifier.padding(top = 40.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xBA000000)
            )
            Text(
                text = recipe.steps.mapIndexed { index, step ->
                    "${('a'.code + index).toChar()}. $step"
                }.joinToString(separator = "\n"),
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 16.sp),
                color = Color(0xBA000000)
            )
        }

        AssistantFloatingButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 24.dp),
            onClick = onOpenAssistant
        )

        if (isAssistantOpen) {
            RecipeAssistantDialog(recipe = recipe, onDismiss = onCloseAssistant)
        }
    }
}

@Composable
private fun RecipeChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun LoadingBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(872.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 284.dp)
                .size(72.dp),
            color = Color(0xFF8B8B8B),
            strokeWidth = 4.dp
        )
        AssistantFloatingButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 28.dp)
        )
    }
}

@Composable
private fun ErrorBlock(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(872.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 284.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CloudOffIcon(modifier = Modifier.size(64.dp))
            Text(
                text = "Error al cargar receta.",
                modifier = Modifier.padding(top = 40.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xB3000000)
            )
        }
        AssistantFloatingButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 17.dp)
        )
    }
}

@Composable
private fun CloudOffIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val iconColor = Color(0xFF6F6F6F)
        val stroke = Stroke(width = 2.dp.toPx())
        drawArc(
            color = iconColor,
            startAngle = 190f,
            sweepAngle = 210f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.22f, size.height * 0.18f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.42f),
            style = stroke
        )
        drawArc(
            color = iconColor,
            startAngle = 245f,
            sweepAngle = 210f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.55f, size.height * 0.29f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.32f, size.height * 0.32f),
            style = stroke
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.72f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.78f, size.height * 0.72f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.57f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.88f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.88f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.88f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.05f, size.height * 0.05f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.95f),
            strokeWidth = 2.2.dp.toPx()
        )
    }
}

private fun CookingEffort.toLabel(): String {
    return when (this) {
        CookingEffort.LOW -> "Pocas ganas: hasta 20 minutos"
        CookingEffort.MEDIUM -> "Mas o menos: 20 a 45 minutos"
        CookingEffort.HIGH -> "Muchas ganas: mas de 45 minutos"
    }
}

private fun RecipeDifficulty.toLabel(): String {
    return when (this) {
        RecipeDifficulty.EASY -> "facil"
        RecipeDifficulty.MEDIUM -> "media"
        RecipeDifficulty.HARD -> "dificil"
    }
}

private fun RecipeDifficulty.toColor(): Color {
    return when (this) {
        RecipeDifficulty.EASY -> Color(0xFF20C85A)
        RecipeDifficulty.MEDIUM -> Color(0xFFFFB000)
        RecipeDifficulty.HARD -> Color(0xFFFF1717)
    }
}

private fun CookingMood.toLabel(): String {
    return when (this) {
        CookingMood.LOW -> "pocas ganas"
        CookingMood.MEDIUM -> "ganas medias"
        CookingMood.HIGH -> "con tiempo"
    }
}

private fun EstimatedCost.toLabel(): String {
    return when (this) {
        EstimatedCost.LOW -> "barato"
        EstimatedCost.MEDIUM -> "medio"
        EstimatedCost.HIGH -> "caro"
    }
}

private fun DeliveryPeopleMode.toLabel(): String {
    return when (this) {
        DeliveryPeopleMode.SOLO -> "Para m\u00ED"
        DeliveryPeopleMode.GROUP -> "Para compartir"
    }
}

private fun DeliveryBudget.toLabel(): String {
    return when (this) {
        DeliveryBudget.LOW -> "Barato"
        DeliveryBudget.MEDIUM -> "Medio"
        DeliveryBudget.HIGH -> "Caro"
    }
}

private enum class HomeCardImageStyle {
    ROULETTE,
    DELIVERY,
    FAVORITES,
    POPULAR
}

private fun Modifier.homeCardImageModifier(style: HomeCardImageStyle): Modifier {
    return when (style) {
        HomeCardImageStyle.ROULETTE -> this
            .size(400.dp)
            .offset(x = (-85).dp, y = (0).dp)
        HomeCardImageStyle.DELIVERY -> this
            .width(400.dp)
            .height(213.dp)
            .offset(x = (-75).dp, y = (0).dp)
        HomeCardImageStyle.FAVORITES -> this
            .width(400.dp)
            .height(185.dp)
            .offset(x = (-65).dp, y = (0).dp)
        HomeCardImageStyle.POPULAR -> this
            .width(400.dp)
            .height(213.dp)
            .offset(x = (-75).dp, y = (0).dp)
    }
}

private val BrandWine = Color(0xFF3C0006)

