package com.example.authenticateuserandpass.utils

import android.app.DatePickerDialog
import android.widget.EditText
import java.util.Calendar

open class Utils {
    fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            editText.context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "%02d/%02d/%04d".format(selectedDay, selectedMonth + 1, selectedYear)
                editText.setText(selectedDate)
            },
            year, month, day
        )
        // ✅ Không cho chọn ngày trong quá khứ
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }


}