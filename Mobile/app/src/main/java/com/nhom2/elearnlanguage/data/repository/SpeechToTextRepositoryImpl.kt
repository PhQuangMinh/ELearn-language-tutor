package com.nhom2.elearnlanguage.data.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.nhom2.elearnlanguage.domain.repository.SpeechToTextRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class SpeechToTextRepositoryImpl (
    private val context: Context
): SpeechToTextRepository {

    private val _results = MutableSharedFlow<String>()
    override fun getResults(): Flow<String> = _results

    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

    override fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

        speechRecognizer.setRecognitionListener(object: RecognitionListener{
            override fun onBeginningOfSpeech() {
            }

            override fun onBufferReceived(p0: ByteArray?) {
            }

            override fun onEndOfSpeech() {
            }

            override fun onError(p0: Int) {
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
            }

            override fun onPartialResults(p0: Bundle?) {
                val text = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.getOrNull(0)
                    .orEmpty()

                if (text.isNotBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        _results.emit(text)
                    }
                }
            }

            override fun onReadyForSpeech(p0: Bundle?) {
            }

            override fun onResults(p0: Bundle?) {
                val text = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.get(0) ?: ""

                CoroutineScope(Dispatchers.IO).launch {
                    _results.emit(text)
                }
            }

            override fun onRmsChanged(p0: Float) {
            }

        })

        speechRecognizer.startListening(intent)
    }

    override fun stopListening() {
        speechRecognizer.stopListening()
    }
}