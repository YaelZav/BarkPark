package com.example.barkpark.repository.firebasempl

import android.util.Log
import com.example.barkpark.model.DogItem
import com.example.barkpark.model.User
import com.example.barkpark.repository.AuthRepository
import com.example.barkpark.util.Resource
import com.example.barkpark.util.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepositoryFirebase : AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val userRef = FirebaseFirestore.getInstance().collection("users")

    override suspend fun currentUser(): Resource<User> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val user = userRef.document(firebaseAuth.currentUser!!.uid).get().await()
                    .toObject(User::class.java)
                Resource.Success(user!!)
            }
        }
    }


    override suspend fun login(Email: String, Password: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val result = firebaseAuth.signInWithEmailAndPassword(Email, Password).await()
                val user = userRef.document(result.user?.uid!!).get().await().toObject(User::class.java)!!
                Resource.Success(user)
            }
        }
    }

    override suspend fun createUser(
        Email: String,
        Password: String,
        Username: String,
        Phone: String,
    ) : Resource<User> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val registrationResult = firebaseAuth.createUserWithEmailAndPassword(Email, Password).await()
                val userId = registrationResult.user?.uid!!
                val newUser = User(Username, Email, Phone, userId)
                userRef.document(userId).set(newUser)
                Resource.Success(newUser)

            }
        }
    }

    override suspend fun getUid() : Resource<String> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val user = firebaseAuth.currentUser!!.uid
                Resource.Success(user!!)
            }
        }
    }

    override suspend fun getUserEmail(emailAddress: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = userRef.whereEqualTo("ownerId", emailAddress).get().await()
            val user = result.toObjects(DogItem::class.java)
            Resource.Success(user!!)
        }
    }

    override suspend fun sendPasswordResetEmail(emailAddress: String) {
        return withContext(Dispatchers.IO) {

            firebaseAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Send password to mail", "Email sent.")
                    } else {
                        Log.d("Can't send password", "Email not sent.")
                    }
                }
        }
    }

    override fun logOut() {
        firebaseAuth.signOut()
    }
}


