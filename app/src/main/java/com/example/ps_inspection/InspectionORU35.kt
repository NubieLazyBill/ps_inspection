package com.example.ps_inspection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ps_inspection.databinding.FragmentInspectionORU35Binding
import kotlinx.coroutines.launch

class InspectionORU35 : Fragment() {

    private var _binding: FragmentInspectionORU35Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectionORU35Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru35Data.collect { data ->
                updateUIFromData(data)
            }
        }

        setupInputListeners()
    }

    private fun updateUIFromData(data: InspectionORU35Data) {
        binding.tsn2Input.setText(data.tsn2)
        binding.tsn3Input.setText(data.tsn3)
        binding.tsn4Input.setText(data.tsn4)
        binding.tn352atgInput.setText(data.tn352atg)
        binding.tn353atgInput.setText(data.tn353atg)

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
        // Слушатели для EditText - ИСПРАВЛЕННАЯ ВЕРСИЯ
        setupEditTextListener(binding.tsn2Input) { text ->
            sharedViewModel.updateORU35Data { tsn2 = text }
        }
        setupEditTextListener(binding.tsn3Input) { text ->
            sharedViewModel.updateORU35Data { tsn3 = text }
        }
        setupEditTextListener(binding.tsn4Input) { text ->
            sharedViewModel.updateORU35Data { tsn4 = text }
        }
        setupEditTextListener(binding.tn352atgInput) { text ->
            sharedViewModel.updateORU35Data { tn352atg = text }
        }
        setupEditTextListener(binding.tn353atgInput) { text ->
            sharedViewModel.updateORU35Data { tn353atg = text }
        }

        // Слушатели для Spinner'ов
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

    // ИСПРАВЛЕННАЯ функция для EditText
    private fun setupEditTextListener(editText: android.widget.EditText, onTextChanged: (String) -> Unit) {
        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChanged(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
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
        fun newInstance() = InspectionORU35()
    }
}