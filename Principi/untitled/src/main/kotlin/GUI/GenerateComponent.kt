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
import io.github.serpro69.kfaker.Faker
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
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedTextField(
                    value = amount.toString(),
                    onValueChange = {
                        if (it.matches(Regex("^\\d{0,3}$"))) amount = it
                    },
                    label = { Text("How many to generate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if(amount.toInt() > 0){
                            users.clear()
                            for (i in 1..amount.toInt()){
                                val generatedUser = Document()
                                generatedUser.append("username", faker.name.firstName())
                                    .append("email", faker.internet.email())
                                    .append("password", "a")
                                    .append("addedToDb", false)
                                users.add(generatedUser)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue, contentColor = Color.White)
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
                                DatabaseUtil.saveUserData(user.getString("username"), user.getString("email"),user.getString("password"))
                                user["addedToDb"] = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue)
                    ) {
                        Text("Scrape Accidens")
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
                DatabaseUtil.saveUserData(user.getString("username"), user.getString("email"),user.getString("password"))
                user["addedToDb"] = true
                isAdded = true
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