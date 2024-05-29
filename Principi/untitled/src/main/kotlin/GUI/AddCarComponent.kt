
package GUI

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

@Composable
@Preview
fun AddCarComponent(modifier: Modifier = Modifier,onSubmit: (String, String, Double?, Int?, Int?, Double?, Int?, Int?) -> Unit) {
    var keyword by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("") }
    var minFuelEffic by remember { mutableStateOf("") }
    var maxFuelEffic by remember { mutableStateOf("") }
    var minTopSpeed by remember { mutableStateOf("") }
    var maxTopSpeed by remember { mutableStateOf("") }
    var minYear by remember { mutableStateOf("") }
    var maxYear by remember { mutableStateOf("") }

    var expandedFuel by remember { mutableStateOf(false) }

    val fuelTypes = listOf("Petrol", "Diesel", "Electric", "Hybrid")
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(LightBlue)
            .padding(8.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(2.dp, LightGray)
    ) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("Keyword") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = minFuelEffic,
                onValueChange = {
                    if (it.matches(Regex("^\\d{0,2}(\\.\\d{0,2})?$"))) minFuelEffic = it
                },
                label = { Text("Min Fuel Efficiency") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxFuelEffic,
                onValueChange = {
                    if (it.matches(Regex("^\\d{0,2}(\\.\\d{0,2})?$"))) maxFuelEffic = it
                },
                label = { Text("Max Fuel Efficiency") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = minTopSpeed,
                onValueChange = {
                    if (it.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?$"))) minTopSpeed = it
                },
                label = { Text("Min Top Speed") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxTopSpeed,
                onValueChange = {
                    if (it.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?$"))) maxTopSpeed = it
                },
                label = { Text("Max Top Speed") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = minYear,
                onValueChange = {
                    if (it.matches(Regex("^\\d{0,3}$"))) minTopSpeed = it
                },
                label = { Text("Min Year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxYear,
                onValueChange = {
                    if (it.matches(Regex("^\\d{0,3}$"))) maxTopSpeed = it
                },
                label = { Text("Max Year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
        }
/*
        DropdownMenu(
            options = fuelTypes,
            selectedOption = fuelType,
            onOptionSelected = { fuelType = it },
            label = "Fuel Type"
        )
*/
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column {
                Button(onClick = { expandedFuel = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue, contentColor = Color.White)) {
                    Text(if (fuelType.isEmpty()) "Select fuel type" else fuelType)
                }
                DropdownMenu(
                    expanded = expandedFuel,
                    onDismissRequest = { expandedFuel = false },
                    modifier = Modifier.width(200.dp)
                ) {
                    fuelTypes.forEach { label ->
                        DropdownMenuItem(onClick = {
                            fuelType = label
                            expandedFuel = false
                        }) {
                            Text(text = label)
                        }
                    }
                }
            }
        }
        Button(
            onClick = {
                onSubmit(
                    keyword,
                    fuelType,
                    minFuelEffic.toDoubleOrNull(),
                    minTopSpeed.toIntOrNull(),
                    minYear.toIntOrNull(),
                    maxFuelEffic.toDoubleOrNull(),
                    maxTopSpeed.toIntOrNull(),
                    maxYear.toIntOrNull()
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue, contentColor = Color.White)
        ) {
            Text("Submit")
        }
    }
    }
}

@Composable
@Preview
fun test() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }
    val options = listOf("Coffee", "Tea", "Juice", "Water", "Soda")

    Column {
        Button(onClick = { expanded = true }) {
            Text(if (selectedOption.isEmpty()) "Select a drink" else selectedOption)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = androidx.compose.ui.Modifier.width(200.dp)
        ) {
            options.forEach { label ->
                DropdownMenuItem(onClick = {
                    selectedOption = label
                    expanded = false
                }) {
                    Text(text = label)
                }
            }
        }
    }
}
fun getCar(
    keyword: String = "",
    fuel_type: String = "",
    min_fuel_effic: Double? = null,
    min_top_speed: Int? = null,
    min_year: Int? = null,
    max_fuel_effic: Double? = null,
    max_top_speed: Int? = null,
    max_year: Int? = null
) {
    val base_url = "https://www.carqueryapi.com/api/0.3/?callback=?&cmd=getTrims&keyword=$keyword"
    val params = StringBuilder(base_url)

    if (fuel_type.isNotEmpty()) params.append("&fuel_type=$fuel_type")
    if (fuel_type.isNotEmpty()) params.append("&fuel_type=$fuel_type")
    min_fuel_effic?.let { params.append("&min_lkm_hwy=$it") }
    min_top_speed?.let { params.append("&min_top_speed=$it") }
    min_year?.let { params.append("&min_year=$it") }
    max_fuel_effic?.let { params.append("&max_lkm_hwy=$it") }
    max_top_speed?.let { params.append("&max_top_speed=$it") }
    max_year?.let { params.append("&max_year=$it") }

    val url = params.toString()

    val client = OkHttpClient()

    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: okhttp3.Call, response: Response) {
            response.use {
                if (!it.isSuccessful) throw IOException("Unexpected code $response")

                val responseBody = it.body?.string()
                val cleanedJson = responseBody?.substringAfter("?(")?.substringBefore(");")
                val gson = GsonBuilder().setPrettyPrinting().create()
                val jsonElement = gson.fromJson(cleanedJson, Any::class.java)
                val prettyJson = gson.toJson(jsonElement)
                println("____________________________________________")
                println("Cars: ")
                println(prettyJson)
            }
        }
    })
}

