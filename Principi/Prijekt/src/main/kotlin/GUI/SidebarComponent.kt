package GUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SidebarComponent(isCollapsed: Boolean, onCollapse: () -> Unit, onMenuItemClick: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(LightBlue)
            .padding(8.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(2.dp, LightGray)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))
            SidebarMenuItem(icon = Icons.Default.PersonAdd, label = "Add user", isCollapsed = isCollapsed, onClick = { onMenuItemClick("AddUser") })
            SidebarMenuItem(icon = Icons.Default.DirectionsCar, label = "Add car", isCollapsed = isCollapsed, onClick = { onMenuItemClick("AddCar") })
            SidebarMenuItem(icon = Icons.Default.People, label = "Users", isCollapsed = isCollapsed, onClick = { onMenuItemClick("Users") })
            SidebarMenuItem(icon = Icons.Default.Garage, label = "Cars", isCollapsed = isCollapsed, onClick = { onMenuItemClick("Cars") })
            SidebarMenuItem(icon = Icons.Default.AddCircle, label = "Generate", isCollapsed = isCollapsed, onClick = { onMenuItemClick("Generate") })
            SidebarMenuItem(icon = Icons.Default.Language, label = "Scrape", isCollapsed = isCollapsed, onClick = { onMenuItemClick("Scrape") })

            Spacer(modifier = Modifier.weight(1f))
            CollapseExpandButton(isCollapsed = isCollapsed, onCollapse = onCollapse)
        }
    }
}


@Composable
fun CollapseExpandButton(isCollapsed: Boolean, onCollapse: () -> Unit) {
    Button(
        onClick = onCollapse,
        colors = ButtonDefaults.buttonColors(backgroundColor = Gray),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isCollapsed) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Expand")
        } else {
            Text("Collapse")
        }
    }
}
