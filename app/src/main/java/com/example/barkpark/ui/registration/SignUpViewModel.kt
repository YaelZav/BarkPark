package com.example.barkpark.ui.registration


import android.util.Log
import android.util.Patterns
import androidx.lifecycle.*
import com.example.barkpark.model.User
import com.example.barkpark.repository.AuthRepository
import com.example.barkpark.repository.FirestorageRepository
import com.example.barkpark.util.Resource
import kotlinx.coroutines.launch

class SignUpViewModel(private val repository:AuthRepository,private val FSrepository:FirestorageRepository) : ViewModel() {

    private val _userRegistrationStatus = MutableLiveData<Resource<User>>()
    val userRegistrationStatus: LiveData<Resource<User>> = _userRegistrationStatus


    fun createUser(
        email: String,
        password: String,
        username: String,
        phone: String,
        data: ByteArray
    ) : String {
        var error = if(email.isEmpty() || username.isEmpty() || password.isEmpty() || phone.isEmpty())
            "Empty values"
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Not a valid email"
        }else "creating user"
        error.let{
            _userRegistrationStatus.postValue(Resource.Error(it))
        }
        if (error == "creating user") {
            _userRegistrationStatus.postValue(Resource.Loading())
            viewModelScope.launch {
                val registrationResult = repository.createUser(email, password, username, phone)
                _userRegistrationStatus.postValue(registrationResult)
                registrationResult.data.toString()?.let { Log.d("testSUVM1", it) }
                FSrepository.uploadUserImage(data, registrationResult.data!!.id!!)
            }
        }
        return error
    }

    class RegisterViewModelFactory(private val repo: AuthRepository,private val FSrepository:FirestorageRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignUpViewModel(repo,FSrepository) as T
        }
    }
}