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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import org.bson.Document
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/*
@Composable
fun UsersComponent(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(LightBlue)
            .padding(8.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(2.dp, LightGray)
    ) {
        Text("This is the Users View", modifier = Modifier.padding(8.dp))
    }
}*/

@Composable
fun UsersComponent(modifier: Modifier = Modifier,onUserClick: (Document) -> Unit) {
    val users = remember { mutableStateListOf<Document>() }

    LaunchedEffect(Unit) {
        users.addAll(DatabaseUtil.fetchUsers())
    }
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(LightBlue)
            .padding(8.dp),
        shape = RoundedCornerShape(4.dp),
    ) {
        val columns = 3 // Set the number of columns
        val rows = (users.size + columns - 1) / columns

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(rows) { rowIndex ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (columnIndex in 0 until columns) {
                        val userIndex = rowIndex * columns + columnIndex
                        if (userIndex < users.size) {
                            UserCard(
                                user = users[userIndex],
                                onClick = { onUserClick(users[userIndex]) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(user: Document, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val defaultImageUrl = "https://storage.googleapis.com/rentride-1df1d.appspot.com/1716825620081.jpg"
    val profilePicUrl = user.getString("profilepic").ifEmpty { defaultImageUrl }

    Card(
        modifier = modifier
            .padding(8.dp)
            .clickable { onClick() },
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
                text = "${user.getString("username")}",
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}
