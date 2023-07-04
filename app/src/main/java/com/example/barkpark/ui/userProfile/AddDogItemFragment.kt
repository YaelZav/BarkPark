package com.example.barkpark.ui.userProfile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.barkpark.R
import com.example.barkpark.databinding.AddDogItemBinding
import com.example.barkpark.model.User
import com.example.barkpark.repository.firebasempl.AuthRepositoryFirebase
import com.example.barkpark.repository.firebasempl.FireStoreRepositoryFirebase
import com.example.barkpark.util.Resource
import com.example.barkpark.util.autoCleared
import com.example.barkpark.model.DogItem
import com.example.barkpark.model.ItemManager
import com.example.barkpark.repository.firebasempl.FirestorageRepositoryFirebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class AddDogItemFragment : Fragment() {

    private var binding : AddDogItemBinding by autoCleared()

    private val viewModel : AddDogItemViewModel by viewModels {
        AddDogItemViewModel.AddDogItemViewModelFactory(AuthRepositoryFirebase(), FireStoreRepositoryFirebase(),FirestorageRepositoryFirebase())
    }

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    private var imageUri : Uri? = null

    val pickImageLauncher : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            binding.addedImage.setImageURI(it)
            if (it != null) {
                requireActivity().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            imageUri = it
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = AddDogItemBinding.inflate(inflater, container, false)
        binding.progressAddDogItem.isVisible = false
        binding.addDogFinish.setOnClickListener {

            if (binding.dogAgeRadioG.checkedRadioButtonId == -1 || binding.dogGenderRadioG.checkedRadioButtonId == -1) {
                Error("Must choose options")
            }


            var dogAge = if (binding.puppyRadio.isChecked)
                "puppy"
            else if (binding.adultRadio.isChecked)
                "adult"
            else if (binding.oldRadio.isChecked)
                "old"
            else
                "Not Picked"

            var dogGender = if (binding.maleRadio.isChecked)
                "male"
            else if (binding.femaleRadio.isChecked)
                "female"
            else
                "Not Picked"


            val item = DogItem(imageUri.toString(),
                binding.dogNameET.text.toString(),
                binding.dogBreedET.text.toString(),
                dogAge,
                dogGender)



            if(binding.addedImage.drawable != null) {
                binding.addedImage.isDrawingCacheEnabled = true
                binding.addedImage.buildDrawingCache()
                val bitmap = (binding.addedImage.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                ItemManager.add(item)
                viewModel.addDogWithImage(item,data)
            }
            else{
                ItemManager.add(item)
                viewModel.addDog(item)
            }

            lifecycleScope.launch {
                delay(1200)
                findNavController().navigate(R.id.action_addDogItemFragment_to_profileFragment)
            }

        }

        binding.addDogImage.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        binding.addDogCancel.setOnClickListener {
            findNavController().navigate(R.id.action_addDogItemFragment_to_profileFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addDogStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Resource.Loading -> {
                    binding.progressAddDogItem.isVisible = true
                    binding.addDogFinish.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressAddDogItem.isVisible = false
                    binding.addDogFinish.isEnabled = true

                }
                is Resource.Error -> {
                    binding.progressAddDogItem.isVisible = false
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.deleteDogStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Resource.Loading -> {
                    binding.progressAddDogItem.isVisible = true
                }
                is Resource.Success -> {
                    binding.progressAddDogItem.isVisible = false
                }
                is Resource.Error -> {
                    binding.progressAddDogItem.isVisible = false
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}