package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerViewAlarmes: RecyclerView
    private lateinit var alarmeAdapter: AlarmeAdapter

    private val listaAlarmes = mutableListOf<Alarme>()
    private var proximoId = 1
    private var posicaoEmEdicao = -1

    private val alarmeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data

                val idRecebido = data?.getIntExtra("id", -1) ?: -1
                val nome = data?.getStringExtra("nomeRemedio") ?: ""
                val descricao = data?.getStringExtra("descricao") ?: ""
                val hora = data?.getStringExtra("hora") ?: ""
                val dias = data?.getStringExtra("diasSelecionados") ?: ""
                val fotoUri = data?.getStringExtra("fotoUri")

                val alarme = Alarme(
                    id = if (idRecebido != -1) idRecebido else proximoId++,
                    nomeRemedio = nome,
                    descricao = descricao,
                    hora = hora,
                    diasSelecionados = dias,
                    fotoUri = fotoUri
                )

                if (posicaoEmEdicao >= 0) {
                    cancelarAlarme(listaAlarmes[posicaoEmEdicao])
                    listaAlarmes[posicaoEmEdicao] = alarme
                    alarmeAdapter.atualizarAlarme(posicaoEmEdicao, alarme)
                    posicaoEmEdicao = -1
                } else {
                    listaAlarmes.add(alarme)
                    alarmeAdapter.adicionarAlarme()
                }

                agendarAlarme(alarme)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        verificarPermissoesDeAlarme()

        val btnPerfil = findViewById<Button>(R.id.btnPerfil)
        val btnAdicionarAlarme = findViewById<Button>(R.id.btnAdicionarAlarme)
        recyclerViewAlarmes = findViewById(R.id.recyclerViewAlarmes)

        alarmeAdapter = AlarmeAdapter(
            listaAlarmes = listaAlarmes,
            onEditar = { alarme, position ->
                posicaoEmEdicao = position

                val intent = Intent(this, AlarmeActivity::class.java).apply {
                    putExtra("id", alarme.id)
                    putExtra("nomeRemedio", alarme.nomeRemedio)
                    putExtra("descricao", alarme.descricao)
                    putExtra("hora", alarme.hora)
                    putExtra("diasSelecionados", alarme.diasSelecionados)
                    putExtra("fotoUri", alarme.fotoUri)
                }

                alarmeLauncher.launch(intent)
            },
            onExcluir = { alarme, position ->
                cancelarAlarme(alarme)
                listaAlarmes.removeAt(position)
                alarmeAdapter.removerAlarme(position)
            }
        )

        recyclerViewAlarmes.adapter = alarmeAdapter

        btnPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        btnAdicionarAlarme.setOnClickListener {
            posicaoEmEdicao = -1
            val intent = Intent(this, AlarmeActivity::class.java)
            alarmeLauncher.launch(intent)
        }
    }

    private fun verificarPermissoesDeAlarme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (!notificationManager.canUseFullScreenIntent()) {
                Toast.makeText(
                    this,
                    "Ative a permissão de tela cheia para os alarmes funcionarem melhor",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun agendarAlarme(alarme: Alarme) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // A essa altura, a MainActivity já tratou canScheduleExactAlarms().
        // Aqui não abrimos mais tela de permissão para não quebrar o fluxo.

        val partesHora = alarme.hora.split(":")
        if (partesHora.size != 2) return

        val hora = partesHora[0].toIntOrNull() ?: return
        val minuto = partesHora[1].toIntOrNull() ?: return

        val calendario = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("alarmeId", alarme.id)
            putExtra("nomeRemedio", alarme.nomeRemedio)
            putExtra("descricao", alarme.descricao)
            putExtra("fotoUri", alarme.fotoUri)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarme.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val infoIntent = Intent(this, HomeActivity::class.java)
        val infoPendingIntent = PendingIntent.getActivity(
            this,
            alarme.id + 5000,
            infoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmInfo = AlarmManager.AlarmClockInfo(
            calendario.timeInMillis,
            infoPendingIntent
        )

        alarmManager.setAlarmClock(alarmInfo, pendingIntent)
    }

    private fun cancelarAlarme(alarme: Alarme) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("alarmeId", alarme.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarme.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}