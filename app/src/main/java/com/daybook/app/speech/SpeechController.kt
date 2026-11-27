package com.daybook.app.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Keeps listening through pauses until stop() is called.
 * Each spoken phrase becomes one line of the transcript.
 */
class SpeechController(
    private val context: Context,
    private val onTranscript: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var active = false
    private val lines = mutableListOf<String>()
    private var partial = ""

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun start() {
        if (active) return
        lines.clear()
        partial = ""
        active = true
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(listener)
        }
        listen()
    }

    fun stop() {
        commitPartial()
        active = false
        handler.removeCallbacksAndMessages(null)
        recognizer?.destroy()
        recognizer = null
    }

    fun transcript(): String =
        (lines + partial).filter { it.isNotBlank() }.joinToString("\n")

    private fun commitPartial() {
        if (partial.isNotBlank()) lines += partial.trim()
        partial = ""
    }

    private fun listen() {
        if (!active) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        recognizer?.startListening(intent)
    }

    private fun restart(delayMs: Long) {
        handler.postDelayed({ listen() }, delayMs)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onPartialResults(partialResults: Bundle?) {
            if (!active) return
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            partial = text
            onTranscript(transcript())
        }

        override fun onResults(results: Bundle?) {
            if (!active) return
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            partial = ""
            if (text.isNotBlank()) lines += text.trim()
            onTranscript(transcript())
            restart(200)
        }

        override fun onError(error: Int) {
            if (!active) return
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    commitPartial()
                    onTranscript(transcript())
                    restart(200)
                }
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> restart(600)
                else -> {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                            "Microphone permission is needed."
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                        SpeechRecognizer.ERROR_SERVER ->
                            "The speech service needs an internet connection, or an offline " +
                                "language pack installed in your phone's Google settings."
                        SpeechRecognizer.ERROR_AUDIO -> "Couldn't use the microphone."
                        else -> "Speech recognition stopped (code $error)."
                    }
                    stop()
                    onError(message)
                }
            }
        }
    }
}
