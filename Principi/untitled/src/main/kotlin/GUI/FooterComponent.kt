package GUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FooterComponent() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightBlue)
            .padding(8.dp),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, LightGray)
    ) {
        Text("RentRide", modifier = Modifier.padding(8.dp))
    }
}