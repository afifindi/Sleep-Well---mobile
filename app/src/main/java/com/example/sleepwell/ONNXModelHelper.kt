package com.example.sleepwell

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import java.nio.FloatBuffer
import java.util.Collections

class ONNXModelHelper(context: Context) {

    // Inisialisasi Environment ONNX
    private val ortEnvironment = OrtEnvironment.getEnvironment()

    // Membaca model dari folder assets saat kelas ini dibuat
    private val ortSession = ortEnvironment.createSession(
        context.assets.open("sleep_model.onnx").readBytes()
    )

    fun predict(
        usia: Float,
        durasiTidur: Float,
        aktivitasFisik: Float,
        tingkatStres: Float,
        gender: String, // "Male" atau "Female"
        tinggiBadan: Float,
        beratBadan: Float
    ): String {

        // --- 1. PRE-PROCESSING (Menyiapkan Data) ---

        // A. Encode Gender (Male=1.0, Female=0.0)
        val isMale = if (gender.equals("Male", ignoreCase = true)) 1.0f else 0.0f

        // B. Hitung BMI & Encode Kategorinya
        // Rumus BMI = Berat(kg) / (Tinggi(m) * Tinggi(m))
        val tinggiMeter = tinggiBadan / 100f
        val bmi = beratBadan / (tinggiMeter * tinggiMeter)

        var isObese = 0.0f
        var isOverweight = 0.0f

        // Logika kategori BMI
        if (bmi >= 30) {
            isObese = 1.0f
        } else if (bmi >= 25) {
            isOverweight = 1.0f
        }
        // Jika < 25 (Normal), keduanya tetap 0.0

        // C. Susun Array Input (URUTAN HARUS SAMA PERSIS DENGAN PYTHON!)
        // Python Order: ['Age', 'Sleep Duration', 'Physical Activity Level', 'Stress Level', 'Gender_Male', 'BMI Category_Obese', 'BMI Category_Overweight']
        val inputData = floatArrayOf(
            usia,
            durasiTidur,
            aktivitasFisik,
            tingkatStres,
            isMale,
            isObese,
            isOverweight
        )

        // --- 2. INFERENCE (Menjalankan Model) ---

        // Buat Tensor input [1 baris, 7 kolom]
        val inputName = ortSession.inputNames.iterator().next()
        val shape = longArrayOf(1, 7)
        val floatBuffer = FloatBuffer.wrap(inputData)
        val inputTensor = OnnxTensor.createTensor(ortEnvironment, floatBuffer, shape)

        // Jalankan Prediksi
        val results = ortSession.run(Collections.singletonMap(inputName, inputTensor))

        // Ambil hasil (Output Random Forest biasanya berupa Label Int64)
        val outputTensor = results[0] as OnnxTensor
        val predictionLabel = (outputTensor.value as LongArray)[0] // Mengambil hasil pertama

        results.close() // Bersihkan memori

        // --- 3. MAPPING HASIL (Angka -> Kata-kata) ---
        return mapLabelToString(predictionLabel)
    }

    private fun mapLabelToString(label: Long): String {
        // Label ini berdasarkan data 'Quality of Sleep' (Skala 1-10 di dataset asli)
        return when (label.toInt()) {
            in 1..4 -> "Buruk (Skor: $label)"
            5 -> "Kurang (Skor: 5)"
            6 -> "Cukup (Skor: 6)"
            7 -> "Baik (Skor: 7)"
            8 -> "Sangat Baik (Skor: 8)"
            in 9..10 -> "Sempurna (Skor: $label)"
            else -> "Tidak Diketahui ($label)"
        }
    }
}