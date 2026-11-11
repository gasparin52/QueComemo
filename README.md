# QueComemo

App Android nativa para decidir que comer, desarrollada con Kotlin, Jetpack Compose y arquitectura offline-first.

## Incluye

- Android + Kotlin + Jetpack Compose
- MVVM con capas `presentation` / `domain` / `data` e inyeccion con Hilt
- Firebase Auth con Google Sign-In
- Room como fuente de verdad de recetas y favoritos
- Seed local de 82 recetas desde `app/src/main/assets/recipes_seed.json`
- Asistente de IA en el detalle de receta con Gemini (Firebase AI Logic)
- Tests unitarios con JUnit, MockK y coroutines-test

## Funcionalidades

- Splash y login con Google.
- Ruleta de cocina: comida o postre, ganas de cocinar, ingredientes disponibles y modo `Solo tengo esto` / `Puedo sumar ingredientes`.
- Ruleta de delivery: para mi o para compartir, presupuesto y spin.
- Favoritos guardados offline en Room, con busqueda.
- Detalle de receta con asistente de IA que responde preguntas sobre esa receta.

## Configuracion

1. Copiar `local.properties.example` como `local.properties` y completar `sdk.dir`.
2. Poner el `google-services.json` del proyecto de Firebase en `app/`.
3. En la consola de Firebase:
   - Authentication: habilitar Google y cargar la huella SHA-1 de debug (`./gradlew signingReport`).
   - AI Logic: tocar "Get started" y elegir **Gemini Developer API**. No hace falta pegar ninguna API key en la app: Firebase la administra.
4. Opcional: `GEMINI_MODEL=...` en `local.properties` para cambiar el modelo del asistente.

Gradle usa JDK 21 (`gradle/gradle-daemon-jvm.properties`), aunque la maquina tenga otro Java por defecto.

## Comandos

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

## Estructura

```text
com.quecomemo.app
  data        Room (entities, DAOs, seed), mappers, repositories, remote/ai (Gemini)
  di          modulos de Hilt
  domain      modelos, contratos de repositories y use cases
  presentation
    assistant chat con IA del detalle de receta
    auth      login
    home      menu, ruletas, favoritos, populares y detalle (un archivo por pantalla)
    splash
```

Documentacion de desarrollo en `docs/` (plan general, plan visual de Figma, ruletas y assets pendientes).
