package br.edu.ifsuldeminas.mch.pdm.alarmrem

data class Alarme(
    val id: Int,
    val nomeRemedio: String,
    val descricao: String,
    val hora: String,
    val diasSelecionados: String,
    val fotoUri: String?
)