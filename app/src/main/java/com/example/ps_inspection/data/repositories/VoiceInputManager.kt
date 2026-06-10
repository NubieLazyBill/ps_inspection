package com.example.ps_inspection.data.utils

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class VoiceInputManager(private val fragment: Fragment) {

    private var onResultCallback: ((String) -> Unit)? = null

    val voiceContract = fragment.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data: Intent? = result.data
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                onResultCallback?.invoke(spokenText)
            } else {
                onResultCallback?.invoke("")
            }
        } else {
            onResultCallback?.invoke("")
        }
    }

    fun startVoiceRecognition(onResult: (String) -> Unit) {
        onResultCallback = onResult
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите показания для ОРУ-35")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

            // 🔧 Увеличиваем время ожидания (работает на большинстве устройств)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L)

            // 🔧 Дополнительный параметр для некоторых устройств
            putExtra("android.speech.extra.GET_AUDIO_FORMAT", 2)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }
        voiceContract.launch(intent)
    }
}