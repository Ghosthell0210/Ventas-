package com.example.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.util.ErrorLogger
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun getWebClientId(context: Context): String {
        return try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            "6057501968-c5u69seg443ko382g82di27mbui9ma07.apps.googleusercontent.com"
        }
    }

    fun signInWithGoogle(
        context: Context,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val serverClientId = getWebClientId(context)
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        _isLoading.value = true

        scope.launch(Dispatchers.Main) {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        ErrorLogger.info("Auth", "Inicio de sesión exitoso con Google: ${user.email}")
                        onSuccess(user)
                    } else {
                        onError("No se pudo obtener el usuario de Google")
                    }
                } else {
                    val err = "Tipo de credencial no esperado: ${credential::class.java.name}"
                    ErrorLogger.warn("Auth", err)
                    onError(err)
                }
            } catch (e: GetCredentialCancellationException) {
                ErrorLogger.info("Auth", "Inicio de sesión con Google cancelado por el usuario.")
                // Graceful cancellation, do not alert as an error
            } catch (e: GetCredentialException) {
                ErrorLogger.error("Auth", "Error en CredentialManager: ${e.message}", e)
                onError("Error al conectar con Google: ${e.message}")
            } catch (e: Exception) {
                ErrorLogger.error("Auth", "Error en autenticación Firebase: ${e.message}", e)
                onError("Error al iniciar sesión: ${e.localizedMessage ?: "Error desconocido"}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        ErrorLogger.info("Auth", "Sesión de Google cerrada.")
    }
}
