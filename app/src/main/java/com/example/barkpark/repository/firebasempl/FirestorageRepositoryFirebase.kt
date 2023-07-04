package com.example.barkpark.repository.firebasempl

import android.net.Uri
import android.util.Log
import com.example.barkpark.repository.FirestorageRepository
import com.example.barkpark.util.Resource
import com.example.barkpark.util.safeCall
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File


class FirestorageRepositoryFirebase : FirestorageRepository {


    var storageRef = FirebaseStorage.getInstance().reference


    override suspend fun uploadUserImage(newPic: ByteArray , userId: String) = withContext(Dispatchers.IO) {
        safeCall {

            val pictureRef = storageRef.child("images/users/${userId}")
            var uploadTask = pictureRef.putBytes(newPic)
            Resource.Success(uploadTask)
        }
    }

    override suspend fun uploadDogImage(newPic: ByteArray, dogId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val pictureRef = storageRef.child("images/dogs/${dogId}")
            var uploadTask = pictureRef.putBytes(newPic)
            Resource.Success(uploadTask)
        }
    }

    override suspend fun downloadUserImage(userId: String): Resource<ByteArray> = withContext(Dispatchers.IO) {
        safeCall {
            val pictureRef = storageRef.child("images/users/${userId}")
            val maxsize: Long = 1024 * 1024 * 5
            val test = pictureRef.getBytes(maxsize).await()
            Resource.Success(test)

        }
    }

    override suspend fun deleteUserImage(userId: String): Unit = withContext(Dispatchers.IO) {
        safeCall {
            val pictureRef = storageRef.child("images/users/$userId")
            val deleteTask = pictureRef.delete().await()
            Resource.Success(deleteTask)
        }
    }

    override suspend fun deleteDogImage(userId: String): Unit = withContext(Dispatchers.IO) {
        safeCall {
            val pictureRef = storageRef.child("images/dogs/$userId")
            val deleteTask = pictureRef.delete().await()
            Resource.Success(deleteTask)
        }
    }
}



