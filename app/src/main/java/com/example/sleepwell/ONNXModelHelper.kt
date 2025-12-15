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
    private val ortSession by lazy {
        val modelBytes = context.assets.open("sleep_model.onnx").readBytes()
        ortEnvironment.createSession(modelBytes)
    }

    // Mapping Output Model (Index 0-5) ke Skor Asli (4-9)
    // Sesuai dengan LabelEncoder di Python: {0: 4, 1: 5, 2: 6, 3: 7, 4: 8, 5: 9}
    private val SCORES_MAPPING = intArrayOf(4, 5, 6, 7, 8, 9)

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
        val bmi = if (tinggiMeter > 0) beratBadan / (tinggiMeter * tinggiMeter) else 0f

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
            usia,             // 1. Age (Fitur pertama)
            durasiTidur,      // 2. Sleep Duration
            aktivitasFisik,   // 3. Physical Activity Level
            tingkatStres,     // 4. Stress Level
            isMale,           // 5. Gender Male
            isObese,          // 6. BMI Obese
            isOverweight      // 7. BMI Overweight
        )

        // --- 2. INFERENCE (Menjalankan Model) ---

        try {
            // Buat Tensor input [1 baris, 7 kolom]
            val inputName = ortSession.inputNames.iterator().next()
            val shape = longArrayOf(1, 7)
            val floatBuffer = FloatBuffer.wrap(inputData)
            val inputTensor = OnnxTensor.createTensor(ortEnvironment, floatBuffer, shape)

            // Jalankan Prediksi
            val results = ortSession.run(Collections.singletonMap(inputName, inputTensor))

            // Ambil hasil (Output berupa Index Kelas, misal: 5)
            val outputTensor = results[0] as OnnxTensor
            val predictionIndex = (outputTensor.value as LongArray)[0] // Mengambil hasil pertama

            results.close() // Bersihkan memori

            // --- 3. MAPPING HASIL (Index -> Skor Asli -> Kata-kata) ---
            return mapIndexToString(predictionIndex)

        } catch (e: Exception) {
            e.printStackTrace()
            return "Error: ${e.message}"
        }
    }

    private fun mapIndexToString(index: Long): String {
        val idx = index.toInt()

        // Pastikan index tidak keluar dari array mapping
        if (idx < 0 || idx >= SCORES_MAPPING.size) {
            return "Tidak Diketahui (Index: $idx)"
        }

        // Ambil skor asli dari mapping
        val realScore = SCORES_MAPPING[idx]

        // Berikan label berdasarkan skor asli (Skala 1-10)
        return when (realScore) {
            in 0..5 -> "Kurang (Skor: $realScore/10)"  // Skor 4, 5
            6 -> "Cukup (Skor: 6/10)"
            7 -> "Baik (Skor: 7/10)"
            8 -> "Sangat Baik (Skor: 8/10)"
            9 -> "Sempurna (Skor: 9/10)"
            else -> "Skor: $realScore/10"
        }
    }
}