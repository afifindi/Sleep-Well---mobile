package com.example.sleepwell

import android.content.Context
import ai.onnxruntime.*
import java.nio.FloatBuffer

class ONNXModelHelper(private val context: Context) {

    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    init {
        try {
            // Initialize ONNX Runtime
            ortEnvironment = OrtEnvironment.getEnvironment()

            // Load model from assets
            val modelBytes = context.assets.open("sleep_model.onnx").readBytes()
            ortSession = ortEnvironment?.createSession(modelBytes)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun predict(
        usia: Int,
        jenisKelamin: String,
        durasiTidur: Float,
        aktivitasFisik: Int,
        tinggiBadan: Int,
        beratBadan: Int,
        tingkatStres: Int
    ): String {
        try {
            // Convert jenis kelamin to numeric (0 = Female, 1 = Male)
            val gender = if (jenisKelamin == "Male") 1f else 0f

            // Prepare input data sesuai dengan urutan features model Anda
            // SESUAIKAN URUTAN INI DENGAN MODEL ANDA!
            val inputData = floatArrayOf(
                usia.toFloat(),
                gender,
                durasiTidur,
                aktivitasFisik.toFloat(),
                tingkatStres.toFloat(),
                tinggiBadan.toFloat(),
                beratBadan.toFloat()
            )

            // Create input tensor
            val inputShape = longArrayOf(1, inputData.size.toLong())
            val inputBuffer = FloatBuffer.wrap(inputData)
            val inputTensor = OnnxTensor.createTensor(ortEnvironment, inputBuffer, inputShape)

            // Run inference
            val inputName = ortSession?.inputNames?.iterator()?.next()
            val results = ortSession?.run(mapOf(inputName to inputTensor))

            // Get output
            val output = results?.get(0)?.value as Array<*>
            val prediction = (output[0] as FloatArray)[0]

            // Clean up
            inputTensor.close()
            results?.close()

            // Interpret hasil prediksi
            // SESUAIKAN DENGAN OUTPUT MODEL ANDA!
            return when {
                prediction < 0.33 -> "Kualitas Tidur: BURUK"
                prediction < 0.66 -> "Kualitas Tidur: CUKUP"
                else -> "Kualitas Tidur: BAIK"
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return "Error: ${e.message}"
        }
    }

    fun close() {
        ortSession?.close()
        ortEnvironment?.close()
    }
}