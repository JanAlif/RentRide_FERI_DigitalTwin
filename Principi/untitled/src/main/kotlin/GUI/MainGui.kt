package GUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import org.bson.Document

fun main() = singleWindowApplication(
    title = "My GUI",
) {
    MaterialTheme {
        MainScreen()
    }
}

@Composable
fun MainScreen() {
    var sidebarWidth by remember { mutableStateOf(200.dp) }
    var isSidebarCollapsed by remember { mutableStateOf(false) }
    var currentView by remember { mutableStateOf("MainContent") }
    var user by remember { mutableStateOf<Document?>(null) }
    var cars by remember{ mutableStateOf<ModelResponse?>(null)}
    var accidents by remember { mutableStateOf<List<Accident>?>(null)}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFF003366)) // Assuming DeepBlue is a color
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .width(if (isSidebarCollapsed) 60.dp else sidebarWidth)
                    .fillMaxHeight()
                    .background(Color.Cyan)
            ) {
                SidebarComponent(
                    isCollapsed = isSidebarCollapsed,
                    onCollapse = { isSidebarCollapsed = !isSidebarCollapsed },
                    onMenuItemClick = { view -> currentView = view }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            when (currentView) {
                "AddUser" -> AddUserComponent(modifier = Modifier.weight(1f),onSuccess = { currentView = "Users" })
                "AddCar" -> AddCarComponent(modifier = Modifier.weight(1f), onSubmit = {carList ->
                    println(carList)
                    cars = carList
                    currentView = "Cars"
                })
                //"AddCar" -> AddCarComponent()
                "Users" -> UsersComponent(modifier = Modifier.weight(1f), onUserClick = {
                    user = it
                    currentView = "UpdateUser"
                })
                "Cars" -> CarComponent(modifier = Modifier.weight(1f), cars, onCarClick = {car -> println(car) })
                "UpdateUser" -> UpdateUserComponent(modifier = Modifier.weight(1f),onSuccess = { currentView = "Users" },user)
                "Generate" -> GenerateComponent(modifier = Modifier.weight(1f))
                "Scrape" -> TrafficAccidentComponent(modifier = Modifier.weight(1f), accidents, onScrapeClick = {
                    println(it)
                    accidents = it
                })
                else -> MainContentComponent(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FooterComponent()
    }
}