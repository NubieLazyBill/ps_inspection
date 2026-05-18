package com.example.ps_inspection.ui.fragments.dialogs

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.ps_inspection.R
import com.github.chrisbanes.photoview.PhotoView

class FullscreenPhotoDialog : DialogFragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tvCounter: TextView

    private var photoPaths: List<String> = emptyList()
    private var startPosition: Int = 0

    companion object {
        private const val ARG_PHOTO_PATHS = "photo_paths"
        private const val ARG_START_POSITION = "start_position"

        fun newInstance(photoPath: String): FullscreenPhotoDialog {
            val fragment = FullscreenPhotoDialog()
            val args = Bundle()
            args.putStringArrayList(ARG_PHOTO_PATHS, arrayListOf(photoPath))
            args.putInt(ARG_START_POSITION, 0)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(photoPaths: List<String>, startPosition: Int): FullscreenPhotoDialog {
            val fragment = FullscreenPhotoDialog()
            val args = Bundle()
            args.putStringArrayList(ARG_PHOTO_PATHS, ArrayList(photoPaths))
            args.putInt(ARG_START_POSITION, startPosition)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        photoPaths = arguments?.getStringArrayList(ARG_PHOTO_PATHS) ?: emptyList()
        startPosition = arguments?.getInt(ARG_START_POSITION, 0) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.dialog_fullscreen_photo_pager, container, false)

        viewPager = root.findViewById(R.id.viewPager)
        tvCounter = root.findViewById(R.id.tvCounter)

        val adapter = PhotoPagerAdapter(photoPaths)
        viewPager.adapter = adapter

        if (photoPaths.isNotEmpty() && startPosition in photoPaths.indices) {
            viewPager.setCurrentItem(startPosition, false)
        }

        updateCounter(viewPager.currentItem + 1)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateCounter(position + 1)
            }
        })

        return root
    }

    private fun updateCounter(current: Int) {
        tvCounter.text = "$current / ${photoPaths.size}"
        tvCounter.visibility = if (photoPaths.size > 1) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    inner class PhotoPagerAdapter(private val paths: List<String>) :
        RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val photoView = PhotoView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                // Включаем возможность увеличения
                setZoomable(true)
                setMaximumScale(5.0f)
                setMediumScale(2.0f)
                setMinimumScale(1.0f)
                setOnClickListener {
                    dismiss()
                }
            }
            return PhotoViewHolder(photoView)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val path = paths[position]
            val bitmap = BitmapFactory.decodeFile(path)
            holder.photoView.setImageBitmap(bitmap)
            // Сбрасываем масштаб при переключении (через setScale)
            holder.photoView.setScale(1.0f, false)
        }

        override fun getItemCount(): Int = paths.size

        inner class PhotoViewHolder(val photoView: PhotoView) : RecyclerView.ViewHolder(photoView)
    }
}