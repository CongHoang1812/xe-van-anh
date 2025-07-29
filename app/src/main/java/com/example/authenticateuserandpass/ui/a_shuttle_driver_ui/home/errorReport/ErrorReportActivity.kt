package com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.errorReport

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ErrorReportActivity : AppCompatActivity() {

    private lateinit var spinnerLoaiSuCo: Spinner
    private lateinit var edtMoTa: EditText
    private lateinit var btnGuiBaoCao: Button
    private lateinit var btnDong: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error_report)

        // Ánh xạ view
        spinnerLoaiSuCo = findViewById(R.id.spinnerLoaiSuCo)
        edtMoTa = findViewById(R.id.edtMoTa)
        btnGuiBaoCao = findViewById(R.id.btnGuiBaoCao)
        btnDong = findViewById(R.id.btnDong)

        // Danh sách loại sự cố
        val loaiSuCoList = listOf(
            "Chọn loại sự cố",
            "Xe đến trễ",
            "Không đón được khách",
            "Khách không liên lạc được",
            "Lỗi hệ thống",
            "Khác"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            loaiSuCoList
        )
        spinnerLoaiSuCo.adapter = adapter

        // Gửi báo cáo
        btnGuiBaoCao.setOnClickListener {
            val loaiSuCo = spinnerLoaiSuCo.selectedItem.toString()
            val moTa = edtMoTa.text.toString().trim()

            if (spinnerLoaiSuCo.selectedItemPosition == 0) {
                Toast.makeText(this, "Vui lòng chọn loại sự cố", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (moTa.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mô tả", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Gửi dữ liệu đến Firebase hoặc Server ở đây
            Log.d("BaoCaoSuCo", "Loại: $loaiSuCo\nMô tả: $moTa")
            Toast.makeText(this, "Đã gửi báo cáo", Toast.LENGTH_SHORT).show()
            finish() // Đóng activity sau khi gửi
        }

        // Đóng
        btnDong.setOnClickListener {
            finish()
        }
    }
}