package GUI

import Util.DatabaseUtil
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.WindowPosition.PlatformDefault.y
import io.github.serpro69.kfaker.Faker
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.bson.Document
import java.io.IOException
import javax.imageio.ImageIO


@Composable
fun GenerateComponent(modifier: Modifier = Modifier) {
    val users = remember { mutableStateListOf<Document>() }
    var amount by remember { mutableStateOf("") }
    val faker = Faker()
    val scope = rememberCoroutineScope()
/*
    LaunchedEffect(Unit) {
        users.addAll(DatabaseUtil.fetchUsers())
    }
*/
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(LightBlue)
            .padding(8.dp),
        shape = RoundedCornerShape(4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.matches(Regex("^\\d{0,3}$"))) amount = it
                    },
                    label = { Text("How many to generate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Button(
                    onClick = {
                        if (amount.isNotEmpty() && amount.toInt() > 0) {
                            users.clear()
                            for (i in 1..amount.toInt()) {
                                val generatedUser = Document()
                                generatedUser.append("username", faker.name.firstName())
                                    .append("email", faker.internet.email())
                                    .append("password", "a")
                                    .append("addedToDb", false)
                                users.add(generatedUser)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue),
                    modifier = Modifier
                        .height(56.dp)
                        .offset(y = 4.dp)
                ) {
                    Text("Generate")
                }
            }
            if(users.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            users.forEach{user ->
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        DatabaseUtil.saveUserData(user.getString("username"), user.getString("email"),user.getString("password"))
                                    }
                                    if (result.status == HttpStatusCode.Created) {
                                        user["addedToDb"] = true
                                    } else {
                                        println("Failed to save user data")
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue)
                    ) {
                        Text("Add all")
                    }
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(users.size) { userIndex ->
                    UserGenerateCard(
                        user = users[userIndex],
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserGenerateCard(user: Document, modifier: Modifier = Modifier) {
    var isAdded by remember { mutableStateOf(user.getBoolean("addedToDb")) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.getString("username"),
                fontSize = 16.sp,
                color = Color.Black
            )
            Button(onClick={
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        DatabaseUtil.saveUserData(user.getString("username"), user.getString("email"),user.getString("password"))
                    }
                    if (result.status == HttpStatusCode.Created) {
                        user["addedToDb"] = true
                        isAdded = true
                    } else {
                        println("Failed to save user data")
                    }
                }

            },
                enabled = !isAdded,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (!isAdded) LightBlue else Color.Gray,
                    contentColor = if (!isAdded) Color.White else Color.LightGray
                )
            ){
                Text("add")
            }
        }
    }
}