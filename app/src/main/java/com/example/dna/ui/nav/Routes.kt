package com.example.dna.ui.nav

import kotlinx.serialization.Serializable

@Serializable
object HomeScreen

@Serializable
object AlphabetScreen

@Serializable
object QuizScreen

@Serializable
data class ARScreen(val model: String)