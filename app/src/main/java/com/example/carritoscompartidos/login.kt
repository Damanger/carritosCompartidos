package com.example.carritoscompartidos

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.carritoscompartidos.ui.theme.CarritosCompartidosTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var selectedOption by remember { mutableStateOf("") }
    var userData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(context, account.idToken!!, selectedOption, userData)
                } else {
                    showToast(context, "Error al iniciar sesión con Google")
                }
            } catch (e: ApiException) {
                showToast(context, "Error al iniciar sesión con Google: ${e.message}")
            }
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var showFormDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize(), // Para ocupar toda la pantalla
        horizontalAlignment = Alignment.CenterHorizontally, // Centra horizontalmente
        verticalArrangement = Arrangement.Center // Centra verticalmente
    ) {
        Text(
            text = "¡Carritos compartidos!",
            fontSize = MaterialTheme.typography.headlineLarge.fontSize, // Tamaño de fuente del texto
            modifier = Modifier.padding(bottom = 5.dp) // Espacio entre el texto y la imagen
        )
        Image(
            painter = painterResource(id = R.drawable.compa),
            contentDescription = null,
            modifier = Modifier.size(300.dp) // Tamaño de la imagen
        )
        Spacer(modifier = Modifier.height(16.dp)) // Espacio entre imagen y botones
        Button(
            onClick = {
                val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val signInClient = GoogleSignIn.getClient(context, signInOptions)
                signInLauncher.launch(signInClient.signInIntent) },
            modifier = Modifier.fillMaxWidth(0.8f) // Ancho del botón del 80% del ancho de la pantalla
        ) {
            Text(text = "Ingresar con Google")
        }
        Spacer(modifier = Modifier.height(16.dp)) // Espacio entre botones
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(0.8f) // Ancho del botón del 80% del ancho de la pantalla
        ) {
            Text(text = "Registrarme")
        }
    }

    // Dialogos
    if (showDialog) {
        RegisterDialog(
            onDismiss = { showDialog = false },
            onConfirm = { option ->
                selectedOption = option
                showDialog = false
                showFormDialog = true
            }
        )
    }

    // Formulario de registro
    if (showFormDialog) {
        when (selectedOption) {
            "Conductor" -> ConductorFormDialog(onDismiss = { showFormDialog = false }) { data ->
                userData = data
                showFormDialog = false
                onSubmitConductor(data, context)
            }
            "Usuario" -> UsuarioFormDialog(onDismiss = { showFormDialog = false }) { data ->
                userData = data
                showFormDialog = false
                onSubmitUsuario(data, context)
            }
        }
    }
}

@Composable
fun RegisterDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedOption by remember { mutableStateOf("Conductor") }
    val context = LocalContext.current

    // Dialogos
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Registrarse")
        },
        text = {
            Column {
                Text("Selecciona una opción:")
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (selectedOption == "Conductor"),
                        onClick = { selectedOption = "Conductor" }
                    )
                    Text("Registrarme como conductor")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (selectedOption == "Usuario"),
                        onClick = { selectedOption = "Usuario" }
                    )
                    Text("Registrarme como usuario")
                }
            }
        },
        confirmButton = {
            // Botón de confirmación
            Button(
                onClick = {
                    showToast(context, "Registrándote como $selectedOption")
                    onConfirm(selectedOption)
                },
                colors = ButtonDefaults.run { val buttonColors: ButtonColors =
                    buttonColors(Color.Green.copy(alpha = 0.5f))
                    buttonColors
                },
            ) {
                Text("Confirmar", color = Color.Black.copy(alpha = 0.75f))
            }

        },
        dismissButton = {
            Button(onClick = onDismiss,
                colors = ButtonDefaults.run { val buttonColors: ButtonColors =
                buttonColors(Color.Red.copy(alpha = 0.5f))
                buttonColors
            },) {
                Text("Cancelar", color = Color.Black.copy(alpha = 0.75f))
            }
        }
    )
}

@Composable
fun ConductorFormDialog(onDismiss: () -> Unit, onSubmit: (Map<String, String>) -> Unit) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var placas by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correoError by remember { mutableStateOf(false) }
    var placasError by remember { mutableStateOf(false) }
    var telefonoError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val tipoConductor = "1"

    val allFieldsValid = !correoError && !placasError && !telefonoError && !passwordError && nombre.isNotBlank() && password.isNotBlank()

    fun isValidPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@_!$]).{8,}$")
        return passwordRegex.containsMatchIn(password)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Registro como Conductor")
        },
        text = {
            Column {
                TextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") }
                )
                Spacer(modifier = Modifier.height(5.dp))
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = !isValidPassword(it)
                    },
                    label = { Text("Contraseña") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Lock
                        else Icons.Filled.Warning

                        IconButton(onClick = {
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    },
                    isError = passwordError
                )
                if (passwordError) {
                    Text(
                        text = "La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial (@, _, !, $)",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                TextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        correoError = !it.endsWith("@gmail.com")
                    },
                    label = { Text("Correo") },
                    isError = correoError
                )
                if (correoError) {
                    Text(
                        text = "El correo debe ser un @gmail.com",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                TextField(
                    value = placas,
                    onValueChange = {
                        placas = it.uppercase()
                        placasError = !placas.matches(Regex("^[A-Z]{3}[0-9]{4}\$"))
                    },
                    label = { Text("Placas") },
                    isError = placasError
                )
                if (placasError) {
                    Text(
                        text = "Las placas deben tener el formato XXX1234",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                TextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it.filter { char -> char.isDigit() }
                        telefonoError = telefono.length > 10
                    },
                    label = { Text("Número de Teléfono") },
                    isError = telefonoError
                )
                if (telefonoError) {
                    Text(
                        text = "El número de teléfono debe tener un máximo de 10 dígitos",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (allFieldsValid) {
                        val data = mapOf(
                            "nombre" to nombre,
                            "password" to password,
                            "correo" to correo,
                            "placas" to placas,
                            "telefono" to telefono,
                            "tipo" to tipoConductor
                        )
                        onSubmit(data)
                    } else {
                        showToast(context, "Por favor completa todos los campos correctamente antes de continuar")
                    }
                },
                colors = ButtonDefaults.run { val buttonColors: ButtonColors =
                    buttonColors(Color.Green.copy(alpha = 0.5f))
                    buttonColors
                },
                enabled = allFieldsValid
            ) {
                Text("Registrarse", color = Color.Black.copy(alpha = 0.75f))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.run { val buttonColors: ButtonColors =
                    buttonColors(Color.Red.copy(alpha = 0.5f))
                    buttonColors
                }
            ) {
                Text("Cancelar", color = Color.Black.copy(alpha = 0.75f))
            }
        }
    )
}

@Composable
fun UsuarioFormDialog(onDismiss: () -> Unit, onSubmit: (Map<String, String>) -> Unit) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf(false) }
    var correoError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val tipoUsuario = "2"

    val allFieldsValid = nombre.isNotBlank() && !nombreError && correo.isNotBlank() && !correoError && !passwordError && password.isNotBlank()

    fun isValidPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@_!$]).{8,}$")
        return passwordRegex.containsMatchIn(password)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Registro como Usuario")
        },
        text = {
            Column {
                TextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        nombreError = it.isBlank()
                    },
                    label = { Text("Nombre") },
                    isError = nombreError
                )
                if (nombreError) {
                    Text(
                        text = "Por favor ingresa un nombre",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = !isValidPassword(it)
                    },
                    label = { Text("Contraseña") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Lock
                        else Icons.Filled.Warning

                        IconButton(onClick = {
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    },
                    isError = passwordError
                )
                if (passwordError) {
                    Text(
                        text = "La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial (@, _, !, $)",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                TextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        correoError = !it.endsWith("@gmail.com")
                    },
                    label = { Text("Correo") },
                    isError = correoError
                )
                if (correoError) {
                    Text(
                        text = "El correo debe ser un @gmail.com",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (allFieldsValid) {
                        val data = mapOf(
                            "nombre" to nombre,
                            "correo" to correo,
                            "password" to password,
                            "tipo" to tipoUsuario
                        )
                        onSubmit(data)
                    } else {
                        showToast(context, "Por favor completa todos los campos correctamente antes de continuar")
                    }
                }, colors = ButtonDefaults.run { val buttonColors: ButtonColors =
                    buttonColors(Color.Green.copy(alpha = 0.5f))
                    buttonColors
                },
                enabled = allFieldsValid
            ) {
                Text("Registrarse", color = Color.Black.copy(alpha = 0.75f))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.run { val buttonColors: ButtonColors =
                    buttonColors(Color.Red.copy(alpha = 0.5f))
                    buttonColors
                }
            ) {
                Text("Cancelar", color = Color.Black.copy(alpha = 0.75f))
            }
        }
    )
}

fun showToast(context: Context, message: String) {
    val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
    toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
    toast.show()
}

fun onSubmitConductor(data: Map<String, String>, context: Context) {
    val database = Firebase.database
    val ref = database.getReference("conductores")

    ref.push().setValue(data).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            showToast(context, "Registro de conductor exitoso")
        } else {
            showToast(context, "Error al registrar conductor: ${task.exception?.message}")
        }
    }
}

fun onSubmitUsuario(data: Map<String, String>, context: Context) {
    val database = Firebase.database
    val ref = database.getReference("usuarios")

    ref.push().setValue(data).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            showToast(context, "Registro de usuario exitoso")
        } else {
            showToast(context, "Error al registrar usuario: ${task.exception?.message}")
        }
    }
}

private fun registerUserInDatabase(user: FirebaseUser?, selectedOption: String, additionalData: Map<String, String>) {
    user?.let { firebaseUser ->
        val database = Firebase.database
        val ref = when (selectedOption) {
            "Conductor" -> database.getReference("conductores")
            "Usuario" -> database.getReference("usuarios")
            else -> null
        }

        val userData = additionalData.toMutableMap().apply {
            this["userId"] = firebaseUser.uid
            this["nombre"] = firebaseUser.displayName ?: ""
            this["correo"] = firebaseUser.email ?: ""
        }

        ref?.push()?.setValue(userData)
    }
}

private fun firebaseAuthWithGoogle(context: Context, idToken: String, selectedOption: String, additionalData: Map<String, String>) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    FirebaseAuth.getInstance().signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                val user = FirebaseAuth.getInstance().currentUser
                showToast(context, "Inicio de sesión exitoso: ${user?.displayName}")
                registerUserInDatabase(user, selectedOption, additionalData)
            } else {
                // If sign in fails, display a message to the user.
                showToast(context, "Error al iniciar sesión: ${task.exception?.message}")
            }
        }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CarritosCompartidosTheme {
        LoginScreen()
    }
}
