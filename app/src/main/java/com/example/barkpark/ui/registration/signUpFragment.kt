package com.example.barkpark.ui.registration

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.barkpark.R
import com.example.barkpark.databinding.SignUpLayoutBinding
import com.example.barkpark.repository.firebasempl.AuthRepositoryFirebase
import com.example.barkpark.repository.firebasempl.FirestorageRepositoryFirebase
import com.example.barkpark.util.Resource
import com.example.barkpark.util.autoCleared
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


class SignUpFragment : Fragment() {

    private var binding : com.example.barkpark.databinding.SignUpLayoutBinding by autoCleared()
    private val viewModel : SignUpViewModel by viewModels() {
        SignUpViewModel.RegisterViewModelFactory(AuthRepositoryFirebase(),FirestorageRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SignUpLayoutBinding.inflate(inflater, container, false)

        binding.progressSignUp.isVisible = false
        binding.signupBtn.setOnClickListener {

            val bitmap = (binding.dogHead.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val sign_up_result = viewModel.createUser(binding.editEmailSignup.text.toString(),
            binding.editPasswordSignup.text.toString(),
            binding.editUsernameSignup.text.toString(),
            binding.editPhoneSignup.text.toString(),data)
            Toast.makeText(requireContext(), sign_up_result, Toast.LENGTH_SHORT).show()
        }

        val dogHead = binding.dogHead
        val tiltAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.head_move)
        dogHead.startAnimation(tiltAnim)

        binding.signUpGoBack.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userRegistrationStatus.observe(viewLifecycleOwner) {
            viewModel.viewModelScope.launch {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressSignUp.isVisible = true
                        binding.signupBtn.isEnabled = false
                    }
                    is Resource.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Registration successful",
                            Toast.LENGTH_SHORT
                        ).show()
                        delay(2000)
                        findNavController().navigate(R.id.action_signUpFragment_to_profileFragment)
                    }
                    is Resource.Error -> {
                        binding.progressSignUp.isVisible = false
                        binding.signupBtn.isEnabled = true
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}