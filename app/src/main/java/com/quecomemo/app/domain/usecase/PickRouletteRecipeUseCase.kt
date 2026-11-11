package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.model.CookingCategoryMode
import com.quecomemo.app.domain.model.CookingEffort
import com.quecomemo.app.domain.model.IngredientMatchMode
import com.quecomemo.app.domain.model.Recipe
import java.text.Normalizer
import javax.inject.Inject

class PickRouletteRecipeUseCase @Inject constructor() {
    operator fun invoke(
        recipes: List<Recipe>,
        categoryMode: CookingCategoryMode,
        effort: CookingEffort,
        selectedIngredients: List<String>,
        ingredientMatchMode: IngredientMatchMode,
        excludedRecipeIds: Set<String> = emptySet()
    ): Recipe? {
        val normalizedIngredients = selectedIngredients
            .map { it.normalizeIngredient() }
            .filter { it.isNotBlank() }

        val candidates = recipes
            .filter { recipe -> recipe.matchesCategoryMode(categoryMode) }
            .filter { recipe -> recipe.matchesEffort(effort) }
            .filter { recipe ->
                when (ingredientMatchMode) {
                    IngredientMatchMode.FLEXIBLE -> recipe.containsSelectedIngredients(normalizedIngredients)
                    IngredientMatchMode.STRICT -> recipe.canBeMadeOnlyWith(normalizedIngredients)
                }
            }

        return candidates
            .filterNot { recipe -> recipe.id in excludedRecipeIds }
            .ifEmpty { candidates }
            .randomOrNull()
    }

    private fun Recipe.matchesEffort(effort: CookingEffort): Boolean {
        return when (effort) {
            CookingEffort.LOW -> totalTimeMinutes <= 20
            CookingEffort.MEDIUM -> totalTimeMinutes in 21..45
            CookingEffort.HIGH -> totalTimeMinutes > 45
        }
    }

    private fun Recipe.matchesCategoryMode(categoryMode: CookingCategoryMode): Boolean {
        val isDessert = tags.any { it.normalizeIngredient() == "postre" } ||
            category.normalizeIngredient().contains("postre")

        return when (categoryMode) {
            CookingCategoryMode.MEAL -> !isDessert
            CookingCategoryMode.DESSERT -> isDessert
        }
    }

    private fun Recipe.containsSelectedIngredients(selectedIngredients: List<String>): Boolean {
        if (selectedIngredients.isEmpty()) return true

        val recipeIngredients = ingredients.map { it.normalizeIngredient() }
        return selectedIngredients.all { selected ->
            recipeIngredients.any { ingredient -> ingredient.contains(selected) || selected.contains(ingredient) }
        }
    }

    private fun Recipe.canBeMadeOnlyWith(selectedIngredients: List<String>): Boolean {
        if (selectedIngredients.isEmpty()) return false

        val importantIngredients = ingredients
            .map { it.normalizeIngredient() }
            .filter { it.isNotBlank() }
            .filterNot { ingredient -> ingredient.isPantryIngredient() || ingredient.isOptionalCookingFat() }

        val matchedIngredients = importantIngredients.filter { ingredient ->
            selectedIngredients.any { selected -> ingredient.matchesSelected(selected) }
        }
        val missingIngredients = importantIngredients - matchedIngredients.toSet()

        if (matchedIngredients.isEmpty()) return false
        if (hasMissingCriticalIngredient(selectedIngredients)) return false

        return missingIngredients.size <= importantIngredients.allowedMissingCount()
    }

    private fun List<String>.allowedMissingCount(): Int {
        return when {
            size <= 5 -> 2
            size <= 8 -> 2
            else -> (size * 0.25f).toInt().coerceAtLeast(2)
        }
    }

    private fun Recipe.hasMissingCriticalIngredient(selectedIngredients: List<String>): Boolean {
        val normalizedName = name.normalizeIngredient()
        return criticalIngredientRules.any { rule ->
            normalizedName.contains(rule.nameKeyword) && !rule.isSatisfiedBy(selectedIngredients)
        }
    }

    private fun String.isOptionalCookingFat(): Boolean {
        val normalized = normalizeIngredient()
        return normalized == "manteca" || normalized == "mantequilla"
    }

    private fun String.matchesSelected(selected: String): Boolean {
        return contains(selected) || selected.contains(this)
    }

    private fun String.isPantryIngredient(): Boolean {
        val normalized = normalizeIngredient()
        return pantryIngredientKeywords.any { keyword ->
            normalized == keyword || normalized.contains(keyword)
        }
    }

    private fun String.normalizeIngredient(): String {
        val noAccents = Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
        return noAccents
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(
                Regex(
                    "\\b(\\d+|1/2|1/4|3/4|medio|media|gr|g|kg|ml|l|cc|litro|litros|cucharada|cucharadas|cucharadita|cucharaditas|taza|tazas|unidad|unidades|a gusto|cantidad necesaria)\\b"
                ),
                " "
            )
            .replace(
                Regex("\\b(picado|picada|picados|picadas|rallado|rallada|rallados|ralladas|cortado|cortada|cortados|cortadas|fresco|fresca|frescos|frescas|molido|molida|molidos|molidas)\\b"),
                " "
            )
            .replace(Regex("\\s+"), " ")
            .trim()
            .singularizeWords()
    }

    private fun String.singularizeWords(): String {
        return split(" ")
            .joinToString(" ") { word ->
                when {
                    word.length <= 3 -> word
                    word.endsWith("ces") -> word.dropLast(3) + "z"
                    word.endsWith("s") -> word.dropLast(1)
                    else -> word
                }
            }
            .trim()
    }

    private companion object {
        val pantryIngredientKeywords = setOf(
            "sal",
            "pimienta",
            "agua",
            "aceite",
            "vinagre",
            "oregano",
            "aji molido",
            "pimenton",
            "provenzal",
            "perejil",
            "laurel",
            "comino",
            "condimento",
            "especia",
            "nuez moscada",
            "ajo en polvo"
        )

        val criticalIngredientRules = listOf(
            CriticalIngredientRule("pollo", setOf("pollo")),
            CriticalIngredientRule("carne", setOf("carne")),
            CriticalIngredientRule("queso", setOf("queso", "muzzarella", "mozzarella")),
            CriticalIngredientRule("arroz", setOf("arroz")),
            CriticalIngredientRule("papa", setOf("papa")),
            CriticalIngredientRule("leche", setOf("leche")),
            CriticalIngredientRule("pizza", setOf("harina", "masa", "prepizza")),
            CriticalIngredientRule("tarta", setOf("tapa de tarta", "masa", "harina")),
            CriticalIngredientRule("empanada", setOf("tapa de empanada", "masa", "harina")),
            CriticalIngredientRule("lenteja", setOf("lenteja")),
            CriticalIngredientRule("sushi", setOf("arroz", "nori"), requireAll = true),
            CriticalIngredientRule("flan", setOf("huevo", "leche"), requireAll = true),
            CriticalIngredientRule("panqueque", setOf("harina", "huevo", "leche"), requireAll = true)
        )
    }

    private data class CriticalIngredientRule(
        val nameKeyword: String,
        val requiredIngredients: Set<String>,
        val requireAll: Boolean = false
    ) {
        fun isSatisfiedBy(selectedIngredients: List<String>): Boolean {
            return if (requireAll) {
                requiredIngredients.all { required ->
                    selectedIngredients.any { selected -> selected.matchesRequired(required) }
                }
            } else {
                requiredIngredients.any { required ->
                    selectedIngredients.any { selected -> selected.matchesRequired(required) }
                }
            }
        }

        private fun String.matchesRequired(required: String): Boolean {
            return contains(required) || required.contains(this)
        }
    }
}
