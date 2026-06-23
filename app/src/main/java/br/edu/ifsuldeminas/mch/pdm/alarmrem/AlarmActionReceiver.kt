package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Para o áudio sempre que uma ação é recebida
        AlarmAudioManager.parar()

        val action = intent.action
        val notificationId = intent.getIntExtra("notificationId", 0)
        val alarmeId = intent.getIntExtra("alarmeId", 0)
        val nome = intent.getStringExtra("nomeRemedio") ?: "Medicamento"
        val descricao = intent.getStringExtra("descricao") ?: "Hora do remédio"
        val fotoUri = intent.getStringExtra("fotoUri")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (action) {
            ACTION_OK -> {
                notificationManager.cancel(notificationId)

                val agora = java.text.SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    java.util.Locale("pt", "BR")
                ).format(java.util.Date())

                val item = HistoricoItem(
                    nomeRemedio = nome,
                    descricao = descricao,
                    dataHoraConclusao = agora
                )

                HistoricoStorage.adicionarRegistro(context, item)
            }

            ACTION_ADIAR -> {
                notificationManager.cancel(notificationId)
                reagendarParaDaqui5Min(context, alarmeId, nome, descricao, fotoUri)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun reagendarParaDaqui5Min(
        context: Context,
        alarmeId: Int,
        nome: String,
        descricao: String,
        fotoUri: String?
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val novoIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarmeId", alarmeId)
            putExtra("nomeRemedio", nome)
            putExtra("descricao", descricao)
            putExtra("fotoUri", fotoUri)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmeId,
            novoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Garante que não haja alarme duplicado
        alarmManager.cancel(pendingIntent)

        val triggerAtMillis = System.currentTimeMillis() + 5 * 60 * 1000L

        // Android 12+ - verificar se pode agendar exato
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            // Fallback recomendado pela documentação: usar setAndAllowWhileIdle
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            return
        }

        // Android M+ com alarme exato e respeitando Doze
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    companion object {
        const val ACTION_OK = "ACTION_OK"
        const val ACTION_ADIAR = "ACTION_ADIAR"
    }
}