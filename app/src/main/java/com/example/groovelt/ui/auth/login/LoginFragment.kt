package com.example.groovelt.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import com.example.groovelt.R
import com.example.groovelt.data.network.FirebaseHelper
import com.example.groovelt.databinding.FragmentLoginBinding
import com.example.groovelt.ui.main.MainActivity
import com.example.groovelt.utils.Constants
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var progressDialog: AlertDialog

    private val firebaseHelper: FirebaseHelper by lazy {
        FirebaseHelper.getInstance()
    }

    private val firebaseAuth: FirebaseAuth by lazy {
        firebaseHelper.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        setListeners()

        return binding.getRoot()
    }

    private fun setListeners() {
        binding.apply {
            btnLogin.setOnClickListener {
                if (!Patterns.EMAIL_ADDRESS.matcher(edEmail.text.toString())
                        .matches() || edEmail.text.isNullOrEmpty()
                ) {
                    showToast("Invalid email field!")
                } else if (edPassword.text.isNullOrEmpty()) {
                    showToast("Invalid password field!")
                } else if (edPassword.text.length < 8) {
                    showToast("Password length must at least 8 characters long!")
                } else {
                    showLoadingDialog()

                    firebaseAuth.signInWithEmailAndPassword(
                        edEmail.text.toString(),
                        edPassword.text.toString()
                    )
                        .addOnSuccessListener { authResult: AuthResult ->
                            hideLoadingDialog()
                            firebaseHelper.currentUser = authResult.user
                            Constants.saveUserId(authResult.user?.uid, requireContext())

                            requireActivity().startActivity(
                                Intent(
                                    requireActivity(),
                                    MainActivity::class.java
                                )
                            )
                        }.addOnFailureListener { e: Exception ->
                            hideLoadingDialog()
                            showToast("Failure: ${e.localizedMessage}")
                        }
                }
            }

            btnRegister.setOnClickListener { v ->
                val action =
                    LoginFragmentDirections.actionNavigationLoginToNavigationRegister()
                findNavController(v).navigate(action)
            }
        }
    }

    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(R.layout.progress_dialog)
        builder.setCancelable(false)
        progressDialog = builder.create()
        progressDialog.show()
    }

    private fun hideLoadingDialog() {
        progressDialog.dismiss()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}