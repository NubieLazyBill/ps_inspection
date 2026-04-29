package com.example.ps_inspection.ui.fragments.inspections

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ps_inspection.data.models.InspectionORU220Data
import com.example.ps_inspection.data.repositories.InspectionMediaManager
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.example.ps_inspection.databinding.FragmentInspectionORU220Binding
import com.example.ps_inspection.ui.fragments.dialogs.CommentsDialogFragment
import com.example.ps_inspection.ui.fragments.dialogs.MediaDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InspectionORU220 : Fragment() {

    private var _binding: FragmentInspectionORU220Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private lateinit var mediaManager: InspectionMediaManager

    private var isUpdatingUIFromViewModel = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectionORU220Binding.inflate(inflater, container, false)
        mediaManager = InspectionMediaManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru220Data.collectLatest { data ->
                updateUIFromData(data)
            }
        }

        // Подписываемся на изменения комментариев ОРУ-220
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru220Comments.collectLatest { comments ->
                updateCommentButtonsState(comments)
            }
        }

        setupInputListeners()
        setupMediaButtons()
        setupFillButtons()
        updatePhotoButtonsState()

        binding.btnFillAllTn1.setOnClickListener {
            fillAllTn220(binding.tn1UpperA, binding.tn1UpperB, binding.tn1UpperC,
                binding.tn1LowerA, binding.tn1LowerB, binding.tn1LowerC,
                "1ТН-220")
        }

        binding.btnFillAllTn2.setOnClickListener {
            fillAllTn220(binding.tn2UpperA, binding.tn2UpperB, binding.tn2UpperC,
                binding.tn2LowerA, binding.tn2LowerB, binding.tn2LowerC,
                "2ТН-220")
        }
    }

    private fun updatePhotoButtonsState() {
        val inspectionId = "current_inspection"
        val photoButtons = mapOf(
            binding.btnMediaMirnaya to "Мирная",
            binding.btnMediaMirnayaTT to "Мирная ТТ",
            binding.btnMediaTopaz to "Топаз",
            binding.btnMediaTopazTT to "Топаз ТТ",
            binding.btnMediaOv to "ОВ",
            binding.btnMediaOvTT to "ОВ ТТ",
            binding.btnMediaOssh to "ТН-220 ОСШ",
            binding.btnMediaV2atg to "2АТГ",
            binding.btnMediaV2atgTT to "2АТГ ТТ",
            binding.btnMediaShsv to "ШСВ",
            binding.btnMediaShsvTT to "ШСВ ТТ",
            binding.btnMediaV3atg to "3АТГ",
            binding.btnMediaV3atgTT to "3АТГ ТТ",
            binding.btnMediaOrbita to "Орбита",
            binding.btnMediaOrbitaTT to "Орбита ТТ",
            binding.btnMediaFakel to "Факел",
            binding.btnMediaFakelTT to "Факел ТТ",
            binding.btnMediaCometa1 to "Комета-1",
            binding.btnMediaCometa1TT to "Комета-1 ТТ",
            binding.btnMediaCometa2 to "Комета-2",
            binding.btnMediaCometa2TT to "Комета-2 ТТ",
            binding.btnMediaTn1 to "1ТН-220",
            binding.btnMediaTn2 to "2ТН-220"
        )

        photoButtons.forEach { (button, name) ->
            val hasPhotos = mediaManager.hasPhotos(inspectionId, name)
            val color = if (hasPhotos) Color.parseColor("#4CAF50") else Color.parseColor("#9E9E9E")
            button.setColorFilter(color)
        }
    }

    fun updateCommentButtonsState(comments: Map<String, List<String>>) {
        val buttonKeys = mapOf(
            binding.btnCommentMirnaya to "Мирная",
            binding.btnCommentMirnayaTT to "Мирная ТТ",
            binding.btnCommentTopaz to "Топаз",
            binding.btnCommentTopazTT to "Топаз ТТ",
            binding.btnCommentOv to "ОВ",
            binding.btnCommentOvTT to "ОВ ТТ",
            binding.btnCommentOssh to "ТН-220 ОСШ",
            binding.btnCommentV2atg to "2АТГ",
            binding.btnCommentV2atgTT to "2АТГ ТТ",
            binding.btnCommentShsv to "ШСВ",
            binding.btnCommentShsvTT to "ШСВ ТТ",
            binding.btnCommentV3atg to "3АТГ",
            binding.btnCommentV3atgTT to "3АТГ ТТ",
            binding.btnCommentOrbita to "Орбита",
            binding.btnCommentOrbitaTT to "Орбита ТТ",
            binding.btnCommentFakel to "Факел",
            binding.btnCommentFakelTT to "Факел ТТ",
            binding.btnCommentCometa1 to "Комета-1",
            binding.btnCommentCometa1TT to "Комета-1 ТТ",
            binding.btnCommentCometa2 to "Комета-2",
            binding.btnCommentCometa2TT to "Комета-2 ТТ",
            binding.btnCommentTn1 to "1ТН-220",
            binding.btnCommentTn2 to "2ТН-220"
        )

        buttonKeys.forEach { (button, key) ->
            val hasComment = comments[key].isNullOrEmpty().not()
            val color = if (hasComment) Color.parseColor("#4CAF50") else Color.parseColor("#9E9E9E")
            button.setColorFilter(color)
        }
    }

    fun refreshPhotoButtonsState() {
        updatePhotoButtonsState()
    }

    private fun setupMediaButtons() {
        val inspectionId = "current_inspection"

        // Фото для всех 23 секций
        binding.btnMediaMirnaya.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Мирная")
                .show(childFragmentManager, "media_mirnaya")
        }
        binding.btnMediaMirnayaTT.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Мирная ТТ")
                .show(childFragmentManager, "media_mirnaya_tt")
        }
        binding.btnMediaTopaz.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Топаз")
                .show(childFragmentManager, "media_topaz")
        }
        binding.btnMediaTopazTT.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Топаз ТТ")
                .show(childFragmentManager, "media_topaz_tt")
        }
        binding.btnMediaOv.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "ОВ")
                .show(childFragmentManager, "media_ov")
        }
        binding.btnMediaOvTT.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "ОВ ТТ")
                .show(childFragmentManager, "media_ov_tt")
        }
        binding.btnMediaOssh.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "ТН-220 ОСШ")
                .show(childFragmentManager, "media_ossh")
        }
        binding.btnMediaV2atg.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "2АТГ")
                .show(childFragmentManager, "media_v2atg")
        }
        binding.btnMediaV2atgTT.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "2АТГ ТТ")
                .show(childFragmentManager, "media_v2atg_tt")
        }
        binding.btnMediaShsv.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "ШСВ")
                .show(childFragmentManager, "media_shsv")
        }
        binding.btnMediaShsvTT.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "ШСВ ТТ")
                .show(childFragmentManager, "media_shsv_tt")
        }
        binding.btnMediaV3atg.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "3АТГ")
                .show(childFragmentManager, "media_v3atg")
        }
        binding.btnMediaV3atgTT.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "3АТГ ТТ")
                .show(childFragmentManager, "media_v3atg_tt")
        }
        binding.btnMediaOrbita.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Орбита")
                .show(childFragmentManager, "media_orbita")
        }
        binding.btnMediaOrbitaTT.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Орбита ТТ")
                .show(childFragmentManager, "media_orbita_tt")
        }
        binding.btnMediaFakel.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Факел")
                .show(childFragmentManager, "media_fakel")
        }
        binding.btnMediaFakelTT.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Факел ТТ")
                .show(childFragmentManager, "media_fakel_tt")
        }
        binding.btnMediaCometa1.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Комета-1")
                .show(childFragmentManager, "media_cometa1")
        }
        binding.btnMediaCometa1TT.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Комета-1 ТТ")
                .show(childFragmentManager, "media_cometa1_tt")
        }
        binding.btnMediaCometa2.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Комета-2")
                .show(childFragmentManager, "media_cometa2")
        }
        binding.btnMediaCometa2TT.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "Комета-2 ТТ")
                .show(childFragmentManager, "media_cometa2_tt")
        }
        binding.btnMediaTn1.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "1ТН-220")
                .show(childFragmentManager, "media_tn1")
        }
        binding.btnMediaTn2.setOnClickListener {
            MediaDialogFragment.newInstance(inspectionId, "2ТН-220")
                .show(childFragmentManager, "media_tn2")
        }

        // Комментарии для всех 23 секций
        binding.btnCommentMirnaya.setOnClickListener {
            CommentsDialogFragment.newInstance("Мирная", "ORU220")
                .show(childFragmentManager, "comment_mirnaya")
        }
        binding.btnCommentMirnayaTT.setOnClickListener {
            CommentsDialogFragment.newInstance("Мирная ТТ", "ORU220")
                .show(childFragmentManager, "comment_mirnaya_tt")
        }
        binding.btnCommentTopaz.setOnClickListener {
            CommentsDialogFragment.newInstance("Топаз", "ORU220")
                .show(childFragmentManager, "comment_topaz")
        }
        binding.btnCommentTopazTT.setOnClickListener {
            CommentsDialogFragment.newInstance("Топаз ТТ", "ORU220")
                .show(childFragmentManager, "comment_topaz_tt")
        }
        binding.btnCommentOv.setOnClickListener {
            CommentsDialogFragment.newInstance("ОВ", "ORU220")
                .show(childFragmentManager, "comment_ov")
        }
        binding.btnCommentOvTT.setOnClickListener {
            CommentsDialogFragment.newInstance("ОВ ТТ", "ORU220")
                .show(childFragmentManager, "comment_ov_tt")
        }
        binding.btnCommentOssh.setOnClickListener {
            CommentsDialogFragment.newInstance("ТН-220 ОСШ", "ORU220")
                .show(childFragmentManager, "comment_ossh")
        }
        binding.btnCommentV2atg.setOnClickListener {
            CommentsDialogFragment.newInstance("2АТГ", "ORU220")
                .show(childFragmentManager, "comment_v2atg")
        }
        binding.btnCommentV2atgTT.setOnClickListener {
            CommentsDialogFragment.newInstance("2АТГ ТТ", "ORU220")
                .show(childFragmentManager, "comment_v2atg_tt")
        }
        binding.btnCommentShsv.setOnClickListener {
            CommentsDialogFragment.newInstance("ШСВ", "ORU220")
                .show(childFragmentManager, "comment_shsv")
        }
        binding.btnCommentShsvTT.setOnClickListener {
            CommentsDialogFragment.newInstance("ШСВ ТТ", "ORU220")
                .show(childFragmentManager, "comment_shsv_tt")
        }
        binding.btnCommentV3atg.setOnClickListener {
            CommentsDialogFragment.newInstance("3АТГ", "ORU220")
                .show(childFragmentManager, "comment_v3atg")
        }
        binding.btnCommentV3atgTT.setOnClickListener {
            CommentsDialogFragment.newInstance("3АТГ ТТ", "ORU220")
                .show(childFragmentManager, "comment_v3atg_tt")
        }
        binding.btnCommentOrbita.setOnClickListener {
            CommentsDialogFragment.newInstance("Орбита", "ORU220")
                .show(childFragmentManager, "comment_orbita")
        }
        binding.btnCommentOrbitaTT.setOnClickListener {
            CommentsDialogFragment.newInstance("Орбита ТТ", "ORU220")
                .show(childFragmentManager, "comment_orbita_tt")
        }
        binding.btnCommentFakel.setOnClickListener {
            CommentsDialogFragment.newInstance("Факел", "ORU220")
                .show(childFragmentManager, "comment_fakel")
        }
        binding.btnCommentFakelTT.setOnClickListener {
            CommentsDialogFragment.newInstance("Факел ТТ", "ORU220")
                .show(childFragmentManager, "comment_fakel_tt")
        }
        binding.btnCommentCometa1.setOnClickListener {
            CommentsDialogFragment.newInstance("Комета-1", "ORU220")
                .show(childFragmentManager, "comment_cometa1")
        }
        binding.btnCommentCometa1TT.setOnClickListener {
            CommentsDialogFragment.newInstance("Комета-1 ТТ", "ORU220")
                .show(childFragmentManager, "comment_cometa1_tt")
        }
        binding.btnCommentCometa2.setOnClickListener {
            CommentsDialogFragment.newInstance("Комета-2", "ORU220")
                .show(childFragmentManager, "comment_cometa2")
        }
        binding.btnCommentCometa2TT.setOnClickListener {
            CommentsDialogFragment.newInstance("Комета-2 ТТ", "ORU220")
                .show(childFragmentManager, "comment_cometa2_tt")
        }
        binding.btnCommentTn1.setOnClickListener {
            CommentsDialogFragment.newInstance("1ТН-220", "ORU220")
                .show(childFragmentManager, "comment_tn1")
        }
        binding.btnCommentTn2.setOnClickListener {
            CommentsDialogFragment.newInstance("2ТН-220", "ORU220")
                .show(childFragmentManager, "comment_tn2")
        }
    }

    private fun updateUIFromData(data: InspectionORU220Data) {
        isUpdatingUIFromViewModel = true

        // Мирная
        setSpinnerSelection(binding.purgingMirnayaA, data.purgingMirnayaA)
        setSpinnerSelection(binding.purgingMirnayaB, data.purgingMirnayaB)
        setSpinnerSelection(binding.purgingMirnayaC, data.purgingMirnayaC)
        setSpinnerSelection(binding.oilMirnayaA, data.oilMirnayaA)
        setSpinnerSelection(binding.oilMirnayaB, data.oilMirnayaB)
        setSpinnerSelection(binding.oilMirnayaC, data.oilMirnayaC)

        // Топаз
        setSpinnerSelection(binding.purgingTopazA, data.purgingTopazA)
        setSpinnerSelection(binding.purgingTopazB, data.purgingTopazB)
        setSpinnerSelection(binding.purgingTopazC, data.purgingTopazC)
        setSpinnerSelection(binding.oilTopazA, data.oilTopazA)
        setSpinnerSelection(binding.oilTopazB, data.oilTopazB)
        setSpinnerSelection(binding.oilTopazC, data.oilTopazC)

        // ОВ
        setSpinnerSelection(binding.purgingOvA, data.purgingOvA)
        setSpinnerSelection(binding.purgingOvB, data.purgingOvB)
        setSpinnerSelection(binding.purgingOvC, data.purgingOvC)
        setSpinnerSelection(binding.oilOvA, data.oilOvA)
        setSpinnerSelection(binding.oilOvB, data.oilOvB)
        setSpinnerSelection(binding.oilOvC, data.oilOvC)

        // ТН-220 ОСШ ф.В
        setSpinnerSelection(binding.tnOsshFvUpper, data.tnOsshFvUpper)
        setSpinnerSelection(binding.tnOsshFvLower, data.tnOsshFvLower)

        // В-220 2АТГ
        setSpinnerSelection(binding.purgingV2atgA, data.purgingV2atgA)
        setSpinnerSelection(binding.purgingV2atgB, data.purgingV2atgB)
        setSpinnerSelection(binding.purgingV2atgC, data.purgingV2atgC)
        setSpinnerSelection(binding.oilTt2atgA, data.oilTt2atgA)
        setSpinnerSelection(binding.oilTt2atgB, data.oilTt2atgB)
        setSpinnerSelection(binding.oilTt2atgC, data.oilTt2atgC)

        // ШСВ-220
        setSpinnerSelection(binding.purgingShSVA, data.purgingShSV220A)
        setSpinnerSelection(binding.purgingShSVB, data.purgingShSV220B)
        setSpinnerSelection(binding.purgingShSVC, data.purgingShSV220C)
        setSpinnerSelection(binding.oilTtShSVA, data.oilTtShSV220A)
        setSpinnerSelection(binding.oilTtShSVB, data.oilTtShSV220B)
        setSpinnerSelection(binding.oilTtShSVC, data.oilTtShSV220C)

        // В-220 3АТГ
        setSpinnerSelection(binding.purgingV3atgA, data.purgingV3atgA)
        setSpinnerSelection(binding.purgingV3atgB, data.purgingV3atgB)
        setSpinnerSelection(binding.purgingV3atgC, data.purgingV3atgC)
        setSpinnerSelection(binding.oilTt3atgA, data.oilTt3atgA)
        setSpinnerSelection(binding.oilTt3atgB, data.oilTt3atgB)
        setSpinnerSelection(binding.oilTt3atgC, data.oilTt3atgC)

        // Орбита
        setSpinnerSelection(binding.purgingOrbitaA, data.purgingOrbitaA)
        setSpinnerSelection(binding.purgingOrbitaB, data.purgingOrbitaB)
        setSpinnerSelection(binding.purgingOrbitaC, data.purgingOrbitaC)
        setSpinnerSelection(binding.oilOrbitaA, data.oilOrbitaA)
        setSpinnerSelection(binding.oilOrbitaB, data.oilOrbitaB)
        setSpinnerSelection(binding.oilOrbitaC, data.oilOrbitaC)

        // Факел
        setSpinnerSelection(binding.purgingFakelA, data.purgingFakelA)
        setSpinnerSelection(binding.purgingFakelB, data.purgingFakelB)
        setSpinnerSelection(binding.purgingFakelC, data.purgingFakelC)
        setSpinnerSelection(binding.oilFakelA, data.oilFakelA)
        setSpinnerSelection(binding.oilFakelB, data.oilFakelB)
        setSpinnerSelection(binding.oilFakelC, data.oilFakelC)

        // Комета-1
        setSpinnerSelection(binding.purgingCometa1A, data.purgingCometa1A)
        setSpinnerSelection(binding.purgingCometa1B, data.purgingCometa1B)
        setSpinnerSelection(binding.purgingCometa1C, data.purgingCometa1C)
        setSpinnerSelection(binding.oilCometa1A, data.oilCometa1A)
        setSpinnerSelection(binding.oilCometa1B, data.oilCometa1B)
        setSpinnerSelection(binding.oilCometa1C, data.oilCometa1C)

        // Комета-2
        setSpinnerSelection(binding.purgingCometa2A, data.purgingCometa2A)
        setSpinnerSelection(binding.purgingCometa2B, data.purgingCometa2B)
        setSpinnerSelection(binding.purgingCometa2C, data.purgingCometa2C)
        setSpinnerSelection(binding.oilCometa2A, data.oilCometa2A)
        setSpinnerSelection(binding.oilCometa2B, data.oilCometa2B)
        setSpinnerSelection(binding.oilCometa2C, data.oilCometa2C)

        // 1ТН-220
        setSpinnerSelection(binding.tn1UpperA, data.tn1UpperA)
        setSpinnerSelection(binding.tn1UpperB, data.tn1UpperB)
        setSpinnerSelection(binding.tn1UpperC, data.tn1UpperC)
        setSpinnerSelection(binding.tn1LowerA, data.tn1LowerA)
        setSpinnerSelection(binding.tn1LowerB, data.tn1LowerB)
        setSpinnerSelection(binding.tn1LowerC, data.tn1LowerC)

        // 2ТН-220
        setSpinnerSelection(binding.tn2UpperA, data.tn2UpperA)
        setSpinnerSelection(binding.tn2UpperB, data.tn2UpperB)
        setSpinnerSelection(binding.tn2UpperC, data.tn2UpperC)
        setSpinnerSelection(binding.tn2LowerA, data.tn2LowerA)
        setSpinnerSelection(binding.tn2LowerB, data.tn2LowerB)
        setSpinnerSelection(binding.tn2LowerC, data.tn2LowerC)

        isUpdatingUIFromViewModel = false
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        if (value.isNotEmpty()) {
            val adapter = spinner.adapter
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString() == value) {
                    spinner.setSelection(i, false)
                    break
                }
            }
        }
    }

    private fun setupInputListeners() {
        // Мирная
        setupSpinnerListener(binding.purgingMirnayaA) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingMirnayaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingMirnayaB) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingMirnayaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingMirnayaC) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingMirnayaC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilMirnayaA) { selectedItem ->
            sharedViewModel.updateORU220Data { oilMirnayaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilMirnayaB) { selectedItem ->
            sharedViewModel.updateORU220Data { oilMirnayaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilMirnayaC) { selectedItem ->
            sharedViewModel.updateORU220Data { oilMirnayaC = selectedItem.toString() }
        }

        // Топаз
        setupSpinnerListener(binding.purgingTopazA) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingTopazA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingTopazB) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingTopazB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingTopazC) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingTopazC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTopazA) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTopazA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTopazB) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTopazB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTopazC) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTopazC = selectedItem.toString() }
        }

        // ОВ
        setupSpinnerListener(binding.purgingOvA) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingOvA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingOvB) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingOvB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingOvC) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingOvC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilOvA) { selectedItem ->
            sharedViewModel.updateORU220Data { oilOvA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilOvB) { selectedItem ->
            sharedViewModel.updateORU220Data { oilOvB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilOvC) { selectedItem ->
            sharedViewModel.updateORU220Data { oilOvC = selectedItem.toString() }
        }

        // ТН-220 ОСШ
        setupSpinnerListener(binding.tnOsshFvUpper) { selectedItem ->
            sharedViewModel.updateORU220Data { tnOsshFvUpper = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tnOsshFvLower) { selectedItem ->
            sharedViewModel.updateORU220Data { tnOsshFvLower = selectedItem.toString() }
        }

        // 2АТГ
        setupSpinnerListener(binding.purgingV2atgA) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingV2atgA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingV2atgB) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingV2atgB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingV2atgC) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingV2atgC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTt2atgA) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTt2atgA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTt2atgB) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTt2atgB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTt2atgC) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTt2atgC = selectedItem.toString() }
        }

        // ШСВ
        setupSpinnerListener(binding.purgingShSVA) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingShSV220A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingShSVB) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingShSV220B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingShSVC) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingShSV220C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtShSVA) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTtShSV220A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtShSVB) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTtShSV220B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtShSVC) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTtShSV220C = selectedItem.toString() }
        }

        // 3АТГ
        setupSpinnerListener(binding.purgingV3atgA) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingV3atgA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingV3atgB) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingV3atgB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingV3atgC) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingV3atgC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTt3atgA) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTt3atgA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTt3atgB) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTt3atgB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTt3atgC) { selectedItem ->
            sharedViewModel.updateORU220Data { oilTt3atgC = selectedItem.toString() }
        }

        // Орбита
        setupSpinnerListener(binding.purgingOrbitaA) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingOrbitaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingOrbitaB) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingOrbitaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingOrbitaC) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingOrbitaC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilOrbitaA) { selectedItem ->
            sharedViewModel.updateORU220Data { oilOrbitaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilOrbitaB) { selectedItem ->
            sharedViewModel.updateORU220Data { oilOrbitaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilOrbitaC) { selectedItem ->
            sharedViewModel.updateORU220Data { oilOrbitaC = selectedItem.toString() }
        }

        // Факел
        setupSpinnerListener(binding.purgingFakelA) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingFakelA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingFakelB) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingFakelB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingFakelC) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingFakelC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilFakelA) { selectedItem ->
            sharedViewModel.updateORU220Data { oilFakelA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilFakelB) { selectedItem ->
            sharedViewModel.updateORU220Data { oilFakelB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilFakelC) { selectedItem ->
            sharedViewModel.updateORU220Data { oilFakelC = selectedItem.toString() }
        }

        // Комета-1
        setupSpinnerListener(binding.purgingCometa1A) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingCometa1A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingCometa1B) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingCometa1B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingCometa1C) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingCometa1C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilCometa1A) { selectedItem ->
            sharedViewModel.updateORU220Data { oilCometa1A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilCometa1B) { selectedItem ->
            sharedViewModel.updateORU220Data { oilCometa1B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilCometa1C) { selectedItem ->
            sharedViewModel.updateORU220Data { oilCometa1C = selectedItem.toString() }
        }

        // Комета-2
        setupSpinnerListener(binding.purgingCometa2A) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingCometa2A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingCometa2B) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingCometa2B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingCometa2C) { selectedItem ->
            sharedViewModel.updateORU220Data { purgingCometa2C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilCometa2A) { selectedItem ->
            sharedViewModel.updateORU220Data { oilCometa2A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilCometa2B) { selectedItem ->
            sharedViewModel.updateORU220Data { oilCometa2B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilCometa2C) { selectedItem ->
            sharedViewModel.updateORU220Data { oilCometa2C = selectedItem.toString() }
        }

        // 1ТН-220
        setupSpinnerListener(binding.tn1UpperA) { selectedItem ->
            sharedViewModel.updateORU220Data { tn1UpperA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1UpperB) { selectedItem ->
            sharedViewModel.updateORU220Data { tn1UpperB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1UpperC) { selectedItem ->
            sharedViewModel.updateORU220Data { tn1UpperC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1LowerA) { selectedItem ->
            sharedViewModel.updateORU220Data { tn1LowerA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1LowerB) { selectedItem ->
            sharedViewModel.updateORU220Data { tn1LowerB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1LowerC) { selectedItem ->
            sharedViewModel.updateORU220Data { tn1LowerC = selectedItem.toString() }
        }

        // 2ТН-220
        setupSpinnerListener(binding.tn2UpperA) { selectedItem ->
            sharedViewModel.updateORU220Data { tn2UpperA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2UpperB) { selectedItem ->
            sharedViewModel.updateORU220Data { tn2UpperB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2UpperC) { selectedItem ->
            sharedViewModel.updateORU220Data { tn2UpperC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2LowerA) { selectedItem ->
            sharedViewModel.updateORU220Data { tn2LowerA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2LowerB) { selectedItem ->
            sharedViewModel.updateORU220Data { tn2LowerB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2LowerC) { selectedItem ->
            sharedViewModel.updateORU220Data { tn2LowerC = selectedItem.toString() }
        }
    }

    private fun setupSpinnerListener(spinner: Spinner, onItemSelected: (Any?) -> Unit) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && !isUpdatingUIFromViewModel) {
                    onItemSelected(parent?.getItemAtPosition(position))
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFillButtons() {
        // Мирная - ТТ
        binding.btnFillAllMirnayaTT.setOnClickListener {
            val value = binding.oilMirnayaA.selectedItem?.toString()
            if (value.isNullOrEmpty() || value == "Выберите") {
                Toast.makeText(requireContext(), "Сначала выберите значение в ТТ фазы А", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fillSpinners(listOf(binding.oilMirnayaB, binding.oilMirnayaC), value) {
                sharedViewModel.updateORU220Data {
                    oilMirnayaB = value
                    oilMirnayaC = value
                }
            }
        }

        // Топаз - ТТ
        binding.btnFillAllTopazTT.setOnClickListener {
            val value = binding.oilTopazA.selectedItem?.toString()
            if (value.isNullOrEmpty() || value == "Выберите") {
                Toast.makeText(requireContext(), "Сначала выберите значение в ТТ фазы А", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fillSpinners(listOf(binding.oilTopazB, binding.oilTopazC), value) {
                sharedViewModel.updateORU220Data {
                    oilTopazB = value
                    oilTopazC = value
                }
            }
        }

        // ОВ - ТТ
        binding.btnFillAllOvTT.setOnClickListener {
            val value = binding.oilOvA.selectedItem?.toString()
            if (value.isNullOrEmpty() || value == "Выберите") {
                Toast.makeText(requireContext(), "Сначала выберите значение в ТТ фазы А", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fillSpinners(listOf(binding.oilOvB, binding.oilOvC), value) {
                sharedViewModel.updateORU220Data {
                    oilOvB = value
                    oilOvC = value
                }
            }
        }

        // 2АТГ - ТТ
        binding.btnFillAllV2atgTT.setOnClickListener {
            val value = binding.oilTt2atgA.selectedItem?.toString()
            if (value.isNullOrEmpty() || value == "Выберите") {
                Toast.makeText(requireContext(), "Сначала выберите значение в ТТ фазы А", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fillSpinners(listOf(binding.oilTt2atgB, binding.oilTt2atgC), value) {
                sharedViewModel.updateORU220Data {
                    oilTt2atgB = value
                    oilTt2atgC = value
                }
            }
        }

        // ШСВ - ТТ
        binding.btnFillAllShsvTT.setOnClickListener {
            val value = binding.oilTtShSVA.selectedItem?.toString()
            if (value.isNullOrEmpty() || value == "Выберите") {
                Toast.makeText(requireContext(), "Сначала выберите значение в ТТ фазы А", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fillSpinners(listOf(binding.oilTtShSVB, binding.oilTtShSVC), value) {
                sharedViewModel.updateORU220Data {
                    oilTtShSV220B = value
                    oilTtShSV220C = value
                }
            }
        }

        // 3АТГ - ТТ
        binding.btnFillAllV3atgTT.setOnClickListener {
            val value = binding.oilTt3atgA.selectedItem?.toString()
            if (value.isNullOrEmpty() || value == "Выберите") {
                Toast.makeText(requireContext(), "Сначала выберите значение в ТТ фазы А", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fillSpinners(listOf(binding.oilTt3atgB, binding.oilTt3atgC), value) {
                sharedViewModel.updateORU220Data {
                    oilTt3atgB = value
                    oilTt3atgC = value
                }
            }
        }

        // Орбита - ТТ
        binding.btnFillAllOrbitaTT.setOnClickListener {
            val value = binding.oilOrbitaA.selectedItem?.toString()
            if (value.isNullOrEmpty() || value == "Выберите") {
                Toast.makeText(requireContext(), "Сначала выберите значение в ТТ фазы А", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fillSpinners(listOf(binding.oilOrbitaB, binding.oilOrbitaC), value) {
                sharedViewModel.updateORU220Data {
                    oilOrbitaB = value
                    oilOrbitaC = value
                }
            }
        }

        // Факел - ТТ
        binding.btnFillAllFakelTT.setOnClickListener {
            val value = binding.oilFakelA.selectedItem?.toString()
            if (value.isNullOrEmpty() || value == "Выберите") {
                Toast.makeText(requireContext(), "Сначала выберите значение в ТТ фазы А", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fillSpinners(listOf(binding.oilFakelB, binding.oilFakelC), value) {
                sharedViewModel.updateORU220Data {
                    oilFakelB = value
                    oilFakelC = value
                }
            }
        }

        // Комета-2 - ТТ
        binding.btnFillAllCometa2TT.setOnClickListener {
            val value = binding.oilCometa2A.selectedItem?.toString()
            if (value.isNullOrEmpty() || value == "Выберите") {
                Toast.makeText(requireContext(), "Сначала выберите значение в ТТ фазы А", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fillSpinners(listOf(binding.oilCometa2B, binding.oilCometa2C), value) {
                sharedViewModel.updateORU220Data {
                    oilCometa2B = value
                    oilCometa2C = value
                }
            }
        }

        // Комета-1 - ТТ
        binding.btnFillAllCometa1TT.setOnClickListener {
            val value = binding.oilCometa1A.selectedItem?.toString()
            if (value.isNullOrEmpty() || value == "Выберите") {
                Toast.makeText(requireContext(), "Сначала выберите значение в ТТ фазы А", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fillSpinners(listOf(binding.oilCometa1B, binding.oilCometa1C), value) {
                sharedViewModel.updateORU220Data {
                    oilCometa1B = value
                    oilCometa1C = value
                }
            }
        }
    }

    private fun fillSpinners(spinners: List<Spinner>, value: String, onUpdate: () -> Unit) {
        isUpdatingUIFromViewModel = true

        for (spinner in spinners) {
            val adapter = spinner.adapter
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString() == value) {
                    spinner.setSelection(i, false)
                    break
                }
            }
        }

        isUpdatingUIFromViewModel = false
        onUpdate()

        Toast.makeText(requireContext(), "Заполнено ${spinners.size} полей", Toast.LENGTH_SHORT).show()
    }

    private fun fillAllTn220(
        upperA: Spinner, upperB: Spinner, upperC: Spinner,
        lowerA: Spinner, lowerB: Spinner, lowerC: Spinner,
        title: String
    ) {
        val value = upperA.selectedItem?.toString() ?: return
        if (value.isEmpty() || value == "Выберите") {
            Toast.makeText(requireContext(), "Сначала выберите значение в поле Верх А", Toast.LENGTH_SHORT).show()
            return
        }

        setSpinnerSilently(upperB, value)
        setSpinnerSilently(upperC, value)
        setSpinnerSilently(lowerA, value)
        setSpinnerSilently(lowerB, value)
        setSpinnerSilently(lowerC, value)

        Toast.makeText(requireContext(), "Все каскады $title заполнены", Toast.LENGTH_SHORT).show()
    }

    private fun setSpinnerSilently(spinner: Spinner, value: String) {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                spinner.setSelection(i, false)
                break
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}