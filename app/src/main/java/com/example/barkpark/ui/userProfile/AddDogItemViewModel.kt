package com.example.barkpark.ui.userProfile


import androidx.lifecycle.*
import com.example.barkpark.model.User
import com.example.barkpark.repository.AuthRepository
import com.example.barkpark.repository.FirestoreRepository
import com.example.barkpark.util.Resource
import kotlinx.coroutines.launch
import com.example.barkpark.model.DogItem
import com.example.barkpark.repository.FirestorageRepository


class AddDogItemViewModel(private val authRep: AuthRepository, val FSRepository: FirestoreRepository,val storageRep: FirestorageRepository) : ViewModel() {

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    val _dogStatus : MutableLiveData<Resource<List<DogItem>>> = MutableLiveData()
    val dogStatus : LiveData<Resource<List<DogItem>>> = _dogStatus

    private val _addDogStatus = MutableLiveData<Resource<DogItem>>()
    val addDogStatus : LiveData<Resource<DogItem>> = _addDogStatus

    private val _deleteDogStatus =  MutableLiveData<Resource<Void>>()
    val deleteDogStatus : LiveData<Resource<Void>> = _deleteDogStatus

    init {
        FSRepository.getDogsLiveData(_dogStatus)
    }

    fun addDog(dog: DogItem) {
        viewModelScope.launch {
            authRep.getUid().data?.let { dog.OwnerId=it }
            if(dog.OwnerId=="")
                _addDogStatus.postValue(Resource.Error("Empty id"))
            else {
                _addDogStatus.postValue(Resource.Loading())
                _addDogStatus.postValue(FSRepository.addDog(dog))
            }
        }
    }


    fun addDogWithImage(dog: DogItem,newPic: ByteArray?) {
        viewModelScope.launch {
            authRep.getUid().data?.let { dog.OwnerId=it }
            if(dog.OwnerId=="")
                _addDogStatus.postValue(Resource.Error("Empty id"))
            else {
                _addDogStatus.postValue(Resource.Loading())
                val dogid = FSRepository.addDog(dog).data!!.Id
                if (newPic != null) {
                    storageRep.uploadDogImage(newPic, dogid)
                }
                _addDogStatus.postValue(_addDogStatus.value)
            }
        }
    }

    fun updatePicture(newPic: ByteArray?) {

        viewModelScope.launch {
            if(newPic == null)
                _currentUser.postValue((Resource.Error("Empty picture")))
            else {
                _currentUser.postValue((Resource.Loading()))
                authRep.getUid().data?.let { storageRep.uploadDogImage(newPic, it) }
                _currentUser.postValue(authRep.getUid().data?.let { FSRepository.getUser(it) })
            }
        }
    }

    class AddDogItemViewModelFactory(val authRepo:AuthRepository, val FSRepository: FirestoreRepository,val storageRep: FirestorageRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddDogItemViewModel(authRepo, FSRepository,storageRep) as T
        }
    }
}