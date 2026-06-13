package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvCadastreSe = findViewById<TextView>(R.id.tvCadastreSe)

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()
            if(email.isEmpty() || senha.isEmpty()){
                Toast.makeText(this, "Os campos não podem ser vazios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(email != FakeUserStore.email || senha !=FakeUserStore.senha){
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
}