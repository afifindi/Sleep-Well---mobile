package com.example.sleepwell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ResultFragment : Fragment() {

    private lateinit var onnxHelper: ONNXModelHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        // Initialize ONNX Helper
        onnxHelper = ONNXModelHelper(requireContext())

        // Get form data
        @Suppress("UNCHECKED_CAST")
        val formData = arguments?.getSerializable("formData") as? HashMap<String, Any>

        if (formData != null) {
            // Run prediction
            val result = onnxHelper.predict(
                usia = formData["usia"] as Int,
                jenisKelamin = formData["jenisKelamin"] as String,
                durasiTidur = formData["durasiTidur"] as Float,
                aktivitasFisik = formData["aktivitasFisik"] as Int,
                tinggiBadan = formData["tinggiBadan"] as Int,
                beratBadan = formData["beratBadan"] as Int,
                tingkatStres = formData["tingkatStres"] as Int
            )

            tvResult.text = result
        } else {
            tvResult.text = "Data tidak tersedia"
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up ONNX resources
        if (::onnxHelper.isInitialized) {
            onnxHelper.close()
        }
    }
}