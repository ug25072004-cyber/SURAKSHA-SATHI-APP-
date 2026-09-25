package com.example.data.firebase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseStorageService(
    private val storageInstance: FirebaseStorage? = null
) {

    val defaultBucket: String = "suraksha-sathi-app-ebb0b.firebasestorage.app"

    val storage: FirebaseStorage?
        get() = storageInstance ?: try {
            FirebaseStorage.getInstance()
        } catch (_: Exception) {
            try {
                FirebaseStorage.getInstance("gs://suraksha-sathi-app-ebb0b.firebasestorage.app")
            } catch (_: Exception) {
                null
            }
        }

    /**
     * Uploads a file from content Uri to Firebase Storage with reactive progress updates.
     */
    fun uploadFileWithProgress(
        context: Context,
        fileUri: Uri,
        folder: String = "incidents/evidence",
        customName: String? = null
    ): Flow<StorageUploadState> = callbackFlow {
        val storageClient = storage
        if (storageClient == null) {
            trySend(StorageUploadState.Error("Firebase Storage is not initialized"))
            close()
            return@callbackFlow
        }

        val fileName = customName ?: getFileNameFromUri(context, fileUri) ?: "upload_${System.currentTimeMillis()}.jpg"
        val mimeType = context.contentResolver.getType(fileUri) ?: "image/jpeg"
        val safeFolder = folder.trim('/')
        val fileRef: StorageReference = storageClient.reference.child("$safeFolder/$fileName")

        trySend(StorageUploadState.Uploading(progress = 0.05f, fileName = fileName))

        val metadata = StorageMetadata.Builder()
            .setContentType(mimeType)
            .setCustomMetadata("uploadedBy", "SurakshaSathiFieldApp")
            .setCustomMetadata("timestamp", System.currentTimeMillis().toString())
            .build()

        val uploadTask = fileRef.putFile(fileUri, metadata)

        uploadTask.addOnProgressListener { snapshot ->
            if (snapshot.totalByteCount > 0) {
                val progress = (snapshot.bytesTransferred.toFloat() / snapshot.totalByteCount.toFloat())
                    .coerceIn(0.05f, 0.95f)
                trySend(StorageUploadState.Uploading(progress = progress, fileName = fileName))
            }
        }.addOnSuccessListener { taskSnapshot ->
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                val result = StorageUploadResult(
                    downloadUrl = uri.toString(),
                    storagePath = fileRef.path,
                    fileName = fileName,
                    sizeBytes = taskSnapshot.totalByteCount
                )
                trySend(StorageUploadState.Success(result))
                close()
            }.addOnFailureListener { error ->
                trySend(StorageUploadState.Error("Upload finished but failed to retrieve download URL: ${error.localizedMessage}"))
                close()
            }
        }.addOnFailureListener { exception ->
            val errorMsg = when {
                exception.message?.contains("User does not have permission", ignoreCase = true) == true ->
                    "Firebase Storage security rules rejected upload. In production mode, please sign in first."
                exception.message?.contains("Object does not exist", ignoreCase = true) == true ->
                    "Storage bucket not accessible: $defaultBucket"
                else -> "Upload error: ${exception.localizedMessage ?: exception.javaClass.simpleName}"
            }
            trySend(StorageUploadState.Error(errorMsg))
            close(exception)
        }

        awaitClose {
            if (uploadTask.isInProgress) {
                uploadTask.cancel()
            }
        }
    }

    /**
     * Uploads in-memory byte array (e.g. compressed bitmap or generated dispatch PDF).
     */
    suspend fun uploadBytes(
        bytes: ByteArray,
        folder: String = "evidence",
        fileName: String = "evidence_${UUID.randomUUID().toString().take(8)}.jpg",
        contentType: String = "image/jpeg"
    ): Result<StorageUploadResult> = withContext(Dispatchers.IO) {
        runCatching {
            val storageClient = storage ?: throw IllegalStateException("Firebase Storage is not initialized")
            val safeFolder = folder.trim('/')
            val fileRef = storageClient.reference.child("$safeFolder/$fileName")
            val metadata = StorageMetadata.Builder()
                .setContentType(contentType)
                .setCustomMetadata("uploadedBy", "SurakshaSathiCrisisCore")
                .build()

            fileRef.putBytes(bytes, metadata).awaitTask()
            val downloadUrl = fileRef.downloadUrl.awaitTask().toString()
            StorageUploadResult(
                downloadUrl = downloadUrl,
                storagePath = fileRef.path,
                fileName = fileName,
                sizeBytes = bytes.size.toLong()
            )
        }
    }

    /**
     * Lists existing files from a folder in Firebase Storage bucket.
     */
    suspend fun listFolderFiles(folder: String = "incidents/evidence"): Result<List<StorageFileItem>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val storageClient = storage ?: return@withContext Result.success(emptyList())
                val folderRef = storageClient.reference.child(folder.trim('/'))
                val listResult = folderRef.listAll().awaitTask()
                val items = mutableListOf<StorageFileItem>()

                for (itemRef in listResult.items) {
                    try {
                        val downloadUrl = itemRef.downloadUrl.awaitTask().toString()
                        val metadata = itemRef.metadata.awaitTask()
                        items.add(
                            StorageFileItem(
                                name = itemRef.name,
                                path = itemRef.path,
                                downloadUrl = downloadUrl,
                                sizeBytes = metadata.sizeBytes,
                                updatedTimestamp = metadata.updatedTimeMillis,
                                contentType = metadata.contentType ?: "image/jpeg"
                            )
                        )
                    } catch (_: Exception) {
                        // Continue parsing remaining items even if one metadata lookup fails
                    }
                }
                items.sortedByDescending { it.updatedTimestamp }
            }
        }

    /**
     * Deletes a file from Firebase Storage.
     */
    suspend fun deleteFile(storagePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val storageClient = storage ?: return@withContext Result.success(Unit)
            storageClient.reference.child(storagePath.removePrefix("/")).delete().awaitTask()
            Unit
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
