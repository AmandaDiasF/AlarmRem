package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmeId = intent.getIntExtra("alarmeId", 0)
        val nome = intent.getStringExtra("nomeRemedio") ?: "Medicamento"
        val descricao = intent.getStringExtra("descricao") ?: "Hora do remédio"
        val fotoUriString = intent.getStringExtra("fotoUri")

        val channelId = "alarmrem_channel"
        val notificationId = alarmeId

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarmes AlarmRem",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Canal de notificações dos alarmes"
            channel.enableVibration(true)
            manager.createNotificationChannel(channel)
        }

        val telaIntent = Intent(context, AlarmScreenActivity::class.java).apply {
            putExtra("notificationId", notificationId)
            putExtra("alarmeId", alarmeId)
            putExtra("nomeRemedio", nome)
            putExtra("descricao", descricao)
            putExtra("fotoUri", fotoUriString)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val telaPendingIntent = PendingIntent.getActivity(
            context,
            alarmeId + 3000,
            telaIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val okIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = "ACTION_OK"
            putExtra("notificationId", notificationId)
            putExtra("alarmeId", alarmeId)
        }

        val adiarIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = "ACTION_ADIAR"
            putExtra("notificationId", notificationId)
            putExtra("alarmeId", alarmeId)
            putExtra("nomeRemedio", nome)
            putExtra("descricao", descricao)
            putExtra("fotoUri", fotoUriString)
        }

        val okPendingIntent = PendingIntent.getBroadcast(
            context,
            alarmeId + 1000,
            okIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val adiarPendingIntent = PendingIntent.getBroadcast(
            context,
            alarmeId + 2000,
            adiarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Hora do remédio: $nome")
            .setContentText(descricao)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(telaPendingIntent)
            .setFullScreenIntent(telaPendingIntent, true)
            .addAction(0, "Adiar", adiarPendingIntent)
            .addAction(0, "OK", okPendingIntent)

        val bitmap = carregarBitmap(context, fotoUriString)

        if (bitmap != null) {
            builder = builder
                .setLargeIcon(bitmap)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null as Bitmap?)
                        .setBigContentTitle("Hora do remédio: $nome")
                        .setSummaryText(descricao)
                )
        } else {
            builder = builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(descricao)
                    .setBigContentTitle("Hora do remédio: $nome")
            )
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(notificationId, builder.build())
        }

        try {
            context.startActivity(telaIntent)
        } catch (_: Exception) {
        }

        falarDescricao(context, nome, descricao)
    }

    private fun carregarBitmap(context: Context, fotoUriString: String?): Bitmap? {
        if (fotoUriString.isNullOrEmpty()) return null

        return try {
            val uri = Uri.parse(fotoUriString)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun falarDescricao(context: Context, nome: String, descricao: String) {
        val textoFalado = "Atenção, hora de tomar: $nome. $descricao"

        lateinit var tts: TextToSpeech

        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val resultadoIdioma = tts.setLanguage(Locale("pt", "BR"))

                if (resultadoIdioma != TextToSpeech.LANG_MISSING_DATA &&
                    resultadoIdioma != TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val params = Bundle().apply {
                            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
                            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                        }

                        tts.speak(
                            textoFalado,
                            TextToSpeech.QUEUE_FLUSH,
                            params,
                            "alarmrem_tts_1"
                        )

                        tts.speak(
                            textoFalado,
                            TextToSpeech.QUEUE_ADD,
                            params,
                            "alarmrem_tts_2"
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        tts.speak(textoFalado, TextToSpeech.QUEUE_FLUSH, null)
                        @Suppress("DEPRECATION")
                        tts.speak(textoFalado, TextToSpeech.QUEUE_ADD, null)
                    }
                }
            }
        }
    }
}