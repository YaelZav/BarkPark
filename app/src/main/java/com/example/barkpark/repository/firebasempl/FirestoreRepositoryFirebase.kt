package com.example.barkpark.repository.firebasempl

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import com.example.barkpark.model.DogItem
import com.example.barkpark.model.User
import com.example.barkpark.repository.FirestoreRepository
import com.example.barkpark.util.Resource
import com.example.barkpark.util.safeCall
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FireStoreRepositoryFirebase : FirestoreRepository {

    private val userRef = FirebaseFirestore.getInstance().collection("users")

    private val dogRef = FirebaseFirestore.getInstance().collection("dogs")

    override suspend fun getUser(id: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = userRef.document(id).get().await()
            val user = result.toObject(User::class.java)
            Resource.Success(user!!)
        }
    }
    override suspend fun getDog(id: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = dogRef.document(id).get().await()
            val user = result.toObject(DogItem::class.java)
            Resource.Success(user!!)
        }
    }

    override suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = userRef.document(userId).delete().await()
            Resource.Success(result)
        }
    }

    override suspend fun updateUsername(userId: String, newName: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = userRef.document(userId).update("name", newName).await()
            Resource.Success(result)
        }
    }

    override suspend fun updatePhone(userId: String, newPhone: String) = withContext(Dispatchers.IO) {
        var error = if(newPhone.isEmpty())
            "Empty number"
        else if (!Patterns.PHONE.matcher(newPhone).matches())
            "Not a valid number"
        else safeCall {
            val result = userRef.document(userId).update("phone", newPhone).await()
            Resource.Success(result)
        }
    }

    override suspend fun addDog(dog: DogItem) = withContext(Dispatchers.IO) {
        safeCall {
            val dogId = dogRef.document().id
            dog.Id = dogId
            dogRef.document(dogId).set(dog).await()
            Resource.Success(dog)
        }
    }

    override suspend fun deleteDog(dogId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = dogRef.document(dogId).delete().await()
            Resource.Success(result)
        }
    }

    override suspend fun getDogsByOwnerId(ownerId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = dogRef.whereEqualTo("ownerId", ownerId).get().await()
            val dogs = result.toObjects(DogItem::class.java)
            Resource.Success(dogs!!)
        }
    }

    override fun getDogsLiveData(data: MutableLiveData<Resource<List<DogItem>>>) {

        data.postValue(Resource.Loading())

        dogRef.orderBy("ownerId").addSnapshotListener { snapshot, e ->
            if(e != null) {
                data.postValue(Resource.Error(e.localizedMessage))
            }
            if(snapshot != null && !snapshot.isEmpty) {
                data.postValue(Resource.Success(snapshot.toObjects(DogItem::class.java)))
            }
            else {
                data.postValue(Resource.Error("No Data"))
            }
        }
    }
}