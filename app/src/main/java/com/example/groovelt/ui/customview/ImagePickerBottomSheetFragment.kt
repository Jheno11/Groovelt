package com.example.groovelt.ui.customview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.groovelt.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImagePickerBottomSheetFragment : BottomSheetDialogFragment() {

    private var listener: OnImagePickerClickListener? = null

    interface OnImagePickerClickListener {
        fun cameraClick()
        fun galleryClick()
    }

    fun setOnButtonClickListener(listener: OnImagePickerClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.receipt_bottom_sheet_fragment, container, false)

        view.findViewById<View>(R.id.btn_camera).setOnClickListener {
            listener?.cameraClick()
            dismiss()
        }

        view.findViewById<View>(R.id.btn_gallery).setOnClickListener {
            listener?.galleryClick()
            dismiss()
        }

        return view
    }
}
