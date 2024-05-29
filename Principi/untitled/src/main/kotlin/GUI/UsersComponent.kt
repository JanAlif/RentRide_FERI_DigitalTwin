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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import org.bson.Document
import javax.imageio.ImageIO




/*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.skija.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream


fun loadNetworkImage(link: String): ImageBitmap = runBlocking {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }

    val byteArray = client.get<ByteArray>(link)
    val inputStream = byteArray.inputStream()
    val bufferedImage = ImageIO.read(inputStream)

    val stream = ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", stream)
    val encodedByteArray = stream.toByteArray()

    client.close()

    Image.makeFromEncoded(encodedByteArray).asImageBitmap()
}

    val byteArray = client.get<ByteArray>(link)
    val inputStream = byteArray.inputStream()
    val bufferedImage = ImageIO.read(inputStream)

    val stream = ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", stream)
    val encodedByteArray = stream.toByteArray()

    client.close()

    Image.makeFromEncoded(encodedByteArray).asImageBitmap()
}*/
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
fun UsersComponent(modifier: Modifier = Modifier, onUserClick: (Document) -> Unit) {
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
        }
    }
}