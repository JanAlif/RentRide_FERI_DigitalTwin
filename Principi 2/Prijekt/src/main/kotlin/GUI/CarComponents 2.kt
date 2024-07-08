package GUI

import Util.DatabaseUtil
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bson.Document
import javax.imageio.ImageIO



@Composable
fun CarComponent(modifier: Modifier = Modifier, cars: ModelResponse?, onCarClick: (Trim?) -> Unit) {
    val carList = cars?.Trims ?: emptyList()
    var selectedOption by remember { mutableStateOf(1) }
    val coroutineScope = rememberCoroutineScope()
    var fetchedCars by remember { mutableStateOf<List<Trim>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                fetchedCars = withContext(Dispatchers.IO) { DatabaseUtil.getAllCars() }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

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
                Button(
                    onClick = { selectedOption = 1 },
                    colors = ButtonDefaults.buttonColors(backgroundColor = if (selectedOption == 1) DarkerBlue else LightBlue)
                ) {
                    Text("Data Base")
                }
                Button(
                    onClick = { selectedOption = 2 },
                    colors = ButtonDefaults.buttonColors(backgroundColor = if (selectedOption == 2) DarkerBlue else LightBlue)
                ) {
                    Text("API")
                }
            }
            if(carList.isEmpty() && selectedOption == 2){
                Text("Go to 'Add car' to display cars from API")
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (selectedOption == 2){
                    items(carList) { car ->
                        CarCard(
                            car = car,
                            view = selectedOption,
                            onClick = { onCarClick(car) },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                else{
                    items(fetchedCars.size) { carIndex ->
                        println("car")
                        CarCard(
                            car = fetchedCars[carIndex],
                            view = selectedOption,
                            onClick = { onCarClick(fetchedCars[carIndex]) },
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                        }
                    }

                }
            }
        }

@Composable
fun CarCard(car: Trim, view:Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var isAdded by remember { mutableStateOf(car.addedToDb) }
    val scope = rememberCoroutineScope()
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
                text = car.model_make_id + ", " + car.model_name,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = car.model_year,
                fontSize = 16.sp,
                color = Color.Black
            )
            if (view == 1){
                Text(
                    text = car.kilometers.toString()+" km",
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            else{
                Button(onClick={
                    scope.launch {
                        DatabaseUtil.saveCarData(car)
                    }
                    car.addedToDb = true
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
}

