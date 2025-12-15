package com.example.sleepwell

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import java.nio.FloatBuffer
import java.util.Collections

class ONNXModelHelper(context: Context) {

    private val ortEnvironment = OrtEnvironment.getEnvironment()

    // 1. UPDATE NAMA FILE MODEL
    // Pastikan file 'sleep_quality_3class.onnx' sudah ada di folder assets
    private val ortSession by lazy {
        // Gunakan try-catch agar jika file lupa dimasukkan, aplikasi tidak langsung crash fatal
        try {
            val modelBytes = context.assets.open("sleep_quality_3class.onnx").readBytes()
            ortEnvironment.createSession(modelBytes)
        } catch (e: Exception) {
            throw RuntimeException("File 'sleep_quality_3class.onnx' tidak ditemukan di folder assets!", e)
        }
    }

    // HAPUS variabel SCORES_MAPPING karena kita tidak pakai skor 4-9 lagi!

    fun predict(
        usia: Float,
        durasiTidur: Float,
        aktivitasFisik: Float,
        tingkatStres: Float,
        gender: String,
        tinggiBadan: Float,
        beratBadan: Float
    ): String {

        // --- PRE-PROCESSING (Sama seperti sebelumnya) ---
        val isMale = if (gender.equals("Male", ignoreCase = true)) 1.0f else 0.0f

        val tinggiMeter = tinggiBadan / 100f
        val bmi = if (tinggiMeter > 0) beratBadan / (tinggiMeter * tinggiMeter) else 0f

        var isObese = 0.0f
        var isOverweight = 0.0f

        if (bmi >= 30) {
            isObese = 1.0f
        } else if (bmi >= 25) {
            isOverweight = 1.0f
        }

        // Susun Input (7 Fitur)
        val inputData = floatArrayOf(
            usia,
            durasiTidur,
            aktivitasFisik,
            tingkatStres,
            isMale,
            isObese,
            isOverweight
        )

        // --- INFERENCE ---
        return try {
            val inputName = ortSession.inputNames.iterator().next()
            val shape = longArrayOf(1, 7)
            val floatBuffer = FloatBuffer.wrap(inputData)
            val inputTensor = OnnxTensor.createTensor(ortEnvironment, floatBuffer, shape)

            val results = ortSession.run(Collections.singletonMap(inputName, inputTensor))

            // PERUBAHAN DI SINI: Baca sebagai String Array, bukan LongArray
            val outputTensor = results[0] as OnnxTensor
            val predictionLabel = (outputTensor.value as Array<String>)[0] // Ambil teks langsung

            results.close()

            // Langsung kembalikan hasil teksnya (ditambah pemanis)
            formatHasil(predictionLabel)

        } catch (e: Exception) {
            e.printStackTrace()
            "Error AI: ${e.message}"
        }
    }

    // Fungsi Formatting Sederhana
    private fun formatHasil(label: String): String {
        return when (label) {
            "Baik" -> "Kualitas Tidur: BAIK 🟢\nPertahankan pola hidup sehat Anda!"
            "Buruk" -> "Kualitas Tidur: BURUK 🔴\nSebaiknya kurangi stres dan atur jam tidur."
            "Sedang" -> "Kualitas Tidur: SEDANG 🟡\nCukup oke, tapi masih bisa ditingkatkan."
            else -> "Kualitas Tidur: $label" // Jika outputnya teks lain
        }
    }
}