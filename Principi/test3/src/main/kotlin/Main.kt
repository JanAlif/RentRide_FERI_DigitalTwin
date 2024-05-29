import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

//input je lahko napisan z imenom ("Paris,FR") ali z koordinatami ("48.8575,2.3514")
fun getDistanceAndTime(origin: String, destination: String) {
    val apiKey = "AIzaSyBI9PE3yyOHh4J5UiJxaoR1u9WzDpWg0j8"
    val url =
        "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$origin&destinations=$destination&key=$apiKey"

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
                println("____________________________________________")
                println("Trip: ")
                println(responseBody)
            }
        }
    })
}

//radious je definiran v metrih. Location prav tako lahko z imenom ali koordinatami

fun getGasStations(location: String, radious: Int) {
    val apiKey = "AIzaSyBI9PE3yyOHh4J5UiJxaoR1u9WzDpWg0j8"
    val url =
        "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$location&radius=$radious&type=gas_station&key=$apiKey"
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
                println("____________________________________________")
                println("Gas Stations: ")
                println(responseBody)
            }
        }
    })
}

fun getCar(
    keyword: String,
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


fun main() {
    val paris = "Paris,FR"
    val maribor = "46.554649,15.645881"

    getDistanceAndTime(paris,maribor)

    getGasStations(maribor, 1000)

    getCar("audi Q7", min_year = 2022)
}
