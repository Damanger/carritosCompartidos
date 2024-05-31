@file:Suppress("NAME_SHADOWING")

package com.example.carritoscompartidos

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var selectedOption by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var showFormDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showWelcomeDialog by remember { mutableStateOf(false) }
    var welcomeMessage by remember { mutableStateOf("") }

    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }

    var userType by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize(), // Para ocupar toda la pantalla
        horizontalAlignment = Alignment.CenterHorizontally, // Centra horizontalmente
        verticalArrangement = Arrangement.Center // Centra verticalmente
    ) {
        Text(
            text = "¡CompaRide!",
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
            onClick = { showLoginDialog = true },
            modifier = Modifier.fillMaxWidth(0.8f) // Ancho del botón del 80% del ancho de la pantalla
        ) {
            Text(text = "Ingresar")
        }
        Spacer(modifier = Modifier.height(16.dp)) // Espacio entre botones
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(0.8f) // Ancho del botón del 80% del ancho de la pantalla
        ) {
            Text(text = "Regístrate")
        }
    }

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text(text = "Ingresar") },
            text = {
                Column {
                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = { loginEmail = it },
                        label = { Text("Correo electrónico") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                        isError = loginError
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                        isError = loginError
                    )
                    if (loginError) {
                        Text(text = "Error al iniciar sesión. Por favor, verifique sus credenciales.", color = Color.Red)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Dentro del callback de éxito de verificación de credenciales
                        verifyLoginCredentials(loginEmail, loginPassword, context) { success, name, type ->
                            if (success) {
                                welcomeMessage = "Bienvenido, $name"
                                userType = type.toString()
                                showWelcomeDialog = true
                                showLoginDialog = false

                            } else {
                                loginError = true
                            }
                        }
                    }
                    ,
                    colors = ButtonDefaults.buttonColors(Color.Green.copy(alpha = 0.5f))
                ) {
                    Text(text = "Ingresar", color = Color.Black.copy(alpha = 0.75f))
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLoginDialog = false },
                    colors = ButtonDefaults.buttonColors(Color.Red.copy(alpha = 0.5f))
                ) {
                    Text(text = "Cancelar", color = Color.Black.copy(alpha = 0.75f))
                }
            }
        )
    }

    if (showWelcomeDialog) {
        AlertDialog(
            onDismissRequest = { showWelcomeDialog = false },
            title = { Text(text = "Bienvenido") },
            text = { Text(text = welcomeMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        // Lanzar MapActivity y pasar el tipo de usuario como extra
                        val intent = Intent(context, MapActivity::class.java)
                        intent.putExtra("USER_TYPE", userType)
                        context.startActivity(intent)
                        // Limpiar los datos ingresados
                        loginEmail = ""
                        loginPassword = ""

                        showWelcomeDialog = false
                        loginError = false
                    },colors = ButtonDefaults.run { val buttonColors: ButtonColors =
                        ButtonDefaults.buttonColors(Color.Green.copy(alpha = 0.5f))
                        buttonColors
                    }
                ) {
                    Text(text = "Ingresar", color = Color.Black.copy(alpha = 0.75f))
                }
            }
        )
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
            "Conductor" -> ConductorFormDialog(onDismiss = { showFormDialog = false })
            "Usuario" -> UsuarioFormDialog(onDismiss = { showFormDialog = false })
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
fun ConductorFormDialog(onDismiss: () -> Unit) {
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
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@_!$])(?!.*\\s).{8,}$")
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
                        val correoParts = it.split("@")
                        correoError = correoParts.size != 2 || !correoParts[1].endsWith("gmail.com") && !correoParts[1].endsWith("hotmail.com") && !correoParts[1].endsWith("gs.utm.mx")

                    },
                    label = { Text("Correo") },
                    isError = correoError
                )
                if (correoError) {
                    Text(
                        text = "El correo debe ser un @gmail.com o @hotmail.com o @gs.utm.mx",
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
                        onSubmitConductor(data, context) {
                            // Esta lambda se ejecutará si el registro es exitoso
                            showToast(context, "Registro de conductor exitoso")
                            onDismiss() // Cerrar el diálogo después del registro exitoso
                        }
                    } else {
                        showToast(context, "Por favor completa todos los campos correctamente antes de continuar")
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Green.copy(alpha = 0.5f)),
                enabled = allFieldsValid
            ) {
                Text("Registrarse", color = Color.Black.copy(alpha = 0.75f))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(Color.Red.copy(alpha = 0.5f))
            ) {
                Text("Cancelar", color = Color.Black.copy(alpha = 0.75f))
            }
        }
    )
}

@Composable
fun UsuarioFormDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf(false) }
    var correoError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val tipoUsuario = "2"

    val allFieldsValid = nombre.isNotBlank() && !nombreError && !passwordError && !correoError && password.isNotBlank()

    fun isValidPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@_!$])(?!.*\\s).{8,}$")
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
                        nombreError = nombre.length > 25
                    },
                    label = { Text("Nombre Completo") },
                    isError = nombreError
                )
                if (nombreError) {
                    Text(
                        text = "El nombre debe tener un máximo de 25 caracteres",
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
                        val correoParts = it.split("@")
                        correoError = correoParts.size != 2 || !correoParts[1].endsWith("gmail.com") && !correoParts[1].endsWith("hotmail.com") && !correoParts[1].endsWith("gs.utm.mx")
                    },
                    label = { Text("Correo") },
                    isError = correoError
                )
                if (correoError) {
                    Text(
                        text = "El correo debe ser un @gmail.com o @hotmail.com o @gs.utm.mx",
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
                            "tipo" to tipoUsuario
                        )
                        onSubmitUsuario(data, context) {
                            // Esta lambda se ejecutará si el registro es exitoso
                            showToast(context, "Registro de usuario exitoso")
                            onDismiss() // Cerrar el diálogo después del registro exitoso
                        }
                    } else {
                        showToast(context, "Por favor completa todos los campos correctamente antes de continuar")
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Green.copy(alpha = 0.5f)),
                enabled = allFieldsValid
            ) {
                Text("Registrarse", color = Color.Black.copy(alpha = 0.75f))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(Color.Red.copy(alpha = 0.5f))
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

fun isEmailExistInBoth(email: String, onResult: (Boolean) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val conductoresRef = database.getReference("conductores")
    val usuariosRef = database.getReference("usuarios")

    conductoresRef.orderByChild("correo").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(conductoresSnapshot: DataSnapshot) {
            if (conductoresSnapshot.exists()) {
                onResult(true)
            } else {
                usuariosRef.orderByChild("correo").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(usuariosSnapshot: DataSnapshot) {
                        onResult(usuariosSnapshot.exists())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onResult(false) // En caso de error, asumir que el correo no existe para permitir la continuación
                    }
                })
            }
        }

        override fun onCancelled(error: DatabaseError) {
            onResult(false) // En caso de error, asumir que el correo no existe para permitir la continuación
        }
    })
}

fun onSubmitConductor(data: Map<String, String>, context: Context, onDismiss: () -> Unit) {
    val correo = data["correo"] ?: ""

    isEmailExistInBoth(correo) { emailExist ->
        if (emailExist) {
            showToast(context, "El correo ya está registrado. Por favor, utiliza otro correo.")
        } else {
            val database = Firebase.database
            val ref = database.getReference("conductores")

            // Proceder con el registro
            ref.push().setValue(data).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(context, "Registro de conductor exitoso")
                    onDismiss()  // Cerrar el diálogo al completar el registro exitosamente
                } else {
                    showToast(context, "Error al registrar conductor: ${task.exception?.message}")
                }
            }
        }
    }
}

fun onSubmitUsuario(data: Map<String, String>, context: Context, onDismiss: () -> Unit) {
    val correo = data["correo"] ?: ""

    isEmailExistInBoth(correo) { emailExist ->
        if (emailExist) {
            showToast(context, "El correo ya está registrado. Por favor, utiliza otro correo.")
        } else {
            val database = Firebase.database
            val ref = database.getReference("usuarios")

            // Proceder con el registro
            ref.push().setValue(data).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(context, "Registro de usuario exitoso")
                    onDismiss()  // Cerrar el diálogo al completar el registro exitosamente
                } else {
                    showToast(context, "Error al registrar usuario: ${task.exception?.message}")
                }
            }
        }
    }
}

fun verifyLoginCredentials(
    email: String, password: String, context: Context,
    onResult: (Boolean, String?, String?) -> Unit
) {
    val database = Firebase.database
    val usersRef = database.getReference("usuarios")
    val conductorsRef = database.getReference("conductores")
    val adminRef = database.getReference("administradores")

    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var userFound = false
            var userName: String? = null
            var userType: String? = null

            for (userSnapshot in snapshot.children) {
                val userPassword = userSnapshot.child("password").getValue(String::class.java)
                if (userSnapshot.child("correo").getValue(String::class.java) == email && userPassword == password) {
                    userName = userSnapshot.child("nombre").getValue(String::class.java)
                    userType = userSnapshot.child("tipo").getValue(String::class.java)
                    userFound = true
                    break
                }
            }

            onResult(userFound, userName, userType)
        }

        override fun onCancelled(error: DatabaseError) {
            showToast(context, "Error al verificar las credenciales: ${error.message}")
            onResult(false, null, null)
        }
    }

    usersRef.addListenerForSingleValueEvent(listener)
    conductorsRef.addListenerForSingleValueEvent(listener)
    adminRef.addListenerForSingleValueEvent(listener)
}

fun openMapActivity(context: Context, userType: String) {
    val intent = Intent(context, MapActivity::class.java).apply {
        putExtra("USER_TYPE", userType)
    }
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CarritosCompartidosTheme {
        LoginScreen()
    }
}
