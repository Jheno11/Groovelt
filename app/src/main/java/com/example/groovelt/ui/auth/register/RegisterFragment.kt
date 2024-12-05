package com.example.groovelt.ui.auth.register

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
import com.example.groovelt.databinding.FragmentRegisterBinding
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Objects

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
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
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        setListeners()

        return binding.getRoot()
    }

    private fun setListeners() {
        binding.apply {
            btnLogin.setOnClickListener { v ->
                val action =
                    RegisterFragmentDirections.actionNavigationRegisterToNavigationLogin()
                findNavController(v).navigate(action)
            }

            btnRegister.setOnClickListener { v ->
                if (edUsername.text.isNullOrEmpty()) {
                    showToast("Invalid name field!")
                } else if (!Patterns.EMAIL_ADDRESS.matcher(edEmail.text.toString())
                        .matches() || edEmail.text.isNullOrEmpty()
                ) {
                    showToast("Invalid email field!")
                } else if (edPassword.text.isNullOrEmpty()) {
                    showToast("Invalid password field!")
                } else if (edPassword.text.length < 8) {
                    showToast("Password length must at least 8 characters long!")
                } else if (edConfirmPassword.text.isNullOrEmpty()) {
                    showToast("Invalid confirm password field!")
                } else if (edPassword.text.toString() != edConfirmPassword.text.toString()) {
                    showToast("Password and Confirm Password not same!")
                } else {
                    showLoadingDialog()

                    val userModel = HashMap<String, String>()
                    userModel["name"] = edUsername.text.toString()
                    userModel["email"] = edEmail.text.toString()
                    userModel["password"] = edPassword.text.toString()
                    userModel["profileImage"] = ""

                    firebaseAuth.createUserWithEmailAndPassword(
                        edEmail.text.toString(),
                        edPassword.text.toString()
                    )
                        .addOnSuccessListener { authResult: AuthResult ->
                            firebaseHelper.userPathDatabase
                                .child(
                                    Objects.requireNonNull<FirebaseUser?>(authResult.user).uid
                                )
                                .setValue(userModel)
                                .addOnSuccessListener {
                                    hideLoadingDialog()
                                    showToast("Successfully Register User! Please Login.")
                                    val action =
                                        RegisterFragmentDirections.actionNavigationRegisterToNavigationLogin()
                                    findNavController(v).navigate(action)
                                }.addOnFailureListener { e ->
                                    hideLoadingDialog()
                                    showToast("Error : ${e.localizedMessage}")
                                }
                        }.addOnFailureListener { e: Exception ->
                            hideLoadingDialog()
                            showToast("Error : ${e.localizedMessage}")
                        }
                }
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