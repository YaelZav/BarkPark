package com.example.barkpark.ui.userProfile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.barkpark.R
import com.example.barkpark.databinding.EditProfileLayoutBinding
import com.example.barkpark.repository.firebasempl.AuthRepositoryFirebase
import com.example.barkpark.repository.firebasempl.FireStoreRepositoryFirebase
import com.example.barkpark.repository.firebasempl.FirestorageRepositoryFirebase
import com.example.barkpark.util.autoCleared
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class EditProfileFragment : Fragment() {

    private var binding: EditProfileLayoutBinding by autoCleared()

    private val viewModel : EditProfileViewModel by viewModels {
        EditProfileViewModel.EditProfileViewModelFactory(AuthRepositoryFirebase(), FireStoreRepositoryFirebase(), FirestorageRepositoryFirebase())
    }

    private var imageUri : Uri? = null

    val pickImageLauncher : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            binding.newUserImage.setImageURI(it)
            if (it != null) {
                requireActivity().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            imageUri = it
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        binding = EditProfileLayoutBinding.inflate(inflater, container, false)

        binding.editProfileCancel.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
        }

        binding.changeUserImage.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        binding.editProfileFinish.setOnClickListener {
            viewModel.viewModelScope.launch {
                if (binding.editUsernameProfile.text?.isEmpty() == false)
                    viewModel.updateUsername(binding.editUsernameProfile.text.toString())

                if (binding.editPhoneProfile.text?.isEmpty() == false)
                    viewModel.updatePhone(binding.editPhoneProfile.text.toString())

                if (binding.newUserImage.drawable != null) {
                    binding.newUserImage.isDrawingCacheEnabled = true
                    binding.newUserImage.buildDrawingCache()
                    val bitmap = (binding.newUserImage.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    viewModel.updatePicture(data)
                }
                delay(2500)
                findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
            }
        }

        return binding.root
    }







}