package com.example.groovelt.data.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.concurrent.Volatile


class FirebaseHelper private constructor() {
    var currentUser: FirebaseUser? = null

    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    val userPathDatabase: DatabaseReference
        get() = database.getReference("Users")

    val favoritesPathDatabase: DatabaseReference
        get() = database.getReference("Favorites")

    companion object {
        fun getInstance() = FirebaseHelper()
    }
}
