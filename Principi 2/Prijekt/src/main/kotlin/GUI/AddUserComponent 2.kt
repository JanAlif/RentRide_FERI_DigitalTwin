import GUI.LightBlue
import Util.DatabaseUtil
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.awt.image.BufferedImage
import java.util.regex.Pattern
import javax.imageio.ImageIO

@Composable
fun AddUserComponent(modifier: Modifier = Modifier, onSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var profilePicture by remember { mutableStateOf<BufferedImage?>(null) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(LightBlue)
            .padding(8.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(2.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                isError = !isValidEmail(email) && email.isNotEmpty()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                isError = confirmPassword != password && confirmPassword.isNotEmpty()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Gray, CircleShape)
                    .clickable {
                        val fileDialog = FileDialog(Frame(), "Select Profile Picture", FileDialog.LOAD)
                        fileDialog.isVisible = true
                        fileDialog.files.firstOrNull()?.let { file ->
                            profilePicture = ImageIO.read(file)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                profilePicture?.let { image ->
                    Image(
                        bitmap = image.toComposeImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp)
                    )
                } ?: run {
                    Icon(
                        painter = painterResource("icons/box.png"),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (isValidEmail(email) && password == confirmPassword) {
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                DatabaseUtil.saveUserData(username, email, password)
                            }
                            message = if (result.status == HttpStatusCode.Created) {
                                onSuccess()
                                "User data saved to MongoDB"
                            } else {
                                "Failed to save user data"
                            }
                        }
                    } else {
                        message = "Please make sure your email is valid and passwords match."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue, contentColor = Color.White)
            ) {
                Text("Save")
            }
            if (message.isNotEmpty()) {
                Text(text = message, color = if (message == "User data saved to MongoDB") Color.Green else Color.Red)
            }
        }
    }
}

fun isValidEmail(email: String): Boolean {
    val emailPattern = Pattern.compile(
        "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    )
    return emailPattern.matcher(email).matches()
}