package com.example.sleepwell

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider

class CalculatorFragment : Fragment(R.layout.fragment_calculator) {

    private lateinit var onnxHelper: ONNXModelHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Helper
        try {
            onnxHelper = ONNXModelHelper(requireContext())
        } catch (e: Exception) {
            Toast.makeText(context, "Error memuat model: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Setup Spinner Gender
        val spinnerGender = view.findViewById<Spinner>(R.id.spinnerGender)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, arrayOf("Male", "Female"))
        spinnerGender.adapter = adapter

        // Setup Tombol Prediksi
        view.findViewById<Button>(R.id.btnPrediksi).setOnClickListener {
            lakukanPrediksi(view)
        }
    }

    private fun lakukanPrediksi(view: View) {
        // 1. Ambil Input dari UI
        val etUsia = view.findViewById<EditText>(R.id.etUsia)
        val etTinggi = view.findViewById<EditText>(R.id.etTinggi)
        val etBerat = view.findViewById<EditText>(R.id.etBerat)
        val etTidur = view.findViewById<EditText>(R.id.etTidur)
        val etFisik = view.findViewById<EditText>(R.id.etFisik)
        val sliderStres = view.findViewById<Slider>(R.id.sliderStres)
        val spinnerGender = view.findViewById<Spinner>(R.id.spinnerGender)

        // 2. Validasi (Cek jika kosong)
        if (etUsia.text.isEmpty() || etTinggi.text.isEmpty() || etBerat.text.isEmpty() ||
            etTidur.text.isEmpty() || etFisik.text.isEmpty()) {
            Toast.makeText(context, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        if (!::onnxHelper.isInitialized) {
            Toast.makeText(context, "Model belum siap. Coba restart aplikasi.", Toast.LENGTH_LONG).show()
            return
        }

        try {
            // 3. Konversi Data
            val usia = etUsia.text.toString().toFloat()
            val tinggi = etTinggi.text.toString().toFloat()
            val berat = etBerat.text.toString().toFloat()
            val tidur = etTidur.text.toString().toFloat()
            val fisik = etFisik.text.toString().toFloat()
            val stres = sliderStres.value
            val gender = spinnerGender.selectedItem.toString()

            // 4. Panggil ONNX Helper
            val hasilPrediksi = onnxHelper.predict(
                usia = usia,
                durasiTidur = tidur,
                aktivitasFisik = fisik,
                tingkatStres = stres,
                gender = gender,
                tinggiBadan = tinggi,
                beratBadan = berat
            )

            // 5. Kirim Data ke ResultFragment via MainActivity
            val data = hashMapOf<String, Any>(
                "hasil" to hasilPrediksi,
                "nama" to "User" // Bisa ditambahkan nama jika ada inputnya
            )

            (activity as? MainActivity)?.showResultFragment(data)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal memproses: ${e.message}", Toast.LENGTH_LONG).show()
            print("Gagal memproses: ${e.message}")
        }
    }
}