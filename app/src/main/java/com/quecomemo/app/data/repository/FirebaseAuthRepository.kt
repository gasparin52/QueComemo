package com.quecomemo.app.data.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.quecomemo.app.domain.model.AppUser
import com.quecomemo.app.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthRepository {

    override fun getCurrentUser(): AppUser? {
        initializeFirebaseIfNeeded()
        return Firebase.auth.currentUser?.toAppUser()
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<AppUser> {
        initializeFirebaseIfNeeded()

        return runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            authResult.user?.toAppUser()
                ?: error("No se pudo recuperar el usuario autenticado.")
        }
    }

    override suspend fun signOut() {
        initializeFirebaseIfNeeded()
        val googleSignInClient = GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(com.quecomemo.app.R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        googleSignInClient.signOut().await()
        FirebaseAuth.getInstance().signOut()
    }

    private fun initializeFirebaseIfNeeded() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }

    private fun com.google.firebase.auth.FirebaseUser.toAppUser(): AppUser {
        return AppUser(
            displayName = displayName,
            email = email
        )
    }
}
