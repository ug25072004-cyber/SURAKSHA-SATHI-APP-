package com.example.data.firebase

import android.net.Uri

data class StorageFileItem(
    val name: String,
    val path: String,
    val downloadUrl: String,
    val sizeBytes: Long = 0L,
    val updatedTimestamp: Long = System.currentTimeMillis(),
    val contentType: String = "image/jpeg"
) {
    val readableSize: String
        get() {
            if (sizeBytes <= 0) return "Unknown"
            val kb = sizeBytes / 1024.0
            return if (kb < 1024) {
                String.format(java.util.Locale.US, "%.1f KB", kb)
            } else {
                String.format(java.util.Locale.US, "%.2f MB", kb / 1024.0)
            }
        }
}

data class StorageUploadResult(
    val downloadUrl: String,
    val storagePath: String,
    val fileName: String,
    val sizeBytes: Long
)

sealed class StorageUploadState {
    data object Idle : StorageUploadState()
    data class Uploading(val progress: Float, val fileName: String) : StorageUploadState()
    data class Success(val result: StorageUploadResult) : StorageUploadState()
    data class Error(val message: String) : StorageUploadState()
}

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Authenticated(
        val uid: String,
        val email: String?,
        val displayName: String?,
        val photoUrl: String?,
        val provider: String,
        val isEmailVerified: Boolean
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Extension function to await Google Play / Firebase Tasks cleanly using coroutines.
 */
suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result) {}
        }
        addOnFailureListener { exception ->
            if (cont.isActive) cont.resumeWith(Result.failure(exception))
        }
        addOnCanceledListener {
            if (cont.isActive) cont.cancel()
        }
    }
