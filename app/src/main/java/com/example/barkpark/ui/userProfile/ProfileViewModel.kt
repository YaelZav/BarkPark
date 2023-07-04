package com.example.barkpark.ui.userProfile


import androidx.lifecycle.*
import com.example.barkpark.model.DogItem
import com.example.barkpark.model.User
import com.example.barkpark.repository.AuthRepository
import com.example.barkpark.repository.FirestorageRepository
import com.example.barkpark.repository.FirestoreRepository
import com.example.barkpark.util.Resource
import kotlinx.coroutines.launch

class ProfileViewModel(private val authRep:AuthRepository, val FSRepository: FirestoreRepository, val storageRep: FirestorageRepository) : ViewModel() {

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    private val _dogStatus : MutableLiveData<Resource<List<DogItem>>> = MutableLiveData()
    val dogStatus : LiveData<Resource<List<DogItem>>> = _dogStatus

    private val _addDogStatus = MutableLiveData<Resource<Void>>()
    val addDogStatus : LiveData<Resource<Void>> = _addDogStatus

    private val _deleteDogStatus =  MutableLiveData<Resource<Void>>()
    val deleteDogStatus : LiveData<Resource<Void>> = _deleteDogStatus

    private val _curUserMap =  MutableLiveData<Resource<ByteArray>>()
    val curUserMap : LiveData<Resource<ByteArray>> = _curUserMap

    init {

        viewModelScope.launch {
            _currentUser.postValue(Resource.Loading())
            _currentUser.postValue(authRep.currentUser())
        }

        viewModelScope.launch {
            _dogStatus.postValue(Resource.Loading())
            _dogStatus.postValue(authRep.getUid().data?.let { FSRepository.getDogsByOwnerId(it) })
        }

        viewModelScope.launch {
            _curUserMap.postValue(Resource(null))
            getProfilPic()
        }
    }

    fun signOut() {
        authRep.logOut()
    }

    fun deleteDog(dogId:String) {
        viewModelScope.launch {
            if(dogId.isEmpty())
                _deleteDogStatus.postValue(Resource.Error("Empty dog id"))
            else {
                _deleteDogStatus.postValue(Resource.Loading())
                _deleteDogStatus.postValue(FSRepository.deleteDog(dogId))
            }
        }
    }

    fun deleteDogpic(dogId:String) {
        viewModelScope.launch {
            if(dogId.isEmpty())
                _deleteDogStatus.postValue(Resource.Error("Empty dog id"))
            else {
                storageRep.deleteDogImage(dogId)
                _deleteDogStatus.postValue(Resource.Loading())
                _deleteDogStatus.postValue(FSRepository.deleteDog(dogId))
            }
        }
    }

    fun getDogs(ownerId: String) {
        viewModelScope.launch {
            if(ownerId.isEmpty())
                _dogStatus.postValue(Resource.Error("Empty dog id"))
            else {
                _dogStatus.postValue(Resource.Loading())
                _dogStatus.postValue(FSRepository.getDogsByOwnerId(ownerId))
            }
        }
    }

    suspend fun getProfilPic() {
        viewModelScope.launch {
            val curbitmap = storageRep.downloadUserImage(authRep.getUid().data!!)
            if(curbitmap.data!!.isEmpty()) {
                _curUserMap.postValue(Resource.Error("no user id"))
            }
            else {
                _curUserMap.postValue(Resource.Loading())
                _curUserMap.postValue(curbitmap)
            }
        }
    }

    class ProfileViewModelFactory(val authRepo:AuthRepository, val FSRepository: FirestoreRepository,val storageRep: FirestorageRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(authRepo, FSRepository, storageRep) as T
        }
    }
}