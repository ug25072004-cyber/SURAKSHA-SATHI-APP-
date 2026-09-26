package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class FirebaseAuthService(
    private val authInstance: FirebaseAuth? = null
) {
    val auth: FirebaseAuth?
        get() = authInstance ?: try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            val provider = user.providerData.firstOrNull { it.providerId != "firebase" }?.providerId ?: "password"
            _authState.value = AuthState.Authenticated(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName ?: user.email?.substringBefore('@') ?: "Responder",
                photoUrl = user.photoUrl?.toString(),
                provider = provider,
                isEmailVerified = user.isEmailVerified
            )
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    init {
        attachAuthListenerIfPossible()
    }

    fun attachAuthListenerIfPossible() {
        try {
            auth?.addAuthStateListener(authStateListener)
            val initialUser = auth?.currentUser
            if (initialUser != null) {
                val provider = initialUser.providerData.firstOrNull { it.providerId != "firebase" }?.providerId ?: "password"
                _authState.value = AuthState.Authenticated(
                    uid = initialUser.uid,
                    email = initialUser.email,
                    displayName = initialUser.displayName ?: initialUser.email?.substringBefore('@') ?: "Responder",
                    photoUrl = initialUser.photoUrl?.toString(),
                    provider = provider,
                    isEmailVerified = initialUser.isEmailVerified
                )
            }
        } catch (_: Exception) {}
    }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    /**
     * Sets a local authenticated responder session when cloud network is offline or API key is restricted.
     */
    fun setLocalAuthenticatedUser(
        uid: String,
        email: String,
        displayName: String,
        provider: String = "tactical_field_offline"
    ) {
        _authState.value = AuthState.Authenticated(
            uid = uid,
            email = email,
            displayName = displayName,
            provider = provider,
            isEmailVerified = true
        )
    }

    /**
     * Creates a new user with Email and Password, and sets their display name / call sign.
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanEmail = email.trim()
            require(cleanEmail.contains("@") && cleanEmail.contains(".")) { "Invalid email address format" }
            require(password.length >= 6) { "Password must be at least 6 characters" }

            val firebaseAuth = auth ?: throw IllegalStateException("Firebase is not initialized")
            val authResult = firebaseAuth.createUserWithEmailAndPassword(cleanEmail, password).awaitTask()
            val user = authResult.user ?: throw IllegalStateException("Firebase user creation returned null")

            if (displayName.isNotBlank()) {
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdate).awaitTask()
            }
            user
        }
    }

    /**
     * Signs in with existing Email and Password.
     */
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanEmail = email.trim()
            require(cleanEmail.isNotBlank()) { "Email cannot be empty" }
            require(password.isNotBlank()) { "Password cannot be empty" }

            val firebaseAuth = auth ?: throw IllegalStateException("Firebase is not initialized")
            val authResult = firebaseAuth.signInWithEmailAndPassword(cleanEmail, password).awaitTask()
            authResult.user ?: throw IllegalStateException("Failed to sign in: user is null")
        }
    }

    /**
     * Signs in using Google Sign-In via Android Credential Manager and links with Firebase Auth.
     */
    suspend fun signInWithGoogle(
        context: Context,
        customWebClientId: String? = null
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        runCatching {
            val firebaseAuth = auth ?: throw IllegalStateException("Firebase is not initialized")
            val credentialManager = CredentialManager.create(context)
            val webClientId = customWebClientId?.takeIf { it.isNotBlank() } ?: getWebClientId(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val activity = findActivity(context)
            val targetContext = activity ?: context

            val response = try {
                credentialManager.getCredential(context = targetContext, request = request)
            } catch (e: GetCredentialCancellationException) {
                throw IllegalStateException("Google Sign-In was cancelled by the user")
            } catch (e: GetCredentialException) {
                throw IllegalStateException("Google Sign-In: ${e.message ?: "Account selection failed"}. Please verify Web Client ID '$webClientId' or use Email/Password.")
            } catch (e: Exception) {
                throw IllegalStateException("Google Sign-In error: ${e.localizedMessage ?: "Unknown error"}")
            }

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).awaitTask()
                authResult.user ?: throw IllegalStateException("Firebase credential sign-in returned null")
            } else {
                throw IllegalStateException("Unrecognized credential type: ${credential.type}")
            }
        }
    }

    /**
     * Sends password reset email.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val firebaseAuth = auth ?: throw IllegalStateException("Firebase is not initialized")
            require(email.isNotBlank()) { "Please enter your registered email address" }
            firebaseAuth.sendPasswordResetEmail(email.trim()).awaitTask()
            Unit
        }
    }

    /**
     * Signs out of Firebase and clears local Credential Manager state.
     */
    suspend fun signOut(context: Context? = null) = withContext(Dispatchers.IO) {
        try {
            auth?.signOut()
            context?.let {
                val credentialManager = CredentialManager.create(it)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
            _authState.value = AuthState.Unauthenticated
        } catch (_: Exception) {}
    }

    /**
     * Resolves the Web Client ID for Google Sign-In:
     * 1. Checks resources for 'default_web_client_id' generated by Google Services.
     * 2. Fallback to Project 461522556814 OAuth Web Client format.
     */
    fun getWebClientId(context: Context): String {
        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val stringVal = context.getString(resId)
                if (stringVal.isNotBlank()) return stringVal
            }
        } catch (_: Exception) {}
        return "461522556814-suraksha-sathi.apps.googleusercontent.com"
    }

    private fun findActivity(context: Context): Activity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }
}
