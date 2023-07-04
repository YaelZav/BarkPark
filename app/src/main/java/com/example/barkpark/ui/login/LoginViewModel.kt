package com.example.barkpark.ui.login

import androidx.lifecycle.*
import com.example.barkpark.model.User
import com.example.barkpark.repository.AuthRepository
import com.example.barkpark.util.Resource
import kotlinx.coroutines.launch

class LoginViewModel(private val authRep:AuthRepository) : ViewModel() {

    private val _userSignInStatus = MutableLiveData<Resource<User>>()
    val userSignInStatus: LiveData<Resource<User>> = _userSignInStatus

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    private val _userCheckEmailstatus = MutableLiveData<Resource<User>>()
    val userCheckEmailstatus: LiveData<Resource<User>> = _userCheckEmailstatus

    init {
        viewModelScope.launch {
            _currentUser.postValue(Resource.Loading())
            _currentUser.postValue(authRep.currentUser())
        }
    }

    fun signInUser(userEmail:String, userPass:String) {
        if(userEmail.isEmpty() || userPass.isEmpty())
            _userSignInStatus.postValue(Resource.Error("Empty email or password"))
        else{
            _userSignInStatus.postValue(Resource.Loading())
            viewModelScope.launch {
                val loginResult = authRep.login(userEmail, userPass)
                _userSignInStatus.postValue(loginResult)
            }
        }
    }


    fun forgotPassword(email: String) {
        viewModelScope.launch {
            val userwithemail = authRep.getUserEmail(email)
            if(userwithemail.data?.isEmpty() == true){

            }
            authRep.sendPasswordResetEmail(email)
        }
    }

    class LoginViewModelFactory(val repo: AuthRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(repo) as T
        }
    }
}