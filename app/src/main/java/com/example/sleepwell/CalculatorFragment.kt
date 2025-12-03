package com.example.sleepwell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class CalculatorFragment : Fragment() {

    private lateinit var etUsia: EditText
    private lateinit var etDurasiTidur: EditText
    private lateinit var etAktivitasFisik: EditText
    private lateinit var etTinggiBadan: EditText
    private lateinit var etBeratBadan: EditText
    private lateinit var spinnerJenisKelamin: Spinner
    private lateinit var seekBarStres: SeekBar
    private lateinit var tvStresValue: TextView
    private lateinit var btnSubmit: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calculator, container, false)

        // Initialize views
        etUsia = view.findViewById(R.id.etUsia)
        etDurasiTidur = view.findViewById(R.id.etDurasiTidur)
        etAktivitasFisik = view.findViewById(R.id.etAktivitasFisik)
        etTinggiBadan = view.findViewById(R.id.etTinggiBadan)
        etBeratBadan = view.findViewById(R.id.etBeratBadan)
        spinnerJenisKelamin = view.findViewById(R.id.spinnerJenisKelamin)
        seekBarStres = view.findViewById(R.id.seekBarStres)
        tvStresValue = view.findViewById(R.id.tvStresValue)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        // Setup Spinner
        val jenisKelaminAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            arrayOf("Pilih Jenis Kelamin", "Male", "Female")
        )
        jenisKelaminAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerJenisKelamin.adapter = jenisKelaminAdapter

        // Setup SeekBar
        seekBarStres.max = 9 // 0-9 untuk represent 1-10
        seekBarStres.progress = 4 // Default value 5
        tvStresValue.text = "5"

        seekBarStres.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvStresValue.text = (progress + 1).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Submit button
        btnSubmit.setOnClickListener {
            if (validateForm()) {
                val formData = HashMap<String, Any>()
                formData["usia"] = etUsia.text.toString().toInt()
                formData["jenisKelamin"] = spinnerJenisKelamin.selectedItem.toString()
                formData["durasiTidur"] = etDurasiTidur.text.toString().toFloat()
                formData["aktivitasFisik"] = etAktivitasFisik.text.toString().toInt()
                formData["tinggiBadan"] = etTinggiBadan.text.toString().toInt()
                formData["beratBadan"] = etBeratBadan.text.toString().toInt()
                formData["tingkatStres"] = seekBarStres.progress + 1

                (activity as MainActivity).showResultFragment(formData)
            }
        }

        return view
    }

    private fun validateForm(): Boolean {
        if (etUsia.text.isEmpty()) {
            Toast.makeText(context, "Masukkan usia", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etDurasiTidur.text.isEmpty()) {
            Toast.makeText(context, "Masukkan durasi tidur", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etAktivitasFisik.text.isEmpty()) {
            Toast.makeText(context, "Masukkan aktivitas fisik", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etTinggiBadan.text.isEmpty()) {
            Toast.makeText(context, "Masukkan tinggi badan", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etBeratBadan.text.isEmpty()) {
            Toast.makeText(context, "Masukkan berat badan", Toast.LENGTH_SHORT).show()
            return false
        }
        if (spinnerJenisKelamin.selectedItemPosition == 0) {
            Toast.makeText(context, "Pilih jenis kelamin", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
