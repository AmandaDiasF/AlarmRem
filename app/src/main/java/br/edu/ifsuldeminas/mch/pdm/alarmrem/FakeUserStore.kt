package br.edu.ifsuldeminas.mch.pdm.alarmrem

object FakeUserStore {
    var nome: String? = null
    var email: String? = null
    var senha: String? = null

    fun isUserRegistered(): Boolean {
        return !nome.isNullOrEmpty() &&
                !email.isNullOrEmpty() &&
                !senha.isNullOrEmpty()
    }
}