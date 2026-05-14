package com.example.ps_inspection.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.androidplot.xy.LineAndPointFormatter
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYPlot
import com.example.ps_inspection.R
import com.example.ps_inspection.data.services.GoogleSheetsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.graphics.toColorInt

class GraphsFragment : Fragment() {

    private lateinit var spinnerSection: Spinner
    private lateinit var spinnerEquipment: Spinner
    private lateinit var spinnerParameter: Spinner
    private lateinit var plot: XYPlot
    private lateinit var tvTitle: TextView
    private lateinit var tvEmpty: TextView

    // ===== ВСЕ ДАННЫЕ =====

    private val sectionList = listOf("ОРУ-35 кВ", "ОРУ-220 кВ", "ОРУ-500 кВ", "АТГ + Реактор", "Здания")

    // equipmentMap: секция -> список ТОЛЬКО названий оборудования
    private val equipmentNamesMap: Map<String, List<String>> = mapOf(
        "ОРУ-35 кВ" to listOf("ТСН", "ТТ-35 2ТСН", "ТТ-35 3ТСН", "В-35 2ТСН", "В-35 3ТСН", "ТН-35"),
        "ОРУ-220 кВ" to listOf("Мирная", "Топаз", "ОВ", "ТН-220 ОСШ", "2АТГ", "ШСВ", "3АТГ", "Орбита", "Факел", "Комета-1", "Комета-2", "1ТН-220", "2ТН-220"),
        "ОРУ-500 кВ" to listOf("Р-500 2С", "ВШТ-31", "ВЛТ-30", "ВШЛ-32", "ВШЛ-21", "ВШТ-22", "ВЛТ-20", "ВШТ-11", "ВШЛ-12", "ТТ-500 Трачуковская", "1ТН-500", "2ТН-500", "СГРЭС-1", "ТН-500 Трачуковская", "ТН-500 Белозёрная"),
        "АТГ + Реактор" to listOf("2АТГ ф.С", "2АТГ ф.В", "2АТГ ф.А", "АТГ резерв", "3АТГ ф.С", "3АТГ ф.В", "3АТГ ф.А", "Р-500 2С ф.С", "Р-500 2С ф.В", "Р-500 2С ф.А"),
        "Здания" to listOf("Компрессорная №1", "Баллонная №1", "Компрессорная №2", "Баллонная №2", "КПЗ ОПУ", "КПЗ-2", "Насосная пожаротушения", "Мастерская по ремонту ВВ", "Артскважина", "Здание артезианской скважины", "Помещение АБ №1,2", "Помещение п/этажа №1,2,3")
    )

    // paramsMap: "Секция|Оборудование" -> список пар ("название параметра" -> список ключей Google Sheets)
    private val paramsMap: Map<String, List<Pair<String, List<String>>>> = mapOf(

        // ==================== ОРУ-35 ====================
        "ОРУ-35 кВ|ТСН" to listOf(
            "2ТСН уровень масла" to listOf("ОРУ-35 2ТСН"),
            "3ТСН уровень масла" to listOf("ОРУ-35 3ТСН"),
            "4ТСН уровень масла" to listOf("ОРУ-35 4ТСН")
        ),
        "ОРУ-35 кВ|ТТ-35 2ТСН" to listOf(
            "ф.А уровень масла" to listOf("ОРУ-35 ТТ-35 2ТСН А"),
            "ф.В уровень масла" to listOf("ОРУ-35 ТТ-35 2ТСН В"),
            "ф.С уровень масла" to listOf("ОРУ-35 ТТ-35 2ТСН С")
        ),
        "ОРУ-35 кВ|ТТ-35 3ТСН" to listOf(
            "ф.А уровень масла" to listOf("ОРУ-35 ТТ-35 3ТСН А"),
            "ф.В уровень масла" to listOf("ОРУ-35 ТТ-35 3ТСН В"),
            "ф.С уровень масла" to listOf("ОРУ-35 ТТ-35 3ТСН С")
        ),
        "ОРУ-35 кВ|В-35 2ТСН" to listOf(
            "ф.А продувка" to listOf("ОРУ-35 В-35 2ТСН А"),
            "ф.В продувка" to listOf("ОРУ-35 В-35 2ТСН В"),
            "ф.С продувка" to listOf("ОРУ-35 В-35 2ТСН С")
        ),
        "ОРУ-35 кВ|В-35 3ТСН" to listOf(
            "ф.А продувка" to listOf("ОРУ-35 В-35 3ТСН А"),
            "ф.В продувка" to listOf("ОРУ-35 В-35 3ТСН В"),
            "ф.С продувка" to listOf("ОРУ-35 В-35 3ТСН С")
        ),
        "ОРУ-35 кВ|ТН-35" to listOf(
            "2АТГ уровень масла" to listOf("ТН-35 2АТГ"),
            "3АТГ уровень масла" to listOf("ТН-35 3АТГ")
        ),

        // ==================== ОРУ-220 ====================
        "ОРУ-220 кВ|Мирная" to listOf(
            "ф.А продувка" to listOf("Мирная прод А"),
            "ф.В продувка" to listOf("Мирная прод В"),
            "ф.С продувка" to listOf("Мирная прод С"),
            "ф.А ТТ уровень масла" to listOf("Мирная ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("Мирная ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("Мирная ТТ масло С")
        ),
        "ОРУ-220 кВ|Топаз" to listOf(
            "ф.А продувка" to listOf("Топаз прод А"),
            "ф.В продувка" to listOf("Топаз прод В"),
            "ф.С продувка" to listOf("Топаз прод С"),
            "ф.А ТТ уровень масла" to listOf("Топаз ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("Топаз ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("Топаз ТТ масло С")
        ),
        "ОРУ-220 кВ|ОВ" to listOf(
            "ф.А продувка" to listOf("ОВ прод А"),
            "ф.В продувка" to listOf("ОВ прод В"),
            "ф.С продувка" to listOf("ОВ прод С"),
            "ф.А ТТ уровень масла" to listOf("ОВ ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("ОВ ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("ОВ ТТ масло С")
        ),
        "ОРУ-220 кВ|ТН-220 ОСШ" to listOf(
            "Верх уровень масла" to listOf("ТН-220 ОСШ верх"),
            "Низ уровень масла" to listOf("ТН-220 ОСШ низ")
        ),
        "ОРУ-220 кВ|2АТГ" to listOf(
            "ф.А продувка" to listOf("В-220 2АТГ прод А"),
            "ф.В продувка" to listOf("В-220 2АТГ прод В"),
            "ф.С продувка" to listOf("В-220 2АТГ прод С"),
            "ф.А ТТ уровень масла" to listOf("2АТГ ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("2АТГ ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("2АТГ ТТ масло С")
        ),
        "ОРУ-220 кВ|ШСВ" to listOf(
            "ф.А продувка" to listOf("ШСВ-220 прод А"),
            "ф.В продувка" to listOf("ШСВ-220 прод В"),
            "ф.С продувка" to listOf("ШСВ-220 прод С"),
            "ф.А ТТ уровень масла" to listOf("ШСВ ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("ШСВ ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("ШСВ ТТ масло С")
        ),
        "ОРУ-220 кВ|3АТГ" to listOf(
            "ф.А продувка" to listOf("В-220 3АТГ прод А"),
            "ф.В продувка" to listOf("В-220 3АТГ прод В"),
            "ф.С продувка" to listOf("В-220 3АТГ прод С"),
            "ф.А ТТ уровень масла" to listOf("3АТГ ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("3АТГ ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("3АТГ ТТ масло С")
        ),
        "ОРУ-220 кВ|Орбита" to listOf(
            "ф.А продувка" to listOf("Орбита прод А"),
            "ф.В продувка" to listOf("Орбита прод В"),
            "ф.С продувка" to listOf("Орбита прод С"),
            "ф.А ТТ уровень масла" to listOf("Орбита ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("Орбита ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("Орбита ТТ масло С")
        ),
        "ОРУ-220 кВ|Факел" to listOf(
            "ф.А продувка" to listOf("Факел прод А"),
            "ф.В продувка" to listOf("Факел прод В"),
            "ф.С продувка" to listOf("Факел прод С"),
            "ф.А ТТ уровень масла" to listOf("Факел ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("Факел ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("Факел ТТ масло С")
        ),
        "ОРУ-220 кВ|Комета-1" to listOf(
            "ф.А продувка" to listOf("Комета-1 прод А"),
            "ф.В продувка" to listOf("Комета-1 прод В"),
            "ф.С продувка" to listOf("Комета-1 прод С"),
            "ф.А ТТ уровень масла" to listOf("Комета-1 ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("Комета-1 ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("Комета-1 ТТ масло С")
        ),
        "ОРУ-220 кВ|Комета-2" to listOf(
            "ф.А продувка" to listOf("Комета-2 прод А"),
            "ф.В продувка" to listOf("Комета-2 прод В"),
            "ф.С продувка" to listOf("Комета-2 прод С"),
            "ф.А ТТ уровень масла" to listOf("Комета-2 ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("Комета-2 ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("Комета-2 ТТ масло С")
        ),
        "ОРУ-220 кВ|1ТН-220" to listOf(
            "ф.А верх уровень масла" to listOf("1ТН-220 верх А"),
            "ф.В верх уровень масла" to listOf("1ТН-220 верх В"),
            "ф.С верх уровень масла" to listOf("1ТН-220 верх С"),
            "ф.А низ уровень масла" to listOf("1ТН-220 низ А"),
            "ф.В низ уровень масла" to listOf("1ТН-220 низ В"),
            "ф.С низ уровень масла" to listOf("1ТН-220 низ С")
        ),
        "ОРУ-220 кВ|2ТН-220" to listOf(
            "ф.А верх уровень масла" to listOf("2ТН-220 верх А"),
            "ф.В верх уровень масла" to listOf("2ТН-220 верх В"),
            "ф.С верх уровень масла" to listOf("2ТН-220 верх С"),
            "ф.А низ уровень масла" to listOf("2ТН-220 низ А"),
            "ф.В низ уровень масла" to listOf("2ТН-220 низ В"),
            "ф.С низ уровень масла" to listOf("2ТН-220 низ С")
        ),

        // ==================== ОРУ-500 ====================
        "ОРУ-500 кВ|Р-500 2С" to listOf(
            "ф.А I эл. продувка" to listOf("Р-500 2С I А1"),
            "ф.В I эл. продувка" to listOf("Р-500 2С I В1"),
            "ф.С I эл. продувка" to listOf("Р-500 2С I С1"),
            "ф.А II эл. продувка" to listOf("Р-500 2С II А2"),
            "ф.В II эл. продувка" to listOf("Р-500 2С II В2"),
            "ф.С II эл. продувка" to listOf("Р-500 2С II С2")
        ),
        "ОРУ-500 кВ|ВШТ-31" to listOf(
            "ф.А продувка" to listOf("ВШТ-31 газ А"),
            "ф.В продувка" to listOf("ВШТ-31 газ В"),
            "ф.С продувка" to listOf("ВШТ-31 газ С"),
            "ф.А ТТ уровень масла" to listOf("ВШТ-31 ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("ВШТ-31 ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("ВШТ-31 ТТ масло С")
        ),
        "ОРУ-500 кВ|ВЛТ-30" to listOf(
            "ф.А продувка" to listOf("ВЛТ-30 газ А"),
            "ф.В продувка" to listOf("ВЛТ-30 газ В"),
            "ф.С продувка" to listOf("ВЛТ-30 газ С"),
            "ф.А ТТ уровень масла" to listOf("ВЛТ-30 ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("ВЛТ-30 ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("ВЛТ-30 ТТ масло С")
        ),
        "ОРУ-500 кВ|ВШЛ-32" to listOf(
            "ф.А I эл. продувка" to listOf("ВШЛ-32 I А1"),
            "ф.В I эл. продувка" to listOf("ВШЛ-32 I В1"),
            "ф.С I эл. продувка" to listOf("ВШЛ-32 I С1"),
            "ф.А II эл. продувка" to listOf("ВШЛ-32 II А2"),
            "ф.В II эл. продувка" to listOf("ВШЛ-32 II В2"),
            "ф.С II эл. продувка" to listOf("ВШЛ-32 II С2"),
            "ф.А ТТ уровень масла" to listOf("ВШЛ-32 ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("ВШЛ-32 ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("ВШЛ-32 ТТ масло С")
        ),
        "ОРУ-500 кВ|ВШЛ-21" to listOf(
            "ф.А I эл. продувка" to listOf("ВШЛ-21 I А1"),
            "ф.В I эл. продувка" to listOf("ВШЛ-21 I В1"),
            "ф.С I эл. продувка" to listOf("ВШЛ-21 I С1"),
            "ф.А II эл. продувка" to listOf("ВШЛ-21 II А2"),
            "ф.В II эл. продувка" to listOf("ВШЛ-21 II В2"),
            "ф.С II эл. продувка" to listOf("ВШЛ-21 II С2"),
            "ф.А ТТ уровень масла" to listOf("ВШЛ-21 ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("ВШЛ-21 ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("ВШЛ-21 ТТ масло С")
        ),
        "ОРУ-500 кВ|ВШТ-22" to listOf(
            "ф.А I эл. продувка" to listOf("ВШТ-22 I А1"),
            "ф.В I эл. продувка" to listOf("ВШТ-22 I В1"),
            "ф.С I эл. продувка" to listOf("ВШТ-22 I С1"),
            "ф.А II эл. продувка" to listOf("ВШТ-22 II А2"),
            "ф.В II эл. продувка" to listOf("ВШТ-22 II В2"),
            "ф.С II эл. продувка" to listOf("ВШТ-22 II С2"),
            "ф.А ТТ уровень масла" to listOf("ВШТ-22 ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("ВШТ-22 ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("ВШТ-22 ТТ масло С")
        ),
        "ОРУ-500 кВ|ВЛТ-20" to listOf(
            "ф.А I эл. продувка" to listOf("ВЛТ-20 I А1"),
            "ф.В I эл. продувка" to listOf("ВЛТ-20 I В1"),
            "ф.С I эл. продувка" to listOf("ВЛТ-20 I С1"),
            "ф.А II эл. продувка" to listOf("ВЛТ-20 II А2"),
            "ф.В II эл. продувка" to listOf("ВЛТ-20 II В2"),
            "ф.С II эл. продувка" to listOf("ВЛТ-20 II С2"),
            "ф.А ТТ уровень масла" to listOf("ВЛТ-20 ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("ВЛТ-20 ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("ВЛТ-20 ТТ масло С")
        ),
        "ОРУ-500 кВ|ВШТ-11" to listOf(
            "ф.А I эл. продувка" to listOf("ВШТ-11 I А1"),
            "ф.В I эл. продувка" to listOf("ВШТ-11 I В1"),
            "ф.С I эл. продувка" to listOf("ВШТ-11 I С1"),
            "ф.А II эл. продувка" to listOf("ВШТ-11 II А2"),
            "ф.В II эл. продувка" to listOf("ВШТ-11 II В2"),
            "ф.С II эл. продувка" to listOf("ВШТ-11 II С2"),
            "ф.А ТТ уровень масла" to listOf("ВШТ-11 ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("ВШТ-11 ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("ВШТ-11 ТТ масло С")
        ),
        "ОРУ-500 кВ|ВШЛ-12" to listOf(
            "ф.А I эл. продувка" to listOf("ВШЛ-12 I А1"),
            "ф.В I эл. продувка" to listOf("ВШЛ-12 I В1"),
            "ф.С I эл. продувка" to listOf("ВШЛ-12 I С1"),
            "ф.А II эл. продувка" to listOf("ВШЛ-12 II А2"),
            "ф.В II эл. продувка" to listOf("ВШЛ-12 II В2"),
            "ф.С II эл. продувка" to listOf("ВШЛ-12 II С2"),
            "ф.А ТТ уровень масла" to listOf("ВШЛ-12 ТТ масло А"),
            "ф.В ТТ уровень масла" to listOf("ВШЛ-12 ТТ масло В"),
            "ф.С ТТ уровень масла" to listOf("ВШЛ-12 ТТ масло С")
        ),
        "ОРУ-500 кВ|ТТ-500 Трачуковская" to listOf(
            "ф.А уровень масла" to listOf("Трачуковская ТТ масло А"),
            "ф.В уровень масла" to listOf("Трачуковская ТТ масло В"),
            "ф.С уровень масла" to listOf("Трачуковская ТТ масло С")
        ),
        "ОРУ-500 кВ|1ТН-500" to listOf(
            "каск1 ф.А уровень масла" to listOf("1ТН-500 каск1 А"),
            "каск1 ф.В уровень масла" to listOf("1ТН-500 каск1 В"),
            "каск1 ф.С уровень масла" to listOf("1ТН-500 каск1 С"),
            "каск2 ф.А уровень масла" to listOf("1ТН-500 каск2 А"),
            "каск2 ф.В уровень масла" to listOf("1ТН-500 каск2 В"),
            "каск2 ф.С уровень масла" to listOf("1ТН-500 каск2 С"),
            "каск3 ф.А уровень масла" to listOf("1ТН-500 каск3 А"),
            "каск3 ф.В уровень масла" to listOf("1ТН-500 каск3 В"),
            "каск3 ф.С уровень масла" to listOf("1ТН-500 каск3 С"),
            "каск4 ф.А уровень масла" to listOf("1ТН-500 каск4 А"),
            "каск4 ф.В уровень масла" to listOf("1ТН-500 каск4 В"),
            "каск4 ф.С уровень масла" to listOf("1ТН-500 каск4 С")
        ),
        "ОРУ-500 кВ|2ТН-500" to listOf(
            "каск1 ф.А уровень масла" to listOf("2ТН-500 каск1 А"),
            "каск1 ф.В уровень масла" to listOf("2ТН-500 каск1 В"),
            "каск1 ф.С уровень масла" to listOf("2ТН-500 каск1 С"),
            "каск2 ф.А уровень масла" to listOf("2ТН-500 каск2 А"),
            "каск2 ф.В уровень масла" to listOf("2ТН-500 каск2 В"),
            "каск2 ф.С уровень масла" to listOf("2ТН-500 каск2 С"),
            "каск3 ф.А уровень масла" to listOf("2ТН-500 каск3 А"),
            "каск3 ф.В уровень масла" to listOf("2ТН-500 каск3 В"),
            "каск3 ф.С уровень масла" to listOf("2ТН-500 каск3 С"),
            "каск4 ф.А уровень масла" to listOf("2ТН-500 каск4 А"),
            "каск4 ф.В уровень масла" to listOf("2ТН-500 каск4 В"),
            "каск4 ф.С уровень масла" to listOf("2ТН-500 каск4 С")
        ),
        "ОРУ-500 кВ|СГРЭС-1" to listOf(
            "каск1 ф.А уровень масла" to listOf("СГРЭС-1 каск1 А"),
            "каск1 ф.В уровень масла" to listOf("СГРЭС-1 каск1 В"),
            "каск1 ф.С уровень масла" to listOf("СГРЭС-1 каск1 С"),
            "каск2 ф.А уровень масла" to listOf("СГРЭС-1 каск2 А"),
            "каск2 ф.В уровень масла" to listOf("СГРЭС-1 каск2 В"),
            "каск2 ф.С уровень масла" to listOf("СГРЭС-1 каск2 С"),
            "каск3 ф.А уровень масла" to listOf("СГРЭС-1 каск3 А"),
            "каск3 ф.В уровень масла" to listOf("СГРЭС-1 каск3 В"),
            "каск3 ф.С уровень масла" to listOf("СГРЭС-1 каск3 С"),
            "каск4 ф.А уровень масла" to listOf("СГРЭС-1 каск4 А"),
            "каск4 ф.В уровень масла" to listOf("СГРЭС-1 каск4 В"),
            "каск4 ф.С уровень масла" to listOf("СГРЭС-1 каск4 С")
        ),
        "ОРУ-500 кВ|ТН-500 Трачуковская" to listOf(
            "1ТН ф.А уровень масла" to listOf("Трачук 1ТН масло А"),
            "1ТН ф.В уровень масла" to listOf("Трачук 1ТН масло В"),
            "1ТН ф.С уровень масла" to listOf("Трачук 1ТН масло С"),
            "2ТН ф.А уровень масла" to listOf("Трачук 2ТН масло А"),
            "2ТН ф.В уровень масла" to listOf("Трачук 2ТН масло В"),
            "2ТН ф.С уровень масла" to listOf("Трачук 2ТН масло С")
        ),
        "ОРУ-500 кВ|ТН-500 Белозёрная" to listOf(
            "ф.А уровень масла" to listOf("Белозёрная 2ТН масло А"),
            "ф.В уровень масла" to listOf("Белозёрная 2ТН масло В"),
            "ф.С уровень масла" to listOf("Белозёрная 2ТН масло С")
        ),

        // ==================== АТГ + Реактор (без изменений, там нет АВС) ====================
        "АТГ + Реактор|2АТГ ф.С" to listOf(
            "Уровень масла БАК" to listOf("2 АТГ С бак"),
            "Уровень масла РПН" to listOf("2 АТГ С РПН"),
            "Давление во вводе 500кВ" to listOf("2 АТГ С давл500"),
            "Давление во вводе 220кВ" to listOf("2 АТГ С давл220"),
            "Температура масла ТС1" to listOf("2 АТГ С ТС1"),
            "Температура масла ТС2" to listOf("2 АТГ С ТС2"),
            "1гр. охл. давление" to listOf("2 АТГ С насос1"),
            "2гр. охл. давление" to listOf("2 АТГ С насос2"),
            "3гр. охл. давление" to listOf("2 АТГ С насос3"),
            "4гр. охл. давление" to listOf("2 АТГ С насос4")
        ),
        "АТГ + Реактор|2АТГ ф.В" to listOf(
            "Уровень масла БАК" to listOf("2 АТГ В бак"), "Уровень масла РПН" to listOf("2 АТГ В РПН"),
            "Давление во вводе 500кВ" to listOf("2 АТГ В давл500"), "Давление во вводе 220кВ" to listOf("2 АТГ В давл220"),
            "Температура масла ТС1" to listOf("2 АТГ В ТС1"), "Температура масла ТС2" to listOf("2 АТГ В ТС2"),
            "1гр. охл. давление" to listOf("2 АТГ В насос1"), "2гр. охл. давление" to listOf("2 АТГ В насос2"),
            "3гр. охл. давление" to listOf("2 АТГ В насос3"), "4гр. охл. давление" to listOf("2 АТГ В насос4")
        ),
        "АТГ + Реактор|2АТГ ф.А" to listOf(
            "Уровень масла БАК" to listOf("2 АТГ А бак"), "Уровень масла РПН" to listOf("2 АТГ А РПН"),
            "Давление во вводе 500кВ" to listOf("2 АТГ А давл500"), "Давление во вводе 220кВ" to listOf("2 АТГ А давл220"),
            "Температура масла ТС1" to listOf("2 АТГ А ТС1"), "Температура масла ТС2" to listOf("2 АТГ А ТС2"),
            "1гр. охл. давление" to listOf("2 АТГ А насос1"), "2гр. охл. давление" to listOf("2 АТГ А насос2"),
            "3гр. охл. давление" to listOf("2 АТГ А насос3"), "4гр. охл. давление" to listOf("2 АТГ А насос4")
        ),
        "АТГ + Реактор|АТГ резерв" to listOf(
            "Уровень масла БАК" to listOf("АТГ рез бак"), "Уровень масла РПН" to listOf("АТГ рез РПН"),
            "Давление во вводе 500кВ" to listOf("АТГ рез давл500"), "Давление во вводе 220кВ" to listOf("АТГ рез давл220"),
            "Температура масла ТС1" to listOf("АТГ рез ТС1"), "Температура масла ТС2" to listOf("АТГ рез ТС2"),
            "1гр. охл. давление" to listOf("АТГ рез насос1"), "2гр. охл. давление" to listOf("АТГ рез насос2"),
            "3гр. охл. давление" to listOf("АТГ рез насос3"), "4гр. охл. давление" to listOf("АТГ рез насос4")
        ),
        "АТГ + Реактор|3АТГ ф.С" to listOf(
            "Уровень масла БАК" to listOf("3 АТГ С бак"), "Уровень масла РПН" to listOf("3 АТГ С РПН"),
            "Давление во вводе 500кВ" to listOf("3 АТГ С давл500"), "Давление во вводе 220кВ" to listOf("3 АТГ С давл220"),
            "Температура масла ТС1" to listOf("3 АТГ С ТС1"), "Температура масла ТС2" to listOf("3 АТГ С ТС2"),
            "1гр. охл. давление" to listOf("3 АТГ С насос1"), "2гр. охл. давление" to listOf("3 АТГ С насос2"),
            "3гр. охл. давление" to listOf("3 АТГ С насос3"), "4гр. охл. давление" to listOf("3 АТГ С насос4")
        ),
        "АТГ + Реактор|3АТГ ф.В" to listOf(
            "Уровень масла БАК" to listOf("3 АТГ В бак"), "Уровень масла РПН" to listOf("3 АТГ В РПН"),
            "Давление во вводе 500кВ" to listOf("3 АТГ В давл500"), "Давление во вводе 220кВ" to listOf("3 АТГ В давл220"),
            "Температура масла ТС1" to listOf("3 АТГ В ТС1"), "Температура масла ТС2" to listOf("3 АТГ В ТС2"),
            "1гр. охл. давление" to listOf("3 АТГ В насос1"), "2гр. охл. давление" to listOf("3 АТГ В насос2"),
            "3гр. охл. давление" to listOf("3 АТГ В насос3"), "4гр. охл. давление" to listOf("3 АТГ В насос4")
        ),
        "АТГ + Реактор|3АТГ ф.А" to listOf(
            "Уровень масла БАК" to listOf("3 АТГ А бак"), "Уровень масла РПН" to listOf("3 АТГ А РПН"),
            "Давление во вводе 500кВ" to listOf("3 АТГ А давл500"), "Давление во вводе 220кВ" to listOf("3 АТГ А давл220"),
            "Температура масла ТС1" to listOf("3 АТГ А ТС1"), "Температура масла ТС2" to listOf("3 АТГ А ТС2"),
            "1гр. охл. давление" to listOf("3 АТГ А насос1"), "2гр. охл. давление" to listOf("3 АТГ А насос2"),
            "3гр. охл. давление" to listOf("3 АТГ А насос3"), "4гр. охл. давление" to listOf("3 АТГ А насос4")
        ),
        "АТГ + Реактор|Р-500 2С ф.С" to listOf(
            "Уровень масла БАК" to listOf("Реактор С бак"), "Давление во вводе 500кВ" to listOf("Реактор С давл500"),
            "Температура масла ТС" to listOf("Реактор С ТС"),
            "1гр. охл. давление" to listOf("Реактор С насос1"), "2гр. охл. давление" to listOf("Реактор С насос2"),
            "3гр. охл. давление" to listOf("Реактор С насос3"), "ТТ нейтрали уровень масла" to listOf("Реактор С ТТ нейтр")
        ),
        "АТГ + Реактор|Р-500 2С ф.В" to listOf(
            "Уровень масла БАК" to listOf("Реактор В бак"), "Давление во вводе 500кВ" to listOf("Реактор В давл500"),
            "Температура масла ТС" to listOf("Реактор В ТС"),
            "1гр. охл. давление" to listOf("Реактор В насос1"), "2гр. охл. давление" to listOf("Реактор В насос2"),
            "3гр. охл. давление" to listOf("Реактор В насос3"), "ТТ нейтрали уровень масла" to listOf("Реактор В ТТ нейтр")
        ),
        "АТГ + Реактор|Р-500 2С ф.А" to listOf(
            "Уровень масла БАК" to listOf("Реактор А бак"), "Давление во вводе 500кВ" to listOf("Реактор А давл500"),
            "Температура масла ТС" to listOf("Реактор А ТС"),
            "1гр. охл. давление" to listOf("Реактор А насос1"), "2гр. охл. давление" to listOf("Реактор А насос2"),
            "3гр. охл. давление" to listOf("Реактор А насос3"), "ТТ нейтрали уровень масла" to listOf("Реактор А ТТ нейтр")
        ),

        // ==================== Здания (без изменений) ====================
        "Здания|Компрессорная №1" to listOf("Состояние запорной арматуры" to listOf("Компр1 арматура"), "Работоспособность обогрева" to listOf("Компр1 обогрев"), "Температура воздуха" to listOf("Компр1 темп")),
        "Здания|Баллонная №1" to listOf("Состояние запорной арматуры" to listOf("Баллон1 арматура"), "Работоспособность обогрева" to listOf("Баллон1 обогрев"), "Температура воздуха" to listOf("Баллон1 темп")),
        "Здания|Компрессорная №2" to listOf("Состояние запорной арматуры" to listOf("Компр2 арматура"), "Работоспособность обогрева" to listOf("Компр2 обогрев"), "Температура воздуха" to listOf("Компр2 темп")),
        "Здания|Баллонная №2" to listOf("Состояние запорной арматуры" to listOf("Баллон2 арматура"), "Работоспособность обогрева" to listOf("Баллон2 обогрев"), "Температура воздуха" to listOf("Баллон2 темп")),
        "Здания|КПЗ ОПУ" to listOf("Состояние запорной арматуры" to listOf("КПЗ ОПУ арматура"), "Работоспособность обогрева" to listOf("КПЗ ОПУ обогрев"), "Температура воздуха" to listOf("КПЗ ОПУ темп")),
        "Здания|КПЗ-2" to listOf("Состояние запорной арматуры" to listOf("КПЗ-2 арматура"), "Работоспособность обогрева" to listOf("КПЗ-2 обогрев"), "Температура воздуха" to listOf("КПЗ-2 темп")),
        "Здания|Насосная пожаротушения" to listOf("Состояние запорной арматуры" to listOf("НПТ арматура"), "Работоспособность обогрева" to listOf("НПТ обогрев"), "Температура воздуха" to listOf("НПТ темп"), "Уровень воды" to listOf("НПТ уровень воды")),
        "Здания|Мастерская по ремонту ВВ" to listOf("Работоспособность обогрева" to listOf("Мастерская обогрев"), "Температура воздуха" to listOf("Мастерская темп")),
        "Здания|Артскважина" to listOf("Работоспособность обогрева" to listOf("Артскважина обогрев")),
        "Здания|Здание артезианской скважины" to listOf("Работоспособность обогрева" to listOf("Артез скважина обогрев")),
        "Здания|Помещение АБ №1,2" to listOf("Работоспособность обогрева" to listOf("Помещ АБ обогрев"), "Температура воздуха" to listOf("Помещ АБ темп")),
        "Здания|Помещение п/этажа №1,2,3" to listOf("Работоспособность обогрева" to listOf("Подвал обогрев"), "Температура воздуха" to listOf("Подвал темп"))
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_graphs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerSection = view.findViewById(R.id.spinnerSection)
        spinnerEquipment = view.findViewById(R.id.spinnerEquipment)
        spinnerParameter = view.findViewById(R.id.spinnerParameter)
        plot = view.findViewById(R.id.plot)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        val btnShowGraph = view.findViewById<Button>(R.id.btnShowGraph)

        // Адаптер для секций
        spinnerSection.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sectionList)

        // При выборе секции — обновляем список оборудования
        spinnerSection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateEquipmentList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // При выборе оборудования — обновляем список параметров
        spinnerEquipment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateParameterList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 🔧 Параметр — без автообновления, просто выбираем

        // 🔧 Кнопка "Показать график"
        btnShowGraph.setOnClickListener {
            loadGraph()
        }

        updateEquipmentList()
    }

    private fun updateEquipmentList() {
        val section = spinnerSection.selectedItem?.toString() ?: return
        val equipment = equipmentNamesMap[section] ?: listOf()
        spinnerEquipment.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, equipment)
    }

    private fun updateParameterList() {
        val section = spinnerSection.selectedItem?.toString() ?: return
        val equipment = spinnerEquipment.selectedItem?.toString() ?: return
        val key = "$section|$equipment"
        val params = paramsMap[key]?.map { it.first } ?: listOf()
        spinnerParameter.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, params)
    }

    private fun loadGraph() {
        val section = spinnerSection.selectedItem?.toString() ?: return
        val equipment = spinnerEquipment.selectedItem?.toString() ?: return
        val key = "$section|$equipment"
        val params = paramsMap[key] ?: return
        val paramIndex = spinnerParameter.selectedItemPosition
        if (paramIndex < 0 || paramIndex >= params.size) return

        val selectedParam = params[paramIndex]
        val keys = selectedParam.second
        val paramName = selectedParam.first

        lifecycleScope.launch {
            try {
                val sheetsService = GoogleSheetsService(requireContext())
                val allData = withContext(Dispatchers.IO) { sheetsService.getAllInspections() }

                if (allData.isNullOrEmpty()) {
                    showEmpty(true)
                    return@launch
                }

                plot.clear()
                showEmpty(false)
                tvTitle.text = "📊 $equipment — $paramName"

                plot.setDomainLabel("Дата осмотра")
                plot.setRangeLabel(paramName)

                // 🔧 Используем дату+время — каждая точка уникальна
                val dateTimeFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                val sortedEntries = mutableListOf<Pair<Long, Float>>()

                val key = keys.firstOrNull() ?: return@launch

                allData.forEach { row ->
                    val dateStr = row["Дата"] ?: ""
                    val timeStr = row["Время"] ?: "00:00"
                    val dateTimeStr = "$dateStr $timeStr"
                    val date = try { dateTimeFormat.parse(dateTimeStr) } catch (e: Exception) { null }
                    val raw = row[key]?.replace(",", ".")?.replace(">", "")?.replace("<", "")?.replace("+", "1")?.trim()
                    val value = raw?.toFloatOrNull()

                    if (value != null && date != null) {
                        sortedEntries.add(date.time to value)
                    }
                }

                // Сортируем по дате
                sortedEntries.sortBy { it.first }

                if (sortedEntries.isEmpty()) {
                    showEmpty(true)
                    return@launch
                }

                Log.d("GRAPHS_DEBUG", "Параметр: $paramName, ключ: $key, точек: ${sortedEntries.size}")
                sortedEntries.forEach { entry ->
                    Log.d("GRAPHS_DEBUG", "  ${java.util.Date(entry.first)} -> ${entry.second}")
                }

                // Создаём серию
                val values = sortedEntries.map { it.second }
                val series = SimpleXYSeries(values, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, paramName)

                val lineColor = "#1565C0".toColorInt()
                val pointColor = "#FF5722".toColorInt()

                val formatter = LineAndPointFormatter(lineColor, pointColor, Color.TRANSPARENT, null).apply {
                    linePaint.strokeWidth = 4f
                    vertexPaint.strokeWidth = 10f
                }

                plot.addSeries(series, formatter)

                // Настройка диапазона Y
                val minVal = sortedEntries.minOf { it.second }
                val maxVal = sortedEntries.maxOf { it.second }
                val padding = ((maxVal - minVal) * 0.2f).coerceAtLeast(1f)
                val rangeMin = (minVal - padding).coerceAtLeast(0f)
                val rangeMax = maxVal + padding

                if (rangeMax > rangeMin) {
                    plot.setRangeBoundaries(rangeMin.toDouble(), rangeMax.toDouble(), com.androidplot.xy.BoundaryMode.FIXED)
                }

                // Подписи осей
                plot.graph.getLineLabelStyle(com.androidplot.xy.XYGraphWidget.Edge.BOTTOM).apply {
                    paint.textSize = 20f
                    paint.color = "#757575".toColorInt()
                }
                plot.graph.getLineLabelStyle(com.androidplot.xy.XYGraphWidget.Edge.LEFT).apply {
                    paint.textSize = 20f
                    paint.color = "#757575".toColorInt()
                }

                plot.redraw()
            } catch (e: Exception) {
                Log.e("GRAPHS_DEBUG", "Ошибка загрузки графика", e)
                e.printStackTrace()
            }
        }
    }

    private fun showEmpty(show: Boolean) {
        tvEmpty.visibility = if (show) View.VISIBLE else View.GONE
        plot.visibility = if (show) View.GONE else View.VISIBLE
    }
}