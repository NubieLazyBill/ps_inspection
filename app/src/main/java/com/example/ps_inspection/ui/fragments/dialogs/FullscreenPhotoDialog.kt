package com.example.ps_inspection.ui.fragments.dialogs

import android.R
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.github.chrisbanes.photoview.PhotoView

class FullscreenPhotoDialog : DialogFragment() {

    private var photoPath: String = ""

    companion object {
        fun newInstance(photoPath: String): FullscreenPhotoDialog {
            val fragment = FullscreenPhotoDialog()
            val args = Bundle()
            args.putString("photo_path", photoPath)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Black_NoTitleBar_Fullscreen)
        photoPath = arguments?.getString("photo_path") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val photoView = PhotoView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageBitmap(BitmapFactory.decodeFile(photoPath))
            setOnClickListener {
                dismiss()
            }
        }
        return photoView
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}