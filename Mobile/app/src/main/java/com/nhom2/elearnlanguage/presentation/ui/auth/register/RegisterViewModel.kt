package com.nhom2.elearnlanguage.presentation.ui.auth.register

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel // -> service/component
class RegisterViewModel @Inject constructor(): ViewModel() {//Inject constructor() -> autowired
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow() // Biến thể hiện để cập nhật trên giao diện, không trực tiếp sửa đổi

    private val _email = MutableStateFlow("") // private field
    val email = _email.asStateFlow() // getEmail()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    fun setName(value: String){
        _name.value = value
    }

    fun setEmail(value: String){
        _email.value = value
    }

    fun setPassword(value: String){
        _password.value = value
    }
}