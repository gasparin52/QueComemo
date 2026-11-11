package com.quecomemo.app

import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quecomemo.app.presentation.auth.LoginScreen
import com.quecomemo.app.presentation.home.HomeScreen
import com.quecomemo.app.presentation.splash.SplashGateScreen
import com.quecomemo.app.presentation.splash.SplashUiState
import com.quecomemo.app.presentation.splash.SplashViewModel
import com.quecomemo.app.ui.theme.QueComemoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            splashViewModel.uiState.value is SplashUiState.Loading
        }

        super.onCreate(savedInstanceState)

        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            splashScreenViewProvider.iconView.animate()
                .rotationBy(360f)
                .setDuration(450L)
                .withEndAction { splashScreenViewProvider.remove() }
                .start()
        }

        enableEdgeToEdge()
        setContent { AppRoot(splashViewModel) }
    }
}

private enum class RootScreen {
    LOGIN,
    HOME
}

@Composable
private fun AppRoot(
    viewModel: SplashViewModel
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var currentScreen by rememberSaveable { mutableStateOf<RootScreen?>(null) }

    LaunchedEffect(uiState) {
        currentScreen = when (uiState) {
            SplashUiState.Loading -> null
            SplashUiState.NavigateToHome -> RootScreen.HOME
            SplashUiState.NavigateToLogin -> RootScreen.LOGIN
        }
    }

    QueComemoTheme {
        Surface {
            when (currentScreen) {
                null -> SplashGateScreen()
                RootScreen.LOGIN -> LoginScreen(
                    onLoginSuccess = { currentScreen = RootScreen.HOME }
                )
                RootScreen.HOME -> HomeScreen(
                    onSignedOut = { currentScreen = RootScreen.LOGIN }
                )
            }
        }
    }
}
