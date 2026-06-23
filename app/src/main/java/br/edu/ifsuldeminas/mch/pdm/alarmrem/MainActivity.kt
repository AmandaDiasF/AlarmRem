package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Lançador para permissão de notificação (Android 13+)
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
            if (!concedida) {
                Toast.makeText(
                    this,
                    "Permissão de notificação negada. Os alarmes podem não aparecer.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // Lançador para a tela de permissão de alarme exato (Android 12+)
    private val exactAlarmSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    "Alarme exato não permitido. Será usado modo aproximado.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1) Solicita permissões importantes logo na entrada do app
        solicitarPermissaoNotificacaoSeNecessario()
        solicitarPermissaoAlarmeExatoSeNecessario()

        // 2) Lógica de login existente
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvCadastreSe = findViewById<TextView>(R.id.tvCadastreSe)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Os campos não podem ser vazios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email != FakeUserStore.email || senha != FakeUserStore.senha) {
                Toast.makeText(this, "email ou senha incorretos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        tvCadastreSe.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun solicitarPermissaoNotificacaoSeNecessario() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun solicitarPermissaoAlarmeExatoSeNecessario() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                exactAlarmSettingsLauncher.launch(intent)
            }
        }
    }
}