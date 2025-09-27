package com.example.ps_inspection

// InspectionATGData.kt
data class InspectionATGData(
    // 2 АТГ ф.С
    var atg2_c_oil_tank: String = "",
    var atg2_c_oil_rpn: String = "",
    var atg2_c_pressure_500: String = "",
    var atg2_c_pressure_220: String = "",
    var atg2_c_temp_ts1: String = "",
    var atg2_c_temp_ts2: String = "",
    var atg2_c_pump_group1: String = "",
    var atg2_c_pump_group2: String = "",
    var atg2_c_pump_group3: String = "",
    var atg2_c_pump_group4: String = "",

    // 2 АТГ ф.В
    var atg2_b_oil_tank: String = "",
    var atg2_b_oil_rpn: String = "",
    var atg2_b_pressure_500: String = "",
    var atg2_b_pressure_220: String = "",
    var atg2_b_temp_ts1: String = "",
    var atg2_b_temp_ts2: String = "",
    var atg2_b_pump_group1: String = "",
    var atg2_b_pump_group2: String = "",
    var atg2_b_pump_group3: String = "",
    var atg2_b_pump_group4: String = "",

    // 2 АТГ ф.А
    var atg2_a_oil_tank: String = "",
    var atg2_a_oil_rpn: String = "",
    var atg2_a_pressure_500: String = "",
    var atg2_a_pressure_220: String = "",
    var atg2_a_temp_ts1: String = "",
    var atg2_a_temp_ts2: String = "",
    var atg2_a_pump_group1: String = "",
    var atg2_a_pump_group2: String = "",
    var atg2_a_pump_group3: String = "",
    var atg2_a_pump_group4: String = "",

    // АТГ резервная фаза
    var atg_reserve_oil_tank: String = "",
    var atg_reserve_oil_rpn: String = "",
    var atg_reserve_pressure_500: String = "",
    var atg_reserve_pressure_220: String = "",
    var atg_reserve_temp_ts1: String = "",
    var atg_reserve_temp_ts2: String = "",
    var atg_reserve_pump_group1: String = "",
    var atg_reserve_pump_group2: String = "",
    var atg_reserve_pump_group3: String = "",
    var atg_reserve_pump_group4: String = "",

    // 3 АТГ ф.С
    var atg3_c_oil_tank: String = "",
    var atg3_c_oil_rpn: String = "",
    var atg3_c_pressure_500: String = "",
    var atg3_c_pressure_220: String = "",
    var atg3_c_temp_ts1: String = "",
    var atg3_c_temp_ts2: String = "",
    var atg3_c_pump_group1: String = "",
    var atg3_c_pump_group2: String = "",
    var atg3_c_pump_group3: String = "",
    var atg3_c_pump_group4: String = "",

    // 3 АТГ ф.В
    var atg3_b_oil_tank: String = "",
    var atg3_b_oil_rpn: String = "",
    var atg3_b_pressure_500: String = "",
    var atg3_b_pressure_220: String = "",
    var atg3_b_temp_ts1: String = "",
    var atg3_b_temp_ts2: String = "",
    var atg3_b_pump_group1: String = "",
    var atg3_b_pump_group2: String = "",
    var atg3_b_pump_group3: String = "",
    var atg3_b_pump_group4: String = "",

    // 3 АТГ ф.А
    var atg3_a_oil_tank: String = "",
    var atg3_a_oil_rpn: String = "",
    var atg3_a_pressure_500: String = "",
    var atg3_a_pressure_220: String = "",
    var atg3_a_temp_ts1: String = "",
    var atg3_a_temp_ts2: String = "",
    var atg3_a_pump_group1: String = "",
    var atg3_a_pump_group2: String = "",
    var atg3_a_pump_group3: String = "",
    var atg3_a_pump_group4: String = "",

    // Реактор Р-500 2С ф.С
    var reactor_c_oil_tank: String = "",
    var reactor_c_pressure_500: String = "",
    var reactor_c_temp_ts: String = "",
    var reactor_c_pump_group1: String = "",
    var reactor_c_pump_group2: String = "",
    var reactor_c_pump_group3: String = "",
    var reactor_c_tt_neutral: String = "",

    // Реактор Р-500 2С ф.В
    var reactor_b_oil_tank: String = "",
    var reactor_b_pressure_500: String = "",
    var reactor_b_temp_ts: String = "",
    var reactor_b_pump_group1: String = "",
    var reactor_b_pump_group2: String = "",
    var reactor_b_pump_group3: String = "",
    var reactor_b_tt_neutral: String = "",

    // Реактор Р-500 2С ф.А
    var reactor_a_oil_tank: String = "",
    var reactor_a_pressure_500: String = "",
    var reactor_a_temp_ts: String = "",
    var reactor_a_pump_group1: String = "",
    var reactor_a_pump_group2: String = "",
    var reactor_a_pump_group3: String = "",
    var reactor_a_tt_neutral: String = ""
)