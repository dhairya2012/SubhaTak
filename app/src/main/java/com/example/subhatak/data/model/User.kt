package com.example.subhatak.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
