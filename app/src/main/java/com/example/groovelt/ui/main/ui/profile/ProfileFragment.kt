package com.example.groovelt.ui.main.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.groovelt.data.network.FirebaseHelper
import com.example.groovelt.databinding.FragmentProfileBinding
import com.example.groovelt.ui.auth.AuthActivity
import com.example.groovelt.ui.customview.ImagePickerBottomSheetFragment
import com.example.groovelt.utils.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale


class ProfileFragment : Fragment() {
    private lateinit var progressDialog: AlertDialog

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val firebaseHelper: FirebaseHelper by lazy {
        FirebaseHelper.getInstance()
    }

    private val userId by lazy {
        Constants.getUserId(requireContext())
    }


    private lateinit var imagePathLocation: String

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            (result.data?.data as Uri).let { uri ->
                showLoadingDialog()

                val base64 = fileToBase64(reduceFileSize(uri.toFile()))
                firebaseHelper.userPathDatabase.child(userId ?: "")
                    .child("profileImage").setValue(base64)
                    .addOnSuccessListener {
                        showToast("Successfully Changing Profile Image!")
                    }
                    .addOnFailureListener { exception ->
                        hideLoadingDialog()
                        showToast("Error: ${exception.localizedMessage}")
                    }
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            File(imagePathLocation).let { image ->
                showLoadingDialog()

                rotateImage(BitmapFactory.decodeFile(image.path), imagePathLocation).compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    FileOutputStream(image)
                )

                val base64 = fileToBase64(reduceFileSize(image))
                firebaseHelper.userPathDatabase.child(userId ?: "")
                    .child("profileImage").setValue(base64)
                    .addOnSuccessListener {
                        showToast("Successfully Changing Profile Image!")
                    }
                    .addOnFailureListener { exception ->
                        hideLoadingDialog()
                        showToast("Error: ${exception.localizedMessage}")
                    }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(com.example.groovelt.R.layout.progress_dialog)
        builder.setCancelable(false)
        progressDialog = builder.create()
        showLoadingDialog()

        observeProfile()
        setViews()

        return binding.root
    }

    private fun observeProfile() {
        firebaseHelper.userPathDatabase.child(userId ?: "").child("profileImage")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hideLoadingDialog()
                    val base64 = snapshot.getValue(String::class.java)
                    val bitmap = base64ToBitmap(base64 ?: "")
                    binding.ivProfile.setImageBitmap(bitmap)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        firebaseHelper.userPathDatabase.child(userId ?: "").child("name")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hideLoadingDialog()
                    val name = snapshot.getValue(String::class.java)
                    binding.tvName.text = name
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        firebaseHelper.userPathDatabase.child(userId ?: "").child("email")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = snapshot.getValue(String::class.java)
                    binding.tvEmail.text = email
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setViews() {
        binding.apply {
            btnChangeImage.setOnClickListener {
                openImagePicker()
            }

            btnChangeName.setOnClickListener {
                openChangeNameDialog()
            }

            btnLogout.setOnClickListener {
                Constants.clearSharedPreferences(requireContext())
                requireActivity().apply {
                    finishAffinity()
                    startActivity(Intent(requireActivity(), AuthActivity::class.java))
                }
            }
        }
    }

    private fun openImagePicker() {
        val bottomSheetFragment = ImagePickerBottomSheetFragment()
        bottomSheetFragment.setOnButtonClickListener(object :
            ImagePickerBottomSheetFragment.OnImagePickerClickListener {
            @SuppressLint("QueryPermissionsNeeded")
            override fun cameraClick() {
                if (checkImagePermission()) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.resolveActivity(requireActivity().packageManager)
                    val storageDir: File? =
                        requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val customTempFile = File.createTempFile(
                        SimpleDateFormat(
                            "dd-MMM-yyyy",
                            Locale.US
                        ).format(System.currentTimeMillis()), ".jpg", storageDir
                    )
                    customTempFile.also {
                        imagePathLocation = it.absolutePath
                        intent.putExtra(
                            MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                                requireContext(),
                                requireActivity().application.packageName,
                                it
                            )
                        )
                        cameraLauncher.launch(intent)
                    }
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        REQUIRED_CAMERA_PERMISSION,
                        REQUEST_CODE_PERMISSIONS
                    )
                }
            }

            override fun galleryClick() {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                val chooser = Intent.createChooser(intent, "Pick an Image")
                galleryLauncher.launch(chooser)
            }
        })

        bottomSheetFragment.show(parentFragmentManager, "ReceiptBottomSheet")
    }

    private fun openChangeNameDialog() {
        val inflater = layoutInflater
        val dialogView: View =
            inflater.inflate(com.example.groovelt.R.layout.dialog_change_username, null)

        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        val input =
            dialogView.findViewById<TextInputEditText>(com.example.groovelt.R.id.deckNameInput)
        val btnSave = dialogView.findViewById<MaterialButton>(com.example.groovelt.R.id.btn_save)
        val btnCancel =
            dialogView.findViewById<MaterialButton>(com.example.groovelt.R.id.btn_cancel)

        btnSave.setOnClickListener { view: View? ->
            if (input.text.isNullOrEmpty()) {
                showToast("Fill name correctly!")
            } else {
                dialog.dismiss()
                showLoadingDialog()
                firebaseHelper.userPathDatabase.child(userId ?: "")
                    .child("name").setValue(input.text.toString())
                    .addOnSuccessListener {
                        showToast("Successfully Change Name!")
                    }
                    .addOnFailureListener { exception ->
                        hideLoadingDialog()
                        showToast("Error: ${exception.localizedMessage}")
                    }
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun rotateImage(bitmap: Bitmap, path: String): Bitmap {
        val matrix = Matrix()
        when (ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    private fun Uri.toFile(): File {
        val tempFile = File.createTempFile(
            "IMG_${System.currentTimeMillis()}_",
            ".jpg",
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        requireActivity().contentResolver.openInputStream(this)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private fun reduceFileSize(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > 1000000)

        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    private fun fileToBase64(file: File): String {
        val bytes = file.readBytes()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun base64ToBitmap(base64: String): Bitmap? {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT) // Mendekode Base64 ke byte array
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun showLoadingDialog() {
        progressDialog.show()
    }

    private fun hideLoadingDialog() {
        progressDialog.dismiss()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun checkImagePermission() = REQUIRED_CAMERA_PERMISSION.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 100
    }
}