package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlarmScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_alarm_screen)

        val imgFoto = findViewById<ImageView>(R.id.imgFotoAlarme)
        val tvTitulo = findViewById<TextView>(R.id.tvTituloAlarme)
        val tvDescricao = findViewById<TextView>(R.id.tvDescricaoAlarme)
        val btnOk = findViewById<Button>(R.id.btnOkAlarme)
        val btnAdiar = findViewById<Button>(R.id.btnAdiarAlarme)

        val notificationId = intent.getIntExtra("notificationId", 0)
        val alarmeId = intent.getIntExtra("alarmeId", 0)
        val nome = intent.getStringExtra("nomeRemedio") ?: "Medicamento"
        val descricao = intent.getStringExtra("descricao") ?: "Hora do remédio"
        val fotoUri = intent.getStringExtra("fotoUri")

        tvTitulo.text = "Hora do remédio: $nome"
        tvDescricao.text = descricao

        if (!fotoUri.isNullOrEmpty()) {
            imgFoto.setImageURI(null)
            imgFoto.setImageURI(Uri.parse(fotoUri))
        } else {
            imgFoto.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        btnOk.setOnClickListener {
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            finish()
        }

        btnAdiar.setOnClickListener {
            val intentAdiar = Intent(this, AlarmActionReceiver::class.java).apply {
                action = "ACTION_ADIAR"
                putExtra("notificationId", notificationId)
                putExtra("alarmeId", alarmeId)
                putExtra("nomeRemedio", nome)
                putExtra("descricao", descricao)
                putExtra("fotoUri", fotoUri)
            }
            sendBroadcast(intentAdiar)
            finish()
        }
    }
}