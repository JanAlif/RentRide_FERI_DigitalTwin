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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.matchers.toBePresent
import it.skrape.selects.html5.body
import it.skrape.selects.html5.div
import org.bson.Document
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.*
import javax.imageio.ImageIO


@Composable
fun TrafficAccidentComponent(modifier: Modifier = Modifier, savedAccident: List<Accident>?, onScrapeClick: (List<Accident>) -> Unit) {
    var accidents = remember { mutableStateListOf<Accident>() }
    println("neke")
    LaunchedEffect(Unit) {
        accidents.removeAll(accidents)
        if (savedAccident != null) {
            accidents.addAll(savedAccident)
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
                onClick = {
                    accidents.addAll(getAccidents())
                    onScrapeClick(accidents)
                          },
                colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue)
            ) {
                Text("Scrape Accidens")
            }
            if(accidents.isNotEmpty()){
                Button(
                    onClick = {
                        accidents.forEach{accident ->
                            DatabaseUtil.saveAccidentData(accident)
                            accident.addedToDb = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue)
                ) {
                    Text("Add all")
                }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(accidents) { accident ->
                TrafficAccidentCard(
                    accident = accident,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        }
    }
}

@Composable
fun TrafficAccidentCard(accident: Accident, modifier: Modifier = Modifier) {
    var isAdded by remember { mutableStateOf(accident.addedToDb) }
    Card(
        modifier = modifier,
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
                text = accident.title+" "+accident.time,
                fontSize = 16.sp,
                color = Color.Black
            )
            Button(onClick={
                DatabaseUtil.saveAccidentData(accident)
                accident.addedToDb = true
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
data class Accident(
    val title: String,
    val time: LocalDateTime,
    val description: String,
    var addedToDb:Boolean = false
)
fun extractData(input: String): List<Accident> {
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy 'ob' HH:mm", Locale.forLanguageTag("sl"))
    val regex = Regex("""<strong>(.*?)<\/strong>\s+\( <small>(.*?)<\/small> \)\s*<br>\s*(.*?)\s*<br>""")
    val matches = regex.findAll(input)
    val dataList = matches.map { matchResult ->
        val titlePart1 = matchResult.groupValues[1].trim()
        val titlePart2 = matchResult.groupValues[2].trim()
        val description = matchResult.groupValues[3].trim()

        Accident(
            title = "$titlePart1",
            time = LocalDateTime.parse(titlePart2, formatter),
            description = description
        )
    }.toList()

    return dataList
}
fun getAccidents(): List<Accident> {
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