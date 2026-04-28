package com.example.ps_inspection.ui.fragments.inspections

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.repositories.InspectionMediaManager
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.example.ps_inspection.databinding.FragmentInspectionORU35Binding
import com.example.ps_inspection.ui.fragments.dialogs.CommentsDialogFragment
import com.example.ps_inspection.ui.fragments.dialogs.MediaDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InspectionORU35 : Fragment() {

    private var _binding: FragmentInspectionORU35Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()

    private var isUpdatingUIFromViewModel = false

    private lateinit var mediaManager: InspectionMediaManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectionORU35Binding.inflate(inflater, container, false)
        mediaManager = InspectionMediaManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru35Data.collectLatest { data ->
                updateUIFromData(data)
            }
        }

        // Подписываемся на изменения комментариев
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru35Comments.collectLatest { comments ->
                updateCommentButtonsState(comments)
            }
        }

        setupInputListeners()
        setupMediaButtons()
        updatePhotoButtonsState()
    }

    fun updateCommentButtonsState(comments: Map<String, List<String>>) {
        val buttonKeys = mapOf(
            binding.btnCommentTsn to "ТСН",
            binding.btnCommentTt352 to "ТТ-35 2ТСН",
            binding.btnCommentTt353 to "ТТ-35 3ТСН",
            binding.btnCommentV352 to "В-35 2ТСН",
            binding.btnCommentV353 to "В-35 3ТСН"
        )

        buttonKeys.forEach { (button, key) ->
            val hasComment = comments[key].isNullOrEmpty().not()
            val color = if (hasComment) {
                Color.parseColor("#4CAF50")
            } else {
                Color.parseColor("#9E9E9E")
            }
            button.setColorFilter(color)
        }
    }

    private fun updatePhotoButtonsState() {
        val inspectionId = "current_inspection"
        val photoButtons = mapOf(
            binding.btnMediaTsn to "ТСН",
            binding.btnMediaTt352 to "ТТ-35 2ТСН",
            binding.btnMediaTt353 to "ТТ-35 3ТСН",
            binding.btnMediaV352 to "В-35 2ТСН",
            binding.btnMediaV353 to "В-35 3ТСН"
        )

        photoButtons.forEach { (button, name) ->
            val hasPhotos = mediaManager.hasPhotos(inspectionId, name)
            val color = if (hasPhotos) {
                Color.parseColor("#4CAF50")
            } else {
                Color.parseColor("#9E9E9E")
            }
            button.setColorFilter(color)
        }
    }

    fun refreshPhotoButtonsState() {
        updatePhotoButtonsState()
    }

    private fun setupMediaButtons() {
        val inspectionId = "current_inspection"

        // Фото для каждой секции
        binding.btnMediaTsn.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "ТСН")
                .show(childFragmentManager, "media_tsn")
        }
        binding.btnMediaTt352.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "ТТ-35 2ТСН")
                .show(childFragmentManager, "media_tt352")
        }
        binding.btnMediaTt353.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "ТТ-35 3ТСН")
                .show(childFragmentManager, "media_tt353")
        }
        binding.btnMediaV352.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "В-35 2ТСН")
                .show(childFragmentManager, "media_v352")
        }
        binding.btnMediaV353.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "В-35 3ТСН")
                .show(childFragmentManager, "media_v353")
        }

        // Комментарии для каждой секции
        binding.btnCommentTsn.setOnClickListener {
            CommentsDialogFragment.newInstance("ТСН", "ORU35").show(childFragmentManager, "comment_tsn")
        }
        binding.btnCommentTt352.setOnClickListener {
            CommentsDialogFragment.newInstance("ТТ-35 2ТСН", "ORU35").show(childFragmentManager, "comment_tt352")
        }
        binding.btnCommentTt353.setOnClickListener {
            CommentsDialogFragment.newInstance("ТТ-35 3ТСН", "ORU35").show(childFragmentManager, "comment_tt353")
        }
        binding.btnCommentV352.setOnClickListener {
            CommentsDialogFragment.newInstance("В-35 2ТСН", "ORU35").show(childFragmentManager, "comment_v352")
        }
        binding.btnCommentV353.setOnClickListener {
            CommentsDialogFragment.newInstance("В-35 3ТСН", "ORU35").show(childFragmentManager, "comment_v353")
        }
    }

    private fun updateUIFromData(data: InspectionORU35Data) {
        isUpdatingUIFromViewModel = true

        updateEditTextIfNeeded(binding.tsn2Input, data.tsn2)
        updateEditTextIfNeeded(binding.tsn3Input, data.tsn3)
        updateEditTextIfNeeded(binding.tsn4Input, data.tsn4)

        setSpinnerSelection(binding.tt352TsnAInput, data.tt352tsnA)
        setSpinnerSelection(binding.tt352TsnBInput, data.tt352tsnB)
        setSpinnerSelection(binding.tt352TsnCInput, data.tt352tsnC)
        setSpinnerSelection(binding.tt353TsnAInput, data.tt353tsnA)
        setSpinnerSelection(binding.tt353TsnBInput, data.tt353tsnB)
        setSpinnerSelection(binding.tt353TsnCInput, data.tt353tsnC)
        setSpinnerSelection(binding.v352TsnAInput, data.v352tsnA)
        setSpinnerSelection(binding.v352TsnBInput, data.v352tsnB)
        setSpinnerSelection(binding.v352TsnCInput, data.v352tsnC)
        setSpinnerSelection(binding.v353TsnAInput, data.v353tsnA)
        setSpinnerSelection(binding.v353TsnBInput, data.v353tsnB)
        setSpinnerSelection(binding.v353TsnCInput, data.v353tsnC)

        isUpdatingUIFromViewModel = false
    }

    private fun updateEditTextIfNeeded(editText: EditText, newValue: String) {
        val currentText = editText.text.toString()
        if (currentText != newValue) {
            editText.setText(newValue)
            editText.setSelection(editText.text.length)
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        if (value.isNotEmpty()) {
            val adapter = spinner.adapter
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString() == value) {
                    if (spinner.selectedItemPosition != i) {
                        spinner.setSelection(i, false)
                    }
                    break
                }
            }
        }
    }

    private fun setupInputListeners() {
        setupEditTextListener(binding.tsn2Input) { text ->
            sharedViewModel.updateORU35Data { tsn2 = text }
        }
        setupEditTextListener(binding.tsn3Input) { text ->
            sharedViewModel.updateORU35Data { tsn3 = text }
        }
        setupEditTextListener(binding.tsn4Input) { text ->
            sharedViewModel.updateORU35Data { tsn4 = text }
        }

        setupSpinnerListener(binding.tt352TsnAInput) { selectedItem ->
            sharedViewModel.updateORU35Data { tt352tsnA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tt352TsnBInput) { selectedItem ->
            sharedViewModel.updateORU35Data { tt352tsnB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tt352TsnCInput) { selectedItem ->
            sharedViewModel.updateORU35Data { tt352tsnC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tt353TsnAInput) { selectedItem ->
            sharedViewModel.updateORU35Data { tt353tsnA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tt353TsnBInput) { selectedItem ->
            sharedViewModel.updateORU35Data { tt353tsnB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tt353TsnCInput) { selectedItem ->
            sharedViewModel.updateORU35Data { tt353tsnC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.v352TsnAInput) { selectedItem ->
            sharedViewModel.updateORU35Data { v352tsnA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.v352TsnBInput) { selectedItem ->
            sharedViewModel.updateORU35Data { v352tsnB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.v352TsnCInput) { selectedItem ->
            sharedViewModel.updateORU35Data { v352tsnC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.v353TsnAInput) { selectedItem ->
            sharedViewModel.updateORU35Data { v353tsnA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.v353TsnBInput) { selectedItem ->
            sharedViewModel.updateORU35Data { v353tsnB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.v353TsnCInput) { selectedItem ->
            sharedViewModel.updateORU35Data { v353tsnC = selectedItem.toString() }
        }
    }

    private fun setupEditTextListener(editText: EditText, onTextChanged: (String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingUIFromViewModel) return
                val newText = s?.toString() ?: ""
                onTextChanged(newText)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSpinnerListener(spinner: Spinner, onItemSelected: (Any?) -> Unit) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    onItemSelected(parent?.getItemAtPosition(position))
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}