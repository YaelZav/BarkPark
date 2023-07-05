package com.example.barkpark.ui.login

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.barkpark.R
import com.example.barkpark.databinding.LoginLayoutBinding
import com.example.barkpark.repository.firebasempl.AuthRepositoryFirebase
import com.example.barkpark.util.Resource
import com.example.barkpark.util.autoCleared
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var binding : LoginLayoutBinding? = null // by autoCleared()
    private val viewModel : LoginViewModel by viewModels {
        LoginViewModel.LoginViewModelFactory(AuthRepositoryFirebase())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoginLayoutBinding.inflate(inflater,container,false)
        val fixedBinding = binding!!
        fixedBinding.registerBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        fixedBinding.loginBtn.setOnClickListener {

            viewModel.signInUser(fixedBinding.emailEt?.text.toString(),
                fixedBinding.passwordEt?.text.toString())
        }

        fixedBinding.forgotPasswordBtn.setOnClickListener {
            viewModel.viewModelScope.launch {
                if (fixedBinding.emailEt.text.isEmpty())
                    Toast.makeText(requireContext(), "Required Email", Toast.LENGTH_SHORT).show()
                else if (!Patterns.EMAIL_ADDRESS.matcher(fixedBinding.emailEt.text).matches())
                    Toast.makeText(requireContext(), "Email is badly formatted", Toast.LENGTH_SHORT).show()
                else {
                    viewModel.forgotPassword(fixedBinding.emailEt.text.toString())
                    delay(1000)
                    Toast.makeText(requireContext(), "Password sent to email", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return fixedBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userSignInStatus.observe(viewLifecycleOwner) {

            when(it){
                is Resource.Loading -> {
                    binding!!.loginProgress.isVisible = true
                    binding!!.loginBtn.isEnabled = false
                }
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                }
                is Resource.Error -> {
                    binding!!.loginProgress.isVisible = false
                    binding!!.loginBtn.isEnabled = true
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) {

            when(it){
                is Resource.Loading -> {
                    binding!!.loginProgress.isVisible = true
                    binding!!.loginBtn.isEnabled = false
                }
                is Resource.Success -> {
                    findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                }
                is Resource.Error -> {
                    binding!!.loginProgress.isVisible = false
                    binding!!.loginBtn.isEnabled = true
                }
            }
        }
    }
}