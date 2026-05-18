package com.example.ps_inspection.ui.fragments.inspections

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ps_inspection.R
import com.example.ps_inspection.data.models.Comment
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.repositories.InspectionMediaManager
import com.example.ps_inspection.data.repositories.LastInspectionManager
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.example.ps_inspection.databinding.FragmentInspectionORU35Binding
import com.example.ps_inspection.ui.fragments.dialogs.CommentsDialogFragment
import com.example.ps_inspection.ui.fragments.dialogs.MediaDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.widget.TextView

class InspectionORU35 : Fragment() {

    private var currentToast: Toast? = null

    private lateinit var layoutInflater: LayoutInflater

    private var _binding: FragmentInspectionORU35Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()

    private var isUpdatingUIFromViewModel = false
    private lateinit var mediaManager: InspectionMediaManager
    private val lastInspectionManager: LastInspectionManager by lazy {
        LastInspectionManager(requireContext())
    }

    // Маппинг параметров для подсказок (будет инициализирован в onViewCreated)
    private lateinit var hintMapping: Map<String, View>
    private lateinit var paramNames: Map<String, String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectionORU35Binding.inflate(inflater, container, false)
        mediaManager = InspectionMediaManager(requireContext())
        layoutInflater = inflater
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем маппинги ТОЛЬКО ПОСЛЕ создания binding
        initMappings()

        // Подписка на данные осмотра
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru35Data.collectLatest { data ->
                updateUIFromData(data)
            }
        }

        // Подписка на комментарии
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru35Comments.collectLatest { comments ->
                updateCommentButtonsState(comments)
            }
        }

        // ПОДПИСКА НА ПОДСКАЗКИ ИЗ GOOGLE SHEETS
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.lastValuesFromSheets.collect { lastValues ->
                applyHintsFromSheets(lastValues)
            }
        }

        setupInputListeners()
        setupMediaButtons()
        updatePhotoButtonsState()

        // Загружаем подсказки из Google Sheets
        loadHintsFromSheets()
    }

    private fun initMappings() {
        hintMapping = mapOf(
            "ОРУ-35 2ТСН" to binding.tsn2Input,
            "ОРУ-35 3ТСН" to binding.tsn3Input,
            "ОРУ-35 4ТСН" to binding.tsn4Input,
            "ОРУ-35 ТТ-35 2ТСН А" to binding.tt352TsnAInput,
            "ОРУ-35 ТТ-35 2ТСН В" to binding.tt352TsnBInput,
            "ОРУ-35 ТТ-35 2ТСН С" to binding.tt352TsnCInput,
            "ОРУ-35 ТТ-35 3ТСН А" to binding.tt353TsnAInput,
            "ОРУ-35 ТТ-35 3ТСН В" to binding.tt353TsnBInput,
            "ОРУ-35 ТТ-35 3ТСН С" to binding.tt353TsnCInput,
            "ОРУ-35 В-35 2ТСН А" to binding.v352TsnAInput,
            "ОРУ-35 В-35 2ТСН В" to binding.v352TsnBInput,
            "ОРУ-35 В-35 2ТСН С" to binding.v352TsnCInput,
            "ОРУ-35 В-35 3ТСН А" to binding.v353TsnAInput,
            "ОРУ-35 В-35 3ТСН В" to binding.v353TsnBInput,
            "ОРУ-35 В-35 3ТСН С" to binding.v353TsnCInput
        )

        paramNames = mapOf(
            "ОРУ-35 2ТСН" to "2ТСН уровень масла",
            "ОРУ-35 3ТСН" to "3ТСН уровень масла",
            "ОРУ-35 4ТСН" to "4ТСН уровень масла",
            "ОРУ-35 ТТ-35 2ТСН А" to "ТТ-35 2ТСН ф.А",
            "ОРУ-35 ТТ-35 2ТСН В" to "ТТ-35 2ТСН ф.В",
            "ОРУ-35 ТТ-35 2ТСН С" to "ТТ-35 2ТСН ф.С",
            "ОРУ-35 ТТ-35 3ТСН А" to "ТТ-35 3ТСН ф.А",
            "ОРУ-35 ТТ-35 3ТСН В" to "ТТ-35 3ТСН ф.В",
            "ОРУ-35 ТТ-35 3ТСН С" to "ТТ-35 3ТСН ф.С",
            "ОРУ-35 В-35 2ТСН А" to "В-35 2ТСН ф.А",
            "ОРУ-35 В-35 2ТСН В" to "В-35 2ТСН ф.В",
            "ОРУ-35 В-35 2ТСН С" to "В-35 2ТСН ф.С",
            "ОРУ-35 В-35 3ТСН А" to "В-35 3ТСН ф.А",
            "ОРУ-35 В-35 3ТСН В" to "В-35 3ТСН ф.В",
            "ОРУ-35 В-35 3ТСН С" to "В-35 3ТСН ф.С"
        )
    }

    private fun loadHintsFromSheets() {
        if (!::hintMapping.isInitialized) return
        val parameterKeys = hintMapping.keys.toList()
        sharedViewModel.loadLastValuesFromSheets(parameterKeys)
    }

    private fun applyHintsFromSheets(lastValues: Map<String, String>) {
        if (!::hintMapping.isInitialized || !::paramNames.isInitialized) return

        if (lastValues.isEmpty()) {
            loadLocalHints()
            return
        }

        for ((key, value) in lastValues) {
            val targetView = hintMapping[key]
            val paramName = paramNames[key] ?: key

            when (targetView) {
                is EditText -> {
                    setupEditTextHint(targetView, value, paramName)
                }
                is Spinner -> {
                    setupSpinnerHint(targetView, value, paramName)
                }
            }
        }
    }

    private fun loadLocalHints() {
        val lastData = lastInspectionManager.getLastOru35Data()
        if (lastData == null) {
            Toast.makeText(requireContext(), "Нет данных о прошлых осмотрах", Toast.LENGTH_SHORT).show()
            return
        }

        setupEditTextHint(binding.tsn2Input, lastData.tsn2, "2ТСН уровень масла")
        setupEditTextHint(binding.tsn3Input, lastData.tsn3, "3ТСН уровень масла")
        setupEditTextHint(binding.tsn4Input, lastData.tsn4, "4ТСН уровень масла")

        setupSpinnerHint(binding.tt352TsnAInput, lastData.tt352tsnA, "ТТ-35 2ТСН ф.А")
        setupSpinnerHint(binding.tt352TsnBInput, lastData.tt352tsnB, "ТТ-35 2ТСН ф.В")
        setupSpinnerHint(binding.tt352TsnCInput, lastData.tt352tsnC, "ТТ-35 2ТСН ф.С")
        setupSpinnerHint(binding.tt353TsnAInput, lastData.tt353tsnA, "ТТ-35 3ТСН ф.А")
        setupSpinnerHint(binding.tt353TsnBInput, lastData.tt353tsnB, "ТТ-35 3ТСН ф.В")
        setupSpinnerHint(binding.tt353TsnCInput, lastData.tt353tsnC, "ТТ-35 3ТСН ф.С")
        setupSpinnerHint(binding.v352TsnAInput, lastData.v352tsnA, "В-35 2ТСН ф.А")
        setupSpinnerHint(binding.v352TsnBInput, lastData.v352tsnB, "В-35 2ТСН ф.В")
        setupSpinnerHint(binding.v352TsnCInput, lastData.v352tsnC, "В-35 2ТСН ф.С")
        setupSpinnerHint(binding.v353TsnAInput, lastData.v353tsnA, "В-35 3ТСН ф.А")
        setupSpinnerHint(binding.v353TsnBInput, lastData.v353tsnB, "В-35 3ТСН ф.В")
        setupSpinnerHint(binding.v353TsnCInput, lastData.v353tsnC, "В-35 3ТСН ф.С")
    }

    private fun setupEditTextHint(editText: EditText, lastValue: String, paramName: String) {
        if (lastValue.isNotBlank() && lastValue != "○" && lastValue != "-" && lastValue != "null") {
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && editText.text.toString().isEmpty()) {
                    // Показываем сразу, без проверки флага
                    showLastValueHint(editText, lastValue, paramName)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSpinnerHint(spinner: Spinner, lastValue: String, paramName: String) {
        if (lastValue.isNotBlank() && lastValue != "○" && lastValue != "-" && lastValue != "null") {
            spinner.setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    if (spinner.selectedItemPosition == 0) {
                        // Показываем сразу, без проверки флага
                        showSpinnerHint(paramName, lastValue)
                    }
                }
                false
            }
        }
    }

    private fun showLastValueHint(currentEditText: EditText, lastValue: String, paramName: String) {
        showCustomToast("📌 $paramName: последнее $lastValue")

        currentEditText.setBackgroundResource(R.drawable.edittext_border_hint)
        currentEditText.postDelayed({
            currentEditText.setBackgroundResource(R.drawable.edittext_border)
        }, 3000)
    }

    private fun showSpinnerHint(paramName: String, lastValue: String) {
        showCustomToast("📌 $paramName: последнее '$lastValue'")
    }

    fun updateCommentButtonsState(comments: Map<String, List<Comment>>) {
        val buttonKeys = mapOf(
            binding.btnCommentTsn to "ТСН",
            binding.btnCommentTt352 to "ТТ-35 2ТСН",
            binding.btnCommentTt353 to "ТТ-35 3ТСН",
            binding.btnCommentV352 to "В-35 2ТСН",
            binding.btnCommentV353 to "В-35 3ТСН"
        )

        buttonKeys.forEach { (button, key) ->
            val hasComment = comments[key].isNullOrEmpty().not()
            val color = if (hasComment) Color.parseColor("#4CAF50") else Color.parseColor("#9E9E9E")
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
            val color = if (hasPhotos) Color.parseColor("#4CAF50") else Color.parseColor("#9E9E9E")
            button.setColorFilter(color)
        }
    }

    fun refreshPhotoButtonsState() {
        updatePhotoButtonsState()
    }

    private fun setupMediaButtons() {
        val inspectionId = "current_inspection"

        binding.btnMediaTsn.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "ТСН").show(childFragmentManager, "media_tsn")
        }
        binding.btnMediaTt352.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "ТТ-35 2ТСН").show(childFragmentManager, "media_tt352")
        }
        binding.btnMediaTt353.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "ТТ-35 3ТСН").show(childFragmentManager, "media_tt353")
        }
        binding.btnMediaV352.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "В-35 2ТСН").show(childFragmentManager, "media_v352")
        }
        binding.btnMediaV353.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "В-35 3ТСН").show(childFragmentManager, "media_v353")
        }

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
        setupEditTextListener(binding.tsn2Input) { text -> sharedViewModel.updateORU35Data { tsn2 = text } }
        setupEditTextListener(binding.tsn3Input) { text -> sharedViewModel.updateORU35Data { tsn3 = text } }
        setupEditTextListener(binding.tsn4Input) { text -> sharedViewModel.updateORU35Data { tsn4 = text } }

        setupSpinnerListener(binding.tt352TsnAInput) { sharedViewModel.updateORU35Data { tt352tsnA = it.toString() } }
        setupSpinnerListener(binding.tt352TsnBInput) { sharedViewModel.updateORU35Data { tt352tsnB = it.toString() } }
        setupSpinnerListener(binding.tt352TsnCInput) { sharedViewModel.updateORU35Data { tt352tsnC = it.toString() } }
        setupSpinnerListener(binding.tt353TsnAInput) { sharedViewModel.updateORU35Data { tt353tsnA = it.toString() } }
        setupSpinnerListener(binding.tt353TsnBInput) { sharedViewModel.updateORU35Data { tt353tsnB = it.toString() } }
        setupSpinnerListener(binding.tt353TsnCInput) { sharedViewModel.updateORU35Data { tt353tsnC = it.toString() } }
        setupSpinnerListener(binding.v352TsnAInput) { sharedViewModel.updateORU35Data { v352tsnA = it.toString() } }
        setupSpinnerListener(binding.v352TsnBInput) { sharedViewModel.updateORU35Data { v352tsnB = it.toString() } }
        setupSpinnerListener(binding.v352TsnCInput) { sharedViewModel.updateORU35Data { v352tsnC = it.toString() } }
        setupSpinnerListener(binding.v353TsnAInput) { sharedViewModel.updateORU35Data { v353tsnA = it.toString() } }
        setupSpinnerListener(binding.v353TsnBInput) { sharedViewModel.updateORU35Data { v353tsnB = it.toString() } }
        setupSpinnerListener(binding.v353TsnCInput) { sharedViewModel.updateORU35Data { v353tsnC = it.toString() } }
    }

    private fun setupEditTextListener(editText: EditText, onTextChanged: (String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingUIFromViewModel) return
                onTextChanged(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSpinnerListener(spinner: Spinner, onItemSelected: (Any?) -> Unit) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) onItemSelected(parent?.getItemAtPosition(position))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showCustomToast(message: String) {
        // Отменяем предыдущий Toast, если он есть
        currentToast?.cancel()

        val layout = layoutInflater.inflate(R.layout.custom_toast, null)
        val text = layout.findViewById<TextView>(R.id.toast_text)
        text.text = message

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.setGravity(android.view.Gravity.TOP, 0, 100)
        toast.show()

        // Сохраняем текущий Toast
        currentToast = toast
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentToast?.cancel()
        currentToast = null
        _binding = null
    }
}