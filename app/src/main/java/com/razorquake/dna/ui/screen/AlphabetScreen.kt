package com.razorquake.dna.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.razorquake.dna.ui.nav.ARScreen
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlphabetScreen(navController: NavController) {
    val listOfAlphabets = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    )
    Column {
        Box(modifier = Modifier.height(60.dp)) {
            Text(text = "Alphabets", fontSize = 24.sp, modifier = Modifier.align(Alignment.Center))
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    state = rememberScrollState()
                ),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center
        ) {
            listOfAlphabets.forEach { alphabet ->
                AlphabetItem(onClick = {
                    navController.navigate(ARScreen(model = alphabet))
                }, alphabet = alphabet)
            }
        }
    }
}

@Composable
fun AlphabetItem(onClick: () -> Unit, alphabet: String, enabled: Boolean = true) {
    val color = remember(
        alphabet
    ) {
        generateRandomLightColor()
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .padding(16.dp)
            .size(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),

    ) {
        Text(
            text = alphabet,
            fontSize = 24.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

fun generateRandomLightColor(): Color {
    val random = Random(System.currentTimeMillis())
    val red = random.nextInt(150, 256)
    val green = random.nextInt(200, 256)
    val blue = random.nextInt(200, 256)
    val color = Color(red, green, blue)
    return color
}