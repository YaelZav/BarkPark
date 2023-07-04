package com.example.barkpark.repository

import androidx.lifecycle.MutableLiveData
import com.example.barkpark.model.DogItem
import com.example.barkpark.model.User
import com.example.barkpark.util.Resource


interface FirestoreRepository {

    suspend fun getUser(id: String) : Resource<User>

    suspend fun getDog(id: String) : Resource<DogItem>

    suspend fun deleteUser(userId: String) : Resource<Void>

    suspend fun updateUsername(userId: String, newName: String) : Resource<Void>

    suspend fun updatePhone(userId: String, newPhone: String)

    suspend fun addDog(dog: DogItem) : Resource<DogItem>

    suspend fun deleteDog(dogId: String) : Resource<Void>

    suspend fun getDogsByOwnerId(ownerId: String) : Resource<List<DogItem>>

    fun getDogsLiveData(data : MutableLiveData<Resource<List<DogItem>>>)
}