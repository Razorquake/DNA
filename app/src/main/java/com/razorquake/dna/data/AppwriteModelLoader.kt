package com.razorquake.dna.data

import android.content.Context
import com.razorquake.dna.BuildConfig
import io.appwrite.Client
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppwriteModelLoader(context: Context) {
    val client = Client(context)
        .setEndpoint("https://cloud.appwrite.io/v1")
        .setProject(BuildConfig.PROJECT_ID)

    val storage = Storage(client)
    private val cacheDir = context.cacheDir

    companion object {
        private const val MAX_CACHE_AGE_HOURS = 24L // Keep files for 24 hours
    }

    fun cleanOldCacheFiles() {
        val thresholdTime = System.currentTimeMillis() - (MAX_CACHE_AGE_HOURS * 60 * 60 * 1000)

        cacheDir.listFiles()?.forEach { file ->
            if (file.lastModified() < thresholdTime) {
                file.delete()
            }
        }
    }

    suspend fun downloadModel(fileName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                cleanOldCacheFiles()
                val cacheFile = File(cacheDir, fileName)
                if (cacheFile.exists()){
                    return@withContext cacheFile
                }
                val result = storage.getFileDownload(
                    bucketId = BuildConfig.BUCKET_ID,
                    fileId = fileName
                )

                cacheFile.outputStream().use { output ->
                    result.inputStream().use {
                        it.copyTo(output)
                    }
                }
                cacheFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}