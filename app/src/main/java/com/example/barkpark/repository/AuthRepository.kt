package com.example.barkpark.repository

import com.example.barkpark.model.DogItem
import com.example.barkpark.model.User
import com.example.barkpark.util.Resource

interface AuthRepository {

    suspend fun currentUser() : Resource<User>

    suspend fun login(Username:String,
                      Password:String) : Resource<User>

    suspend fun createUser(Username:String,
                           Password:String,
                           Email:String,
                           Name:String) : Resource<User>

    suspend fun getUid() : Resource<String>

    suspend fun getUserEmail(emailAddress: String) : Resource<List<DogItem>>

    suspend fun sendPasswordResetEmail(emailAddress: String)

    fun logOut()
}