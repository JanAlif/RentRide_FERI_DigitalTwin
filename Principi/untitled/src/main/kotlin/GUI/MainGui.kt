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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFF003366)) // Assuming DeepBlue is a color
    ) {
        HeaderComponent()
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
                "AddCar" -> AddCarComponent(modifier = Modifier.weight(1f))
                "Users" -> UsersComponent(modifier = Modifier.weight(1f))
                "Cars" -> CarComponent(modifier = Modifier.weight(1f))
                "Generate" -> GenerateComponent(modifier = Modifier.weight(1f))
                "Scrape" -> ScrapeComponent(modifier = Modifier.weight(1f))
                else -> MainContentComponent(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FooterComponent()
    }
}