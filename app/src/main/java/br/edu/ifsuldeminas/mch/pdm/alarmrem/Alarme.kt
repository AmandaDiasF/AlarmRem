package br.edu.ifsuldeminas.mch.pdm.alarmrem

import java.io.Serializable

data class Alarme(
    val id: Int,
    val nomeRemedio: String,
    val descricao: String,
    val hora: String,
    val diasSelecionados: String,
    val fotoUri: String?
) : Serializable