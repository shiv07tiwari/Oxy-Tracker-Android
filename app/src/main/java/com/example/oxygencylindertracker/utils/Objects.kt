package com.example.oxygencylindertracker.utils

data class User (val phoneNumber: String, val name : String)

data class UserValidationResponse (
    val success : Boolean,
    val isEmpty : Boolean,
    val isError : Boolean
)