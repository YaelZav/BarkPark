package com.example.barkpark.repository

import android.net.Uri
import com.example.barkpark.util.Resource
import com.google.firebase.storage.UploadTask

interface FirestorageRepository {

    suspend fun uploadUserImage(image: ByteArray, userId: String) : Resource<UploadTask>

    suspend fun uploadDogImage(image: ByteArray, dogId: String) : Resource<UploadTask>

    suspend fun downloadUserImage(userId: String) : Resource<ByteArray>

    suspend fun deleteUserImage(userId: String)

    suspend fun deleteDogImage(dogId: String)
}