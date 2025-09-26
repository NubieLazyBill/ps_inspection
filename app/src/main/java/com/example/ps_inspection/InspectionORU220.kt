package com.example.ps_inspection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ps_inspection.databinding.FragmentInspectionORU220Binding
import kotlinx.coroutines.launch

class InspectionORU220 : Fragment() {

    private var _binding: FragmentInspectionORU220Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectionORU220Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru220Data.collect { data ->
                updateUIFromData(data)
            }
        }

        setupInputListeners()
    }

    private fun updateUIFromData(data: InspectionORU220Data) {
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
    }

    private fun setSpinnerSelection(spinner: android.widget.Spinner, value: String) {
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

        // Остальные секции добавляются по аналогии...
        // Для экономии времени покажу только первые секции

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

        // ТН-220 ОСШ ф.В
        setupSpinnerListener(binding.tnOsshFvUpper) { selectedItem ->
            sharedViewModel.updateORU220Data { tnOsshFvUpper = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tnOsshFvLower) { selectedItem ->
            sharedViewModel.updateORU220Data { tnOsshFvLower = selectedItem.toString() }
        }

        // В-220 2АТГ
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

        // ШСВ-220
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

// В-220 3АТГ
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

        // Остальные секции добавляются по тому же принципу...
    }

    // Функция для настройки слушателя Spinner
    private fun setupSpinnerListener(spinner: android.widget.Spinner, onItemSelected: (Any?) -> Unit) {
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

    companion object {
        fun newInstance() = InspectionORU220()
    }
}