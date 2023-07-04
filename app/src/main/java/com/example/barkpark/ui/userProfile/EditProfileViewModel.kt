package com.example.barkpark.ui.userProfile


import androidx.lifecycle.*
import com.example.barkpark.model.User
import com.example.barkpark.repository.AuthRepository
import com.example.barkpark.repository.FirestorageRepository
import com.example.barkpark.repository.FirestoreRepository
import com.example.barkpark.util.Resource
import kotlinx.coroutines.launch

class EditProfileViewModel(private val authRep:AuthRepository, val FSRepository: FirestoreRepository, val storageRep: FirestorageRepository) : ViewModel() {

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    fun updateUsername(newName: String) {
        viewModelScope.launch {
            if(newName == "")
                _currentUser.postValue(Resource.Error("Empty username"))
            else {
                _currentUser.postValue(Resource.Loading())
                authRep.getUid().data?.let { FSRepository.updateUsername(it, newName) }
                _currentUser.postValue(authRep.getUid().data?.let { FSRepository.getUser(it) })
            }
        }
    }

    fun updatePhone(newPhone: String) {

        viewModelScope.launch {
            if(newPhone == "")
                _currentUser.postValue(Resource.Error("Empty number"))
            else {
                _currentUser.postValue(Resource.Loading())
                authRep.getUid().data?.let { FSRepository.updatePhone(it, newPhone) }
                _currentUser.postValue(authRep.getUid().data?.let { FSRepository.getUser(it) })
            }
        }
    }

    fun updatePicture(newPic: ByteArray?) {

        viewModelScope.launch {
            if(newPic == null)
                _currentUser.postValue((Resource.Error("Empty picture")))
            else {
                _currentUser.postValue((Resource.Loading()))
                authRep.getUid().data?.let { storageRep.uploadUserImage(newPic, it) }
                _currentUser.postValue(authRep.getUid().data?.let { FSRepository.getUser(it) })
            }
        }
    }




    class EditProfileViewModelFactory(val authRepo: AuthRepository, val FSRepository: FirestoreRepository, val storageRep: FirestorageRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditProfileViewModel(authRepo, FSRepository, storageRep) as T
        }
    }
}