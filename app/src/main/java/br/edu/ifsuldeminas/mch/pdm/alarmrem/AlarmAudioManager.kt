package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Locale

object AlarmAudioManager {

    private var tts: TextToSpeech? = null
    private var loopAtivo = false
    private var textoLoop = ""
    private val handler = Handler(Looper.getMainLooper())

    fun falar(context: Context, nome: String, descricao: String) {
        parar()

        textoLoop = "Atenção, hora de tomar: $nome. $descricao"
        loopAtivo = true

        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val resultadoIdioma = tts?.setLanguage(Locale("pt", "BR"))

                if (
                    resultadoIdioma != TextToSpeech.LANG_MISSING_DATA &&
                    resultadoIdioma != TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    iniciarLoop()
                }
            }
        }
    }

    private fun iniciarLoop() {
        handler.removeCallbacksAndMessages(null)

        val runnable = object : Runnable {
            override fun run() {
                if (!loopAtivo || tts == null) return

                falarAgora()

                if (loopAtivo) {
                    handler.postDelayed(this, 6000)
                }
            }
        }

        handler.post(runnable)
    }

    private fun falarAgora() {
        val ttsLocal = tts ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val params = Bundle().apply {
                putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }

            ttsLocal.stop()
            ttsLocal.speak(
                textoLoop,
                TextToSpeech.QUEUE_FLUSH,
                params,
                System.currentTimeMillis().toString()
            )
        } else {
            @Suppress("DEPRECATION")
            ttsLocal.stop()
            @Suppress("DEPRECATION")
            ttsLocal.speak(textoLoop, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    fun parar() {
        loopAtivo = false
        handler.removeCallbacksAndMessages(null)
        tts?.stop()
        tts?.shutdown()
        tts = null
        textoLoop = ""
    }
}