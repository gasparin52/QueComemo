package com.quecomemo.app.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.quecomemo.app.domain.model.StartDestination
import com.quecomemo.app.domain.repository.SessionRepository
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class FirebaseSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionRepository {
    override fun getStartDestination(): StartDestination {
        return runCatching {
            FirebaseApp.initializeApp(context)
            FirebaseAuth.getInstance().currentUser != null
        }
            .map { isLoggedIn ->
                if (isLoggedIn) StartDestination.HOME else StartDestination.LOGIN
            }
            .getOrDefault(StartDestination.LOGIN)
    }
}
