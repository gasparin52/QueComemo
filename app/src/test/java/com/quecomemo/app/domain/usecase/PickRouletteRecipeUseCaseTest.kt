package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.model.CookingCategoryMode
import com.quecomemo.app.domain.model.CookingEffort
import com.quecomemo.app.domain.model.IngredientMatchMode
import com.quecomemo.app.testutil.testRecipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PickRouletteRecipeUseCaseTest {
    private val useCase = PickRouletteRecipeUseCase()

    private val omelette = testRecipe(
        id = "omelette",
        name = "Omelette simple",
        totalMinutes = 10,
        ingredients = listOf("2 huevos", "sal", "aceite")
    )
    private val omeletteDeQueso = testRecipe(
        id = "omelette-queso",
        name = "Omelette de queso",
        totalMinutes = 12,
        ingredients = listOf("2 huevos", "100 g de queso", "sal")
    )
    private val guiso = testRecipe(
        id = "guiso",
        name = "Guiso de lentejas",
        totalMinutes = 60,
        ingredients = listOf("lentejas", "cebolla", "zanahoria", "chorizo")
    )
    private val milanesa = testRecipe(
        id = "milanesa",
        name = "Milanesa de carne",
        totalMinutes = 30,
        ingredients = listOf("carne", "pan rallado", "huevo")
    )
    private val flan = testRecipe(
        id = "flan",
        name = "Flan casero",
        totalMinutes = 60,
        ingredients = listOf("huevos", "leche", "azucar"),
        tags = listOf("postre")
    )
    private val all = listOf(omelette, omeletteDeQueso, guiso, milanesa, flan)

    @Test
    fun `filtra por ganas de cocinar segun el tiempo total`() {
        val low = pick(effort = CookingEffort.LOW, mode = IngredientMatchMode.FLEXIBLE)
        val medium = pick(effort = CookingEffort.MEDIUM, mode = IngredientMatchMode.FLEXIBLE)
        val high = pick(effort = CookingEffort.HIGH, mode = IngredientMatchMode.FLEXIBLE)

        assert(low?.id in setOf("omelette", "omelette-queso"))
        assertEquals("milanesa", medium?.id)
        assertEquals("guiso", high?.id)
    }

    @Test
    fun `modo postre no devuelve platos salados`() {
        val result = useCase(
            recipes = all,
            categoryMode = CookingCategoryMode.DESSERT,
            effort = CookingEffort.HIGH,
            selectedIngredients = emptyList(),
            ingredientMatchMode = IngredientMatchMode.FLEXIBLE
        )

        assertEquals("flan", result?.id)
    }

    @Test
    fun `modo flexible exige que la receta contenga los ingredientes elegidos`() {
        val result = pick(
            effort = CookingEffort.MEDIUM,
            ingredients = listOf("Carne"),
            mode = IngredientMatchMode.FLEXIBLE
        )
        val none = pick(
            effort = CookingEffort.MEDIUM,
            ingredients = listOf("salmon"),
            mode = IngredientMatchMode.FLEXIBLE
        )

        assertEquals("milanesa", result?.id)
        assertNull(none)
    }

    @Test
    fun `modo estricto con huevo devuelve la receta simple e ignora condimentos`() {
        repeat(20) {
            val result = pick(
                effort = CookingEffort.LOW,
                ingredients = listOf("huevo"),
                mode = IngredientMatchMode.STRICT
            )
            assertEquals("omelette", result?.id)
        }
    }

    @Test
    fun `modo estricto descarta recetas con ingrediente critico en el nombre`() {
        val result = useCase(
            recipes = listOf(omeletteDeQueso),
            categoryMode = CookingCategoryMode.MEAL,
            effort = CookingEffort.LOW,
            selectedIngredients = listOf("huevos"),
            ingredientMatchMode = IngredientMatchMode.STRICT
        )

        assertNull(result)
    }

    @Test
    fun `modo estricto sin ingredientes no devuelve nada`() {
        assertNull(pick(effort = CookingEffort.LOW, mode = IngredientMatchMode.STRICT))
    }

    @Test
    fun `evita repetir la receta excluida si hay alternativas`() {
        repeat(20) {
            val result = useCase(
                recipes = all,
                categoryMode = CookingCategoryMode.MEAL,
                effort = CookingEffort.LOW,
                selectedIngredients = emptyList(),
                ingredientMatchMode = IngredientMatchMode.FLEXIBLE,
                excludedRecipeIds = setOf("omelette")
            )
            assertEquals("omelette-queso", result?.id)
        }
    }

    @Test
    fun `repite la receta excluida si es la unica opcion`() {
        val result = useCase(
            recipes = all,
            categoryMode = CookingCategoryMode.MEAL,
            effort = CookingEffort.HIGH,
            selectedIngredients = emptyList(),
            ingredientMatchMode = IngredientMatchMode.FLEXIBLE,
            excludedRecipeIds = setOf("guiso")
        )

        assertEquals("guiso", result?.id)
    }

    private fun pick(
        effort: CookingEffort,
        mode: IngredientMatchMode,
        ingredients: List<String> = emptyList()
    ) = useCase(
        recipes = all,
        categoryMode = CookingCategoryMode.MEAL,
        effort = effort,
        selectedIngredients = ingredients,
        ingredientMatchMode = mode
    )
}
