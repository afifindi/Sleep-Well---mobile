package com.example.sleepwell

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class ResultFragment : Fragment(R.layout.fragment_result) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data yang dikirim dari MainActivity
        val arguments = arguments
        val hasil = arguments?.getSerializable("formData") as? HashMap<String, Any>

        val tvHasil = view.findViewById<TextView>(R.id.tvHasil)

        if (hasil != null) {
            val skorTidur = hasil["hasil"] as String
            tvHasil.text = skorTidur
        }
    }
}