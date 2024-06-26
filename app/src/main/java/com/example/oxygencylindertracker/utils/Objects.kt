package com.example.oxygencylindertracker.utils

data class User (val phoneNumber: String, val name : String)

data class UserValidationResponse (
    val success : Boolean,
    val isEmpty : Boolean,
    val isError : Boolean
)

data class Cylinder(
    val id: String,
    val currentOwner: String,
    val timestamp: String,
    val date: Long,
    val type: String,
    val isCitizen: Boolean
)

data class Citizen (
    val address: String,
    val name : String,
    val imageLink : String,
    val phone : String
)