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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import org.bson.Document
import javax.imageio.ImageIO


@Composable
fun UsersComponent(modifier: Modifier = Modifier, onUserClick: (Document) -> Unit) {
    val users = remember { mutableStateListOf<Document>() }

    LaunchedEffect(Unit) {
        users.addAll(DatabaseUtil.getAllUsers())
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(LightBlue)
            .padding(8.dp),
        shape = RoundedCornerShape(4.dp),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(users.size) { userIndex ->
                UserCard(
                    user = users[userIndex],
                    onClick = { onUserClick(users[userIndex]) },
                    modifier = Modifier.padding(8.dp)
                )
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
                text = user.getString("username"),
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.getString("email"),
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}