package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val tvNomeUsuario = findViewById<TextView>(R.id.tvNomeUsuario)
        val tvEmailUsuario = findViewById<TextView>(R.id.tvEmailUsuario)
        val btnSair = findViewById<Button>(R.id.btnSair)

        tvNomeUsuario.text = FakeUserStore.nome ?: "Usuário não identificado"
        tvEmailUsuario.text = FakeUserStore.email ?: "E-mail não informado"

        btnSair.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}