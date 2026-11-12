# QueComemo

App Android nativa para decidir que comer, desarrollada con Kotlin, Jetpack Compose y arquitectura offline-first.

## Incluye

- Android + Kotlin
- Jetpack Compose
- Hilt
- Firebase Auth con Google Sign-In
- Room como fuente local de recetas
- Seed local de 500 recetas desde `app/src/main/assets/recipes_seed.json`
- Tema visual inicial

## Direccion actual

La app ya no depende de Spoonacular ni de una API externa para el flujo principal. Las recetas viven localmente en Room y la UI observa esa base.

Las imagenes finales quedan para una etapa posterior. Si una receta no tiene imagen, la app muestra solo nombre y especificaciones, sin iconos rotos ni dependencia de PNGs.

## Estado actual

- Splash/login/home iniciales
- Auth con Firebase
- Listado inicial de 500 recetas locales
- Documentacion de desarrollo en `docs/PLAN_DESARROLLO_IA.md`
- Guia de assets pendientes en `docs/ASSETS_PENDIENTES.md`
