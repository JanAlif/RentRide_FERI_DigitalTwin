package GUI

import Util.DatabaseUtil
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import org.bson.Document
import java.awt.FileDialog
import java.awt.Frame
import java.awt.image.BufferedImage
import java.util.regex.Pattern
import javax.imageio.ImageIO

@Composable
fun UpdateUserComponent(modifier: Modifier = Modifier, onSuccess: () -> Unit, user: Document?) {
    var username by remember { mutableStateOf(user?.getString("username") ?: "") }
    var email by remember { mutableStateOf(user?.getString("email") ?: "") }
    var profilePicture by remember { mutableStateOf<BufferedImage?>(null) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.getString("profilepic")?.let { url ->
            // Load profile picture from URL if available (pseudo code)
            profilePicture = loadImageFromUrl(url)
        }
    }

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
                    if (isValidEmail(email)) {
                        val result = DatabaseUtil.updateUserData(user?.get("_id").toString(), username, email, user?.get("password").toString(), profilePicture)
                        message = result
                    } else {
                        message = "Please make sure your email is valid."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue, contentColor = Color.White)
            ) {
                Text("Save")
            }
            if (message.isNotEmpty()) {
                if (message == "User data saved to MongoDB") {
                    onSuccess()
                } else {
                    Text(text = message, color = Color.Red)
                }
            }
        }
    }
}


fun loadImageFromUrl(url: String): BufferedImage? {
    // Pseudo code to load an image from URL
    return null // Replace with actual image loading code
}