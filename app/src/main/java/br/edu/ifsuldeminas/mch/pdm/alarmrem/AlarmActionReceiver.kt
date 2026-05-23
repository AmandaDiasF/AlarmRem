package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class AlarmActionReceiver : BroadcastReceiver() {

    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val notificationId = intent.getIntExtra("notificationId", 0)
        val alarmeId = intent.getIntExtra("alarmeId", 0)
        val nome = intent.getStringExtra("nomeRemedio") ?: "Medicamento"
        val descricao = intent.getStringExtra("descricao") ?: "Hora do remédio"
        val fotoUri = intent.getStringExtra("fotoUri")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (action) {
            "ACTION_OK" -> {
                notificationManager.cancel(notificationId)
            }

            "ACTION_ADIAR" -> {
                notificationManager.cancel(notificationId)

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val novoHorario = Calendar.getInstance().apply {
                    add(Calendar.MINUTE, 5)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        novoHorario.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        novoHorario.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }
}