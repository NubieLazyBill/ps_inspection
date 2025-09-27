package com.example.ps_inspection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ps_inspection.databinding.FragmentInspectionORU500Binding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InspectionORU500 : Fragment() {

    private var _binding: FragmentInspectionORU500Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()

    // Флаг для отслеживания, обновляем ли мы UI программно
    private var isUpdatingUIFromViewModel = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectionORU500Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru500Data.collectLatest { data ->
                updateUIFromData(data)
            }
        }

        setupInputListeners()
    }

    private fun updateUIFromData(data: InspectionORU500Data) {
        isUpdatingUIFromViewModel = true

        // Ячейка 1: В-500 Р-500 2С
        setSpinnerSelection(binding.purgingR5002sA1, data.purgingR5002sA1)
        setSpinnerSelection(binding.purgingR5002sB1, data.purgingR5002sB1)
        setSpinnerSelection(binding.purgingR5002sC1, data.purgingR5002sC1)
        setSpinnerSelection(binding.purgingR5002sA2, data.purgingR5002sA2)
        setSpinnerSelection(binding.purgingR5002sB2, data.purgingR5002sB2)
        setSpinnerSelection(binding.purgingR5002sC2, data.purgingR5002sC2)

        // Ячейка 2: В-500 ВШТ-31
        updateEditTextIfNeeded(binding.gasPressureVsht31A, data.gasPressureVsht31A)
        updateEditTextIfNeeded(binding.gasPressureVsht31B, data.gasPressureVsht31B)
        updateEditTextIfNeeded(binding.gasPressureVsht31C, data.gasPressureVsht31C)
        setSpinnerSelection(binding.oilTtVsht31A, data.oilTtVsht31A)
        setSpinnerSelection(binding.oilTtVsht31B, data.oilTtVsht31B)
        setSpinnerSelection(binding.oilTtVsht31C, data.oilTtVsht31C)

        // Ячейка 3: В-500 ВЛТ-30
        updateEditTextIfNeeded(binding.gasPressureVlt30A, data.gasPressureVlt30A)
        updateEditTextIfNeeded(binding.gasPressureVlt30B, data.gasPressureVlt30B)
        updateEditTextIfNeeded(binding.gasPressureVlt30C, data.gasPressureVlt30C)
        setSpinnerSelection(binding.oilTtVlt30A, data.oilTtVlt30A)
        setSpinnerSelection(binding.oilTtVlt30B, data.oilTtVlt30B)
        setSpinnerSelection(binding.oilTtVlt30C, data.oilTtVlt30C)
        setSpinnerSelection(binding.oilTtTrachukovskayaA, data.oilTtTrachukovskayaA)
        setSpinnerSelection(binding.oilTtTrachukovskayaB, data.oilTtTrachukovskayaB)
        setSpinnerSelection(binding.oilTtTrachukovskayaC, data.oilTtTrachukovskayaC)
        setSpinnerSelection(binding.oil2tnTrachukovskayaA, data.oil2tnTrachukovskayaA)
        setSpinnerSelection(binding.oil2tnTrachukovskayaB, data.oil2tnTrachukovskayaB)
        setSpinnerSelection(binding.oil2tnTrachukovskayaC, data.oil2tnTrachukovskayaC)
        setSpinnerSelection(binding.oil1tnTrachukovskayaA, data.oil1tnTrachukovskayaA)
        setSpinnerSelection(binding.oil1tnTrachukovskayaB, data.oil1tnTrachukovskayaB)
        setSpinnerSelection(binding.oil1tnTrachukovskayaC, data.oil1tnTrachukovskayaC)

        // Ячейка 4: В-500 ВШЛ-32
        setSpinnerSelection(binding.purgingVshl32A1, data.purgingVshl32A1)
        setSpinnerSelection(binding.purgingVshl32B1, data.purgingVshl32B1)
        setSpinnerSelection(binding.purgingVshl32C1, data.purgingVshl32C1)
        setSpinnerSelection(binding.purgingVshl32A2, data.purgingVshl32A2)
        setSpinnerSelection(binding.purgingVshl32B2, data.purgingVshl32B2)
        setSpinnerSelection(binding.purgingVshl32C2, data.purgingVshl32C2)
        setSpinnerSelection(binding.oilTtVshl32A, data.oilTtVshl32A)
        setSpinnerSelection(binding.oilTtVshl32B, data.oilTtVshl32B)
        setSpinnerSelection(binding.oilTtVshl32C, data.oilTtVshl32C)

        // Ячейка 5: В-500 ВШЛ-21
        setSpinnerSelection(binding.purgingVshl21A1, data.purgingVshl21A1)
        setSpinnerSelection(binding.purgingVshl21B1, data.purgingVshl21B1)
        setSpinnerSelection(binding.purgingVshl21C1, data.purgingVshl21C1)
        setSpinnerSelection(binding.purgingVshl21A2, data.purgingVshl21A2)
        setSpinnerSelection(binding.purgingVshl21B2, data.purgingVshl21B2)
        setSpinnerSelection(binding.purgingVshl21C2, data.purgingVshl21C2)
        setSpinnerSelection(binding.oilTtVshl21A, data.oilTtVshl21A)
        setSpinnerSelection(binding.oilTtVshl21B, data.oilTtVshl21B)
        setSpinnerSelection(binding.oilTtVshl21C, data.oilTtVshl21C)

        // Ячейка 6: В-500 ВШТ-22
        setSpinnerSelection(binding.purgingVsht22A1, data.purgingVsht22A1)
        setSpinnerSelection(binding.purgingVsht22B1, data.purgingVsht22B1)
        setSpinnerSelection(binding.purgingVsht22C1, data.purgingVsht22C1)
        setSpinnerSelection(binding.purgingVsht22A2, data.purgingVsht22A2)
        setSpinnerSelection(binding.purgingVsht22B2, data.purgingVsht22B2)
        setSpinnerSelection(binding.purgingVsht22C2, data.purgingVsht22C2)
        setSpinnerSelection(binding.oilTtVsht22A, data.oilTtVsht22A)
        setSpinnerSelection(binding.oilTtVsht22B, data.oilTtVsht22B)
        setSpinnerSelection(binding.oilTtVsht22C, data.oilTtVsht22C)

        // Ячейка 7: В-500 ВШТ-11
        setSpinnerSelection(binding.purgingVsht11A1, data.purgingVsht11A1)
        setSpinnerSelection(binding.purgingVsht11B1, data.purgingVsht11B1)
        setSpinnerSelection(binding.purgingVsht11C1, data.purgingVsht11C1)
        setSpinnerSelection(binding.purgingVsht11A2, data.purgingVsht11A2)
        setSpinnerSelection(binding.purgingVsht11B2, data.purgingVsht11B2)
        setSpinnerSelection(binding.purgingVsht11C2, data.purgingVsht11C2)
        setSpinnerSelection(binding.oilTtVsht11A, data.oilTtVsht11A)
        setSpinnerSelection(binding.oilTtVsht11B, data.oilTtVsht11B)
        setSpinnerSelection(binding.oilTtVsht11C, data.oilTtVsht11C)

        // Ячейка 8: В-500 ВШЛ-12
        setSpinnerSelection(binding.purgingVshl12A1, data.purgingVshl12A1)
        setSpinnerSelection(binding.purgingVshl12B1, data.purgingVshl12B1)
        setSpinnerSelection(binding.purgingVshl12C1, data.purgingVshl12C1)
        setSpinnerSelection(binding.purgingVshl12A2, data.purgingVshl12A2)
        setSpinnerSelection(binding.purgingVshl12B2, data.purgingVshl12B2)
        setSpinnerSelection(binding.purgingVshl12C2, data.purgingVshl12C2)
        setSpinnerSelection(binding.oilTtVshl12A, data.oilTtVshl12A)
        setSpinnerSelection(binding.oilTtVshl12B, data.oilTtVshl12B)
        setSpinnerSelection(binding.oilTtVshl12C, data.oilTtVshl12C)
        setSpinnerSelection(binding.oil2tnBelozernayaA, data.oil2tnBelozernayaA)
        setSpinnerSelection(binding.oil2tnBelozernayaB, data.oil2tnBelozernayaB)
        setSpinnerSelection(binding.oil2tnBelozernayaC, data.oil2tnBelozernayaC)

        // 1ТН-500 Каскады
        setSpinnerSelection(binding.tn1500Cascade1A, data.tn1500Cascade1A)
        setSpinnerSelection(binding.tn1500Cascade1B, data.tn1500Cascade1B)
        setSpinnerSelection(binding.tn1500Cascade1C, data.tn1500Cascade1C)
        setSpinnerSelection(binding.tn1500Cascade2A, data.tn1500Cascade2A)
        setSpinnerSelection(binding.tn1500Cascade2B, data.tn1500Cascade2B)
        setSpinnerSelection(binding.tn1500Cascade2C, data.tn1500Cascade2C)
        setSpinnerSelection(binding.tn1500Cascade3A, data.tn1500Cascade3A)
        setSpinnerSelection(binding.tn1500Cascade3B, data.tn1500Cascade3B)
        setSpinnerSelection(binding.tn1500Cascade3C, data.tn1500Cascade3C)
        setSpinnerSelection(binding.tn1500Cascade4A, data.tn1500Cascade4A)
        setSpinnerSelection(binding.tn1500Cascade4B, data.tn1500Cascade4B)
        setSpinnerSelection(binding.tn1500Cascade4C, data.tn1500Cascade4C)

        // 2ТН-500 Каскады
        setSpinnerSelection(binding.tn2500Cascade1A, data.tn2500Cascade1A)
        setSpinnerSelection(binding.tn2500Cascade1B, data.tn2500Cascade1B)
        setSpinnerSelection(binding.tn2500Cascade1C, data.tn2500Cascade1C)
        setSpinnerSelection(binding.tn2500Cascade2A, data.tn2500Cascade2A)
        setSpinnerSelection(binding.tn2500Cascade2B, data.tn2500Cascade2B)
        setSpinnerSelection(binding.tn2500Cascade2C, data.tn2500Cascade2C)
        setSpinnerSelection(binding.tn2500Cascade3A, data.tn2500Cascade3A)
        setSpinnerSelection(binding.tn2500Cascade3B, data.tn2500Cascade3B)
        setSpinnerSelection(binding.tn2500Cascade3C, data.tn2500Cascade3C)
        setSpinnerSelection(binding.tn2500Cascade4A, data.tn2500Cascade4A)
        setSpinnerSelection(binding.tn2500Cascade4B, data.tn2500Cascade4B)
        setSpinnerSelection(binding.tn2500Cascade4C, data.tn2500Cascade4C)

        // ТН-500 СГРЭС-1 Каскады
        setSpinnerSelection(binding.tn500Sgres1Cascade1A, data.tn500Sgres1Cascade1A)
        setSpinnerSelection(binding.tn500Sgres1Cascade1B, data.tn500Sgres1Cascade1B)
        setSpinnerSelection(binding.tn500Sgres1Cascade1C, data.tn500Sgres1Cascade1C)
        setSpinnerSelection(binding.tn500Sgres1Cascade2A, data.tn500Sgres1Cascade2A)
        setSpinnerSelection(binding.tn500Sgres1Cascade2B, data.tn500Sgres1Cascade2B)
        setSpinnerSelection(binding.tn500Sgres1Cascade2C, data.tn500Sgres1Cascade2C)
        setSpinnerSelection(binding.tn500Sgres1Cascade3A, data.tn500Sgres1Cascade3A)
        setSpinnerSelection(binding.tn500Sgres1Cascade3B, data.tn500Sgres1Cascade3B)
        setSpinnerSelection(binding.tn500Sgres1Cascade3C, data.tn500Sgres1Cascade3C)
        setSpinnerSelection(binding.tn500Sgres1Cascade4A, data.tn500Sgres1Cascade4A)
        setSpinnerSelection(binding.tn500Sgres1Cascade4B, data.tn500Sgres1Cascade4B)
        setSpinnerSelection(binding.tn500Sgres1Cascade4C, data.tn500Sgres1Cascade4C)

        isUpdatingUIFromViewModel = false
    }

    private fun updateEditTextIfNeeded(editText: android.widget.EditText, newValue: String) {
        val currentText = editText.text.toString()
        if (currentText != newValue) {
            editText.setText(newValue)
            editText.setSelection(editText.text.length)
        }
    }

    private fun setSpinnerSelection(spinner: android.widget.Spinner, value: String) {
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
        // Слушатели для EditText
        setupEditTextListener(binding.gasPressureVsht31A) { text ->
            sharedViewModel.updateORU500Data { gasPressureVsht31A = text }
        }
        setupEditTextListener(binding.gasPressureVsht31B) { text ->
            sharedViewModel.updateORU500Data { gasPressureVsht31B = text }
        }
        setupEditTextListener(binding.gasPressureVsht31C) { text ->
            sharedViewModel.updateORU500Data { gasPressureVsht31C = text }
        }
        setupEditTextListener(binding.gasPressureVlt30A) { text ->
            sharedViewModel.updateORU500Data { gasPressureVlt30A = text }
        }
        setupEditTextListener(binding.gasPressureVlt30B) { text ->
            sharedViewModel.updateORU500Data { gasPressureVlt30B = text }
        }
        setupEditTextListener(binding.gasPressureVlt30C) { text ->
            sharedViewModel.updateORU500Data { gasPressureVlt30C = text }
        }

        // Ячейка 1: В-500 Р-500 2С
        setupSpinnerListener(binding.purgingR5002sA1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sA1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingR5002sB1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sB1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingR5002sC1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sC1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingR5002sA2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sA2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingR5002sB2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sB2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingR5002sC2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sC2 = selectedItem.toString() }
        }

        // Ячейка 2: В-500 ВШТ-31
        setupSpinnerListener(binding.oilTtVsht31A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht31A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht31B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht31B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht31C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht31C = selectedItem.toString() }
        }

        // Ячейка 3: В-500 ВЛТ-30
        setupSpinnerListener(binding.oilTtVlt30A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVlt30A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVlt30B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVlt30B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVlt30C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVlt30C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtTrachukovskayaA) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtTrachukovskayaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtTrachukovskayaB) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtTrachukovskayaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtTrachukovskayaC) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtTrachukovskayaC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnTrachukovskayaA) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnTrachukovskayaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnTrachukovskayaB) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnTrachukovskayaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnTrachukovskayaC) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnTrachukovskayaC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil1tnTrachukovskayaA) { selectedItem ->
            sharedViewModel.updateORU500Data { oil1tnTrachukovskayaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil1tnTrachukovskayaB) { selectedItem ->
            sharedViewModel.updateORU500Data { oil1tnTrachukovskayaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil1tnTrachukovskayaC) { selectedItem ->
            sharedViewModel.updateORU500Data { oil1tnTrachukovskayaC = selectedItem.toString() }
        }

        // Ячейка 4: В-500 ВШЛ-32
        setupSpinnerListener(binding.purgingVshl32A1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32A1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl32B1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32B1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl32C1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32C1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl32A2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32A2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl32B2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32B2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl32C2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32C2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl32A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl32A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl32B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl32B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl32C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl32C = selectedItem.toString() }
        }

        // Ячейка 5: В-500 ВШЛ-21
        setupSpinnerListener(binding.purgingVshl21A1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21A1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl21B1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21B1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl21C1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21C1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl21A2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21A2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl21B2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21B2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl21C2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21C2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl21A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl21A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl21B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl21B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl21C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl21C = selectedItem.toString() }
        }

        // Ячейка 6: В-500 ВШТ-22
        setupSpinnerListener(binding.purgingVsht22A1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22A1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht22B1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22B1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht22C1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22C1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht22A2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22A2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht22B2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22B2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht22C2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22C2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht22A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht22A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht22B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht22B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht22C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht22C = selectedItem.toString() }
        }

        // Ячейка 7: В-500 ВШТ-11
        setupSpinnerListener(binding.purgingVsht11A1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11A1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht11B1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11B1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht11C1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11C1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht11A2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11A2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht11B2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11B2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht11C2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11C2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht11A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht11A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht11B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht11B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht11C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht11C = selectedItem.toString() }
        }

        // Ячейка 8: В-500 ВШЛ-12
        setupSpinnerListener(binding.purgingVshl12A1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12A1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl12B1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12B1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl12C1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12C1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl12A2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12A2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl12B2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12B2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl12C2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12C2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl12A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl12A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl12B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl12B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl12C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl12C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnBelozernayaA) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnBelozernayaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnBelozernayaB) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnBelozernayaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnBelozernayaC) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnBelozernayaC = selectedItem.toString() }
        }

        // 1ТН-500 Каскады
        setupSpinnerListener(binding.tn1500Cascade1A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade1A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade1B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade1B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade1C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade1C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade2A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade2A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade2B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade2B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade2C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade2C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade3A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade3A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade3B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade3B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade3C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade3C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade4A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade4A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade4B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade4B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade4C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade4C = selectedItem.toString() }
        }

        // 2ТН-500 Каскады
        setupSpinnerListener(binding.tn2500Cascade1A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade1A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade1B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade1B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade1C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade1C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade2A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade2A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade2B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade2B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade2C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade2C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade3A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade3A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade3B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade3B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade3C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade3C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade4A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade4A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade4B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade4B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade4C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade4C = selectedItem.toString() }
        }

        // ТН-500 СГРЭС-1 Каскады
        setupSpinnerListener(binding.tn500Sgres1Cascade1A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade1A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade1B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade1B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade1C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade1C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade2A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade2A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade2B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade2B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade2C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade2C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade3A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade3A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade3B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade3B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade3C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade3C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade4A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade4A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade4B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade4B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade4C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade4C = selectedItem.toString() }
        }
    }

    private fun setupEditTextListener(editText: android.widget.EditText, onTextChanged: (String) -> Unit) {
        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingUIFromViewModel) return
                val newText = s?.toString() ?: ""
                onTextChanged(newText)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

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
        @JvmStatic
        fun newInstance() = InspectionORU500()
    }
}