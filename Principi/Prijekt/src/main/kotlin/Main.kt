import com.google.gson.GsonBuilder
import it.skrape.core.document
import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.matchers.toBe
import it.skrape.matchers.toBePresent
import it.skrape.matchers.toBePresentTimes
import it.skrape.matchers.toContain
import it.skrape.selects.and
import it.skrape.selects.html5.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import io.github.serpro69.kfaker.Faker
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

data class Accident(
    val title: String,
    val description: String,
)
fun extractData(input: String): List<Accident> {
    val regex = Regex("""<strong>(.*?)<\/strong>\s+\( <small>(.*?)<\/small> \)\s*<br>\s*(.*?)\s*<br>""")
    val matches = regex.findAll(input)
    val dataList = matches.map { matchResult ->
        val titlePart1 = matchResult.groupValues[1].trim()
        val titlePart2 = matchResult.groupValues[2].trim()
        val description = matchResult.groupValues[3].trim()

        Accident(
            title = "$titlePart1 ($titlePart2)",
            description = description
        )
    }.toList()

    return dataList
}
fun getDocumentByUrl(urlToScrape: String): List<Accident> {
    val extracted = skrape(BrowserFetcher) {
        request {
            url = "https://www.rtvslo.si/stanje-na-cestah"
        }

        response {
            htmlDocument(this.responseBody) {
                body {
                    findFirst{
                        toBePresent
                        div {
                            withId = "main-container"
                            findFirst{
                                toBePresent
                                div {
                                    withClass = "container"
                                    findLast {
                                        toBePresent
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return extractData(extracted.toString())
}


fun main() {
    val faker = Faker()
    println(faker.name.firstName())
    val test = getDocumentByUrl("https://www.rtvslo.si/stanje-na-cestah")
    println(test)
    /*val someHtml = """
    <body>
        <a-custom-tag>foo</a-custom-tag>
        <a-custom-tag class="some-style">bar</a-custom-tag>
    </body>
"""

    val neke = htmlDocument(someHtml) {
        "a-custom-tag" {
            findAll{
                toBePresent
            }
        }
    }
    println(neke)*/
}
