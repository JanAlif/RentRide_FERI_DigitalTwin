import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

fun main() {
    val apiKey = "AIzaSyBI9PE3yyOHh4J5UiJxaoR1u9WzDpWg0j8"
    val origin = "Seattle,WA"
    val destination = "San Francisco,CA"
    val url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$origin&destinations=$destination&key=$apiKey"

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
                println(responseBody)
            }
        }
    })
}
