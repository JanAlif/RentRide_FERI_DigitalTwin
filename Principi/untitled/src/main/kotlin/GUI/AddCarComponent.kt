
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

@Composable
@Preview
fun AddCarComponent(modifier: Modifier = Modifier,onSubmit: (ModelResponse?) -> Unit) {
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
                    if (it.matches(Regex("^\\d{0,4}$"))) minYear = it
                },
                label = { Text("Min Year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxYear,
                onValueChange = {
                    if (it.matches(Regex("^\\d{0,4}$"))) maxYear = it
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
                var carList: ModelResponse? = null
                val base_url = "https://www.carqueryapi.com/api/0.3/?callback=?&cmd=getTrims&keyword=$keyword"
                val params = StringBuilder(base_url)

                if (fuelType.isNotEmpty()) params.append("&fuel_type=$fuelType")
                minFuelEffic.let { params.append("&min_lkm_hwy=$it") }
                minTopSpeed.let { params.append("&min_top_speed=$it") }
                minYear.let { params.append("&min_year=$it") }
                maxFuelEffic.let { params.append("&max_lkm_hwy=$it") }
                maxTopSpeed.let { params.append("&max_top_speed=$it") }
                maxYear.let { params.append("&max_year=$it") }

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
                            if (cleanedJson != null) {
                                carList = Json.decodeFromString<ModelResponse>(cleanedJson)
                                onSubmit(
                                    carList
                                )
                            }
                        }
                    }
                })
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue, contentColor = Color.White)
        ) {
            Text("Submit")
        }
    }
    }
}

@Serializable
data class Trim(
    val model_id: String? = null,
    val model_make_id: String,
    val model_name: String,
    val model_trim: String?= null,
    val model_year: String,
    val model_body: String?= null,
    val model_engine_position: String?= null,
    val model_engine_cc: String?= null,
    val model_engine_cyl: String?= null,
    val model_engine_type: String?= null,
    val model_engine_valves_per_cyl: String?= null,
    val model_engine_power_ps: String?= null,
    val model_engine_power_rpm: String?= null,
    val model_engine_torque_nm: String?= null,
    val model_engine_torque_rpm: String?= null,
    val model_engine_bore_mm: String?= null,
    val model_engine_stroke_mm: String?= null,
    val model_engine_compression: String?= null,
    val model_engine_fuel: String?= null,
    val model_top_speed_kph: String?= null,
    val model_0_to_100_kph: String?= null,
    val model_drive: String?= null,
    val model_transmission_type: String?= null,
    val model_seats: String?= null,
    val model_doors: String?= null,
    val model_weight_kg: String?= null,
    val model_length_mm: String?= null,
    val model_width_mm: String?= null,
    val model_height_mm: String?= null,
    val model_wheelbase_mm: String?= null,
    val model_lkm_hwy: String?= null,
    val model_lkm_mixed: String?= null,
    val model_lkm_city: String?= null,
    val model_fuel_cap_l: String?= null,
    val model_sold_in_us: String?= null,
    val model_co2: String?= null,
    val model_make_display: String?= null,
    val make_display: String?= null,
    val make_country: String?= null,
    val kilometers: Int?= null,
    var addedToDb: Boolean = false
)
@Serializable
data class ModelResponse(
    val Trims: List<Trim>
)

