package com.example.ps_inspection.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.androidplot.xy.LineAndPointFormatter
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYPlot
import com.example.ps_inspection.data.services.GoogleSheetsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class GraphsFragment : Fragment() {

    private lateinit var equipmentSpinner: Spinner
    private lateinit var parameterSpinner: Spinner
    private lateinit var plot: XYPlot

    private val equipmentList = listOf(
        "2 АТГ ф.С", "2 АТГ ф.В", "2 АТГ ф.А",
        "3 АТГ ф.С", "3 АТГ ф.В", "3 АТГ ф.А",
        "Реактор ф.С", "Реактор ф.В", "Реактор ф.А"
    )

    private val parameterList = listOf(
        "Температура масла ТС1",
        "Температура масла ТС2",
        "Уровень масла (бак)",
        "Давление 500"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        equipmentSpinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, equipmentList)
        }
        root.addView(equipmentSpinner)

        parameterSpinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, parameterList)
        }
        root.addView(parameterSpinner)

        plot = XYPlot(requireContext(), "График показаний").apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(16, 16, 16, 16)
            setDomainLabel("Дата осмотра")
            setRangeLabel("Значение")
        }
        root.addView(plot)

        val listener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadData()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        equipmentSpinner.onItemSelectedListener = listener
        parameterSpinner.onItemSelectedListener = listener

        return root
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val sheetsService = GoogleSheetsService(requireContext())
                val allData = withContext(Dispatchers.IO) {
                    sheetsService.getAllInspections()
                }

                if (allData.isNullOrEmpty()) return@launch

                val selectedEquipment = equipmentList[equipmentSpinner.selectedItemPosition]
                val selectedParam = parameterList[parameterSpinner.selectedItemPosition]
                val key = getKeyForEquipment(selectedEquipment, selectedParam)

                val seriesData = mutableListOf<Number>()
                val dates = mutableListOf<String>()

                allData.forEach { row ->
                    val value = row[key]?.replace(",", ".")?.toFloatOrNull()
                    if (value != null) {
                        seriesData.add(value)
                        val date = row["Дата"] ?: ""
                        dates.add(date)
                    }
                }

                if (seriesData.isNotEmpty()) {
                    val series = SimpleXYSeries(seriesData, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, selectedParam)
                    val formatter = LineAndPointFormatter(
                        Color.parseColor("#4CAF50"),  // цвет линии
                        Color.parseColor("#FF5722"),  // цвет точек
                        Color.TRANSPARENT,            // цвет заливки
                        null
                    )
                    plot.clear()
                    plot.addSeries(series, formatter)
                    plot.redraw()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getKeyForEquipment(equipment: String, parameter: String): String {
        return when {
            equipment == "2 АТГ ф.С" -> {
                when (parameter) {
                    "Температура масла ТС1" -> "2 АТГ С ТС1"
                    "Температура масла ТС2" -> "2 АТГ С ТС2"
                    "Уровень масла (бак)" -> "2 АТГ С бак"
                    "Давление 500" -> "2 АТГ С давл500"
                    else -> ""
                }
            }
            // Добавим остальные позже
            else -> ""
        }
    }
}