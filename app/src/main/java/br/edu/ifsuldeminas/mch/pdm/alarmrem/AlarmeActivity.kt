package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.util.Calendar

class AlarmeActivity : AppCompatActivity() {

    private lateinit var etNomeRemedio: EditText
    private lateinit var etDescricao: EditText
    private lateinit var btnSelecionarHora: Button
    private lateinit var btnAdicionarFoto: Button
    private lateinit var btnSalvarAlarme: Button
    private lateinit var imgMedicamento: ImageView

    private lateinit var cbDom: CheckBox
    private lateinit var cbSeg: CheckBox
    private lateinit var cbTer: CheckBox
    private lateinit var cbQua: CheckBox
    private lateinit var cbQui: CheckBox
    private lateinit var cbSex: CheckBox
    private lateinit var cbSab: CheckBox
    private lateinit var cbTodos: CheckBox

    private var fotoUri: Uri? = null
    private var horaSelecionada: Int = -1
    private var minutoSelecionado: Int = -1

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
            if (concedida) {
                abrirCamera()
            } else {
                Toast.makeText(this, "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
            }
        }

    private val tirarFotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { sucesso ->
            if (sucesso) {
                fotoUri?.let { uri ->
                    imgMedicamento.setImageURI(null)
                    imgMedicamento.setImageURI(uri)
                    Toast.makeText(this, "Foto capturada com sucesso", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this, "URI da foto inválida", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Não foi possível tirar a foto", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarme)

        etNomeRemedio = findViewById(R.id.etNomeRemedio)
        etDescricao = findViewById(R.id.etDescricao)
        btnSelecionarHora = findViewById(R.id.btnSelecionarHora)
        btnAdicionarFoto = findViewById(R.id.btnAdicionarFoto)
        btnSalvarAlarme = findViewById(R.id.btnSalvarAlarme)
        imgMedicamento = findViewById(R.id.imgMedicamento)

        cbDom = findViewById(R.id.cbDom)
        cbSeg = findViewById(R.id.cbSeg)
        cbTer = findViewById(R.id.cbTer)
        cbQua = findViewById(R.id.cbQua)
        cbQui = findViewById(R.id.cbQui)
        cbSex = findViewById(R.id.cbSex)
        cbSab = findViewById(R.id.cbSab)
        cbTodos = findViewById(R.id.cbTodos)

        preencherDadosSeForEdicao()

        btnSelecionarHora.setOnClickListener {
            abrirSeletorDeHora()
        }

        btnAdicionarFoto.setOnClickListener {
            verificarPermissaoECapturarFoto()
        }

        btnSalvarAlarme.setOnClickListener {
            salvarAlarme()
        }
    }

    private fun preencherDadosSeForEdicao() {
        val nomeRecebido = intent.getStringExtra("nomeRemedio")
        val descricaoRecebida = intent.getStringExtra("descricao")
        val horaRecebida = intent.getStringExtra("hora")
        val diasRecebidos = intent.getStringExtra("diasSelecionados")
        val fotoUriRecebida = intent.getStringExtra("fotoUri")

        if (!nomeRecebido.isNullOrEmpty()) {
            etNomeRemedio.setText(nomeRecebido)
        }

        if (!descricaoRecebida.isNullOrEmpty()) {
            etDescricao.setText(descricaoRecebida)
        }

        if (!horaRecebida.isNullOrEmpty()) {
            btnSelecionarHora.text = horaRecebida
            val partes = horaRecebida.split(":")
            if (partes.size == 2) {
                horaSelecionada = partes[0].toIntOrNull() ?: -1
                minutoSelecionado = partes[1].toIntOrNull() ?: -1
            }
        }

        if (!fotoUriRecebida.isNullOrEmpty()) {
            fotoUri = Uri.parse(fotoUriRecebida)
            imgMedicamento.setImageURI(null)
            imgMedicamento.setImageURI(fotoUri)
        }

        marcarDiasSelecionados(diasRecebidos)
    }

    private fun marcarDiasSelecionados(diasRecebidos: String?) {
        if (diasRecebidos.isNullOrEmpty()) return

        if (diasRecebidos == "Todos os dias") {
            cbTodos.isChecked = true
            return
        }

        val dias = diasRecebidos.split(",").map { it.trim() }

        cbDom.isChecked = dias.contains("Dom")
        cbSeg.isChecked = dias.contains("Seg")
        cbTer.isChecked = dias.contains("Ter")
        cbQua.isChecked = dias.contains("Qua")
        cbQui.isChecked = dias.contains("Qui")
        cbSex.isChecked = dias.contains("Sex")
        cbSab.isChecked = dias.contains("Sáb")
    }

    private fun abrirSeletorDeHora() {
        val calendario = Calendar.getInstance()
        val horaAtual = if (horaSelecionada != -1) horaSelecionada else calendario.get(Calendar.HOUR_OF_DAY)
        val minutoAtual = if (minutoSelecionado != -1) minutoSelecionado else calendario.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hora, minuto ->
                horaSelecionada = hora
                minutoSelecionado = minuto
                btnSelecionarHora.text = String.format("%02d:%02d", hora, minuto)
            },
            horaAtual,
            minutoAtual,
            true
        )

        timePickerDialog.show()
    }

    private fun salvarAlarme() {
        val nome = etNomeRemedio.text.toString().trim()
        val descricao = etDescricao.text.toString().trim()
        val diasSelecionados = obterDiasSelecionados()
        val idRecebido = intent.getIntExtra("id", -1)

        if (nome.isEmpty()) {
            etNomeRemedio.error = "Digite o nome do remédio"
            etNomeRemedio.requestFocus()
            return
        }

        if (descricao.isEmpty()) {
            etDescricao.error = "Digite a descrição"
            etDescricao.requestFocus()
            return
        }

        if (horaSelecionada == -1 || minutoSelecionado == -1) {
            Toast.makeText(this, "Selecione uma hora", Toast.LENGTH_SHORT).show()
            return
        }

        val horaFormatada = String.format("%02d:%02d", horaSelecionada, minutoSelecionado)

        val resultadoIntent = Intent().apply {
            putExtra("id", idRecebido)
            putExtra("nomeRemedio", nome)
            putExtra("descricao", descricao)
            putExtra("hora", horaFormatada)
            putExtra("diasSelecionados", diasSelecionados)
            putExtra("fotoUri", fotoUri?.toString())
        }

        setResult(RESULT_OK, resultadoIntent)

        Toast.makeText(
            this,
            "Alarme salvo com sucesso!\n$diasSelecionados",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    private fun obterDiasSelecionados(): String {
        val dias = mutableListOf<String>()

        if (cbTodos.isChecked) {
            return "Todos os dias"
        }

        if (cbDom.isChecked) dias.add("Dom")
        if (cbSeg.isChecked) dias.add("Seg")
        if (cbTer.isChecked) dias.add("Ter")
        if (cbQua.isChecked) dias.add("Qua")
        if (cbQui.isChecked) dias.add("Qui")
        if (cbSex.isChecked) dias.add("Sex")
        if (cbSab.isChecked) dias.add("Sáb")

        return if (dias.isEmpty()) {
            "Nenhum dia selecionado"
        } else {
            dias.joinToString(", ")
        }
    }

    private fun verificarPermissaoECapturarFoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            abrirCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamera() {
        val arquivoFoto = criarArquivoDeImagem()

        fotoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            arquivoFoto
        )

        fotoUri?.let { uri ->
            tirarFotoLauncher.launch(uri)
        } ?: run {
            Toast.makeText(this, "Erro ao criar URI da foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun criarArquivoDeImagem(): File {
        val diretorio = getExternalFilesDir("Pictures")
            ?: throw IllegalStateException("Não foi possível acessar a pasta de imagens")

        if (!diretorio.exists()) {
            diretorio.mkdirs()
        }

        return File.createTempFile(
            "medicamento_",
            ".jpg",
            diretorio
        )
    }
}