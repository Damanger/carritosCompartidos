package com.example.carritoscompartidos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.carritoscompartidos.ui.theme.CarritosCompartidosTheme

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperar el tipo de usuario del intent
        val userType = intent.getStringExtra("USER_TYPE") ?: "2" // Valor predeterminado: Usuario
        val userTypeString = convertUserTypeToString(userType)

        setContent {
            CarritosCompartidosTheme {
                MapScreen(userTypeString)
            }
        }
    }

    private fun convertUserTypeToString(userType: String): String {
        return when (userType) {
            "0" -> "Administrador"
            "1" -> "Conductor"
            else -> "Usuario"
        }
    }
}

@Composable
fun MapScreen(userType: String) {
    // Aquí puedes personalizar la pantalla del mapa según el tipo de usuario
    Text(text = "Bienvenido al mapa, $userType")
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    CarritosCompartidosTheme {
        MapScreen(userType = "Usuario")
    }
}
