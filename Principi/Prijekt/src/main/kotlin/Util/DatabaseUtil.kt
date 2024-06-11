package Util
import GUI.Accident
import GUI.Trim
import com.gargoylesoftware.htmlunit.javascript.host.geo.Coordinates
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Acl
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.StorageClient
import com.google.gson.*
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import kotlinx.serialization.encodeToString
import org.bson.Document
import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.net.http.HttpClient
import java.time.Instant
import javax.imageio.ImageIO
import kotlin.random.Random
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*


object DatabaseUtil {
    val mongoClient: MongoClient = MongoClients.create("mongodb+srv://admin:PK3TipQ3Mj9Ji6N@projektnipraktikum.epnifwl.mongodb.net/RentRideApp")

    // Get database and collectionUsers
    val database = mongoClient.getDatabase("RentRideApp")
    val collectionUsers: MongoCollection<Document> = database.getCollection("users")
    val collectionCars: MongoCollection<Document> = database.getCollection("cars")
    val collectionAccidents: MongoCollection<Document> = database.getCollection("trafficAccidents")
    init {
        initializeFirebase()
    }
    fun initializeFirebase() {
        val serviceAccount = FileInputStream("src/main/resources/firebaseKey.json")

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setStorageBucket("rentride-1df1d.appspot.com") // Replace with your Firebase Storage bucket URL
            .build()

        FirebaseApp.initializeApp(options)
    }
    private fun uploadImageToFirebase(image: BufferedImage): String {
        val bucket = StorageClient.getInstance().bucket()
        val imageName = "${System.currentTimeMillis()}.jpeg"
        val blob = bucket.create(
            imageName,
            ByteArrayInputStream(convertBufferedImageToByteArray(image)),
            "image/jpeg"
        )
        blob.updateAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))
        val mediaLink = "https://storage.googleapis.com/${bucket.name}/$imageName"
        return mediaLink
    }

    private fun convertBufferedImageToByteArray(image: BufferedImage): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(image, "jpeg", byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }



    /*
     fun updateUserData(userId: String, username: String="", email: String?, password: String="", profilePicture: BufferedImage?=null): String {
        val objectId: ObjectId = try {
            ObjectId(userId)
        } catch (e: IllegalArgumentException) {
            return "Invalid user ID format."
        }
        val userDocument = collectionUsers.find(Filters.eq("_id", objectId)).first() ?: return "User not found."

        username?.let {
            val usernameFilter = Filters.eq("username", it)
            val excludeIdFilter = Filters.ne("_id", objectId)
            val existingUser = collectionUsers.find(Filters.and(usernameFilter, excludeIdFilter)).first()
            if (existingUser != null && existingUser["_id"] != userId) {
                return "Username is already in use."
            }
            userDocument["username"] = it
        }

        email?.let {
            val usernameFilter = Filters.eq("email", it)
            val excludeIdFilter = Filters.ne("_id", objectId)
            val existingEmail = collectionUsers.find(Filters.and(usernameFilter, excludeIdFilter)).first()
            if (existingEmail != null && existingEmail["_id"] != userId) {
                return "Email is already in use."
            }
            userDocument["email"] = it
        }

        password.let {
            /*
            val salt = BCrypt.gensalt(10)
            val hashedPassword = BCrypt.hashpw(it, salt)
            userDocument["password"] = hashedPassword*/
            userDocument["password"] = it
        }

        profilePicture?.let {
            val profilePicUrl = uploadImageToFirebase(it)
            userDocument["profilepic"] = profilePicUrl
        }

        userDocument["updatedAt"] = Instant.now()

        collectionUsers.replaceOne(Filters.eq("_id", objectId), userDocument)
        return "User data updated successfully."
    }
    fun fetchCars(): List<Document> {
        val save =collectionCars.find().toList()
        println(save)
        return save
    }
    fun fetchUsers(): List<Document> {
        return collectionUsers.find().toList()
    }
    fun saveUserData(username: String, email: String, password: String, profilePicture: BufferedImage? = null): String {
        // Check if username or email already exists
        val existingUser = collectionUsers.find(Filters.eq("username", username)).first()
        val existingEmail = collectionUsers.find(Filters.eq("email", email)).first()
        if (existingUser != null) {
            return "Username is already in use."
        }
        else if (existingEmail != null){
            return "Email is already in use."
        }

        val userDocument = Document()
        val salt = BCrypt.gensalt(10)
        val hashedPassword = BCrypt.hashpw(password, salt)
        val timestamp = Instant.now()
        val profilePicUrl = profilePicture?.let { uploadImageToFirebase(it) } ?: "https://storage.googleapis.com/rentride-1df1d.appspot.com/1716825620081.jpg"

        userDocument.append("username", username)
            .append("email", email)
            .append("password", hashedPassword)
            .append("profilepic", profilePicUrl)
            .append("previousRides", emptyList<String>())
            .append("createdAt", timestamp)
            .append("updatedAt", timestamp)
            .append("__v", 0)

        collectionUsers.insertOne(userDocument)
        return "User data saved to MongoDB"
    }*/
    data class Coordinates(val type: String, val coordinates: List<Double>)
    fun saveCarData(car: Trim): Boolean {

        val userDocument = Document()
        val timestamp = Instant.now()

        val location = Document("location", Document()
            .append("type", "Point")
            .append("coordinates", listOf<Double>(15.64586,46.55467))
        )

        userDocument.append("brand", car.model_make_id)
            .append("model", car.model_name)
            .append("year", car.model_year)
            .append("totalKm", Random.nextInt(10000, 100001))
            .append("isElectric", if(car.model_engine_fuel == "Electric")true else false)
            .append("location", Document()
                .append("type", "Point")
                .append("coordinates", listOf(15.267710000000001,46.239740000000005)))
            .append("inUse", false)
            .append("carpic", "public/photos/defaultAvatar.jpg")
            .append("previousRides", emptyList<String>())
            .append("createdAt", timestamp)
            .append("updatedAt", timestamp)
            .append("__v", 0)

        collectionCars.insertOne(userDocument)
        return true
    }
    fun saveAccidentData(accident: Accident): Boolean {

        val accidentDocument = Document()
        val timestamp = Instant.now()

        accidentDocument.append("title", accident.title)
            .append("time", accident.time)
            .append("description", accident.description)
            .append("__v", 0)

        collectionAccidents.insertOne(accidentDocument)
        return true
    }
    suspend fun saveUserData(username: String, email: String, password: String): HttpResponse {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                gson()
            }
        }

        val userData = JsonObject().apply {
            addProperty("username", username)
            addProperty("email", email)
            addProperty("password", password)
        }

        val response: HttpResponse = client.post("http://localhost:4000/api/users/register") {
            contentType(ContentType.Application.Json)
            setBody(userData)
        }

        client.close()
        return response
    }
    suspend fun saveCar(car: Trim): Boolean {

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                gson()
            }
        }
        val jsonArray = JsonArray()


        jsonArray.add(JsonPrimitive(123))
        jsonArray.add(JsonPrimitive(456))
        val carData = JsonObject().apply {
            addProperty("brand", car.model_make_id)
            addProperty("model", car.model_name)
            addProperty("year", car.model_year)
            addProperty("totalKm", Random.nextInt(10000, 100001))
            addProperty("isElectric", if(car.model_engine_fuel == "Electric")true else false)
            add("location", JsonArray().apply {
                add(12.43444)
                add(43.12333)
            })
        }


        val response: HttpResponse = client.post("http://localhost:4000/api/cars") {
            contentType(ContentType.Application.Json)
            setBody(carData)
        }
        client.close()

        if (response.status == HttpStatusCode.Created) {
            return true
        } else {
            throw Exception("Failed to add car")
        }
    }
    suspend fun getAllCars(): List<Trim> {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response: HttpResponse = client.get("http://localhost:4000/api/cars")
        client.close()
        if (response.status == HttpStatusCode.OK) {
            val jsonArray = Gson().fromJson(response.bodyAsText(), JsonArray::class.java)
            var test = jsonArray.map { jsonElement ->
                val jsonObject = jsonElement.asJsonObject
                Trim(
                    model_make_id = jsonObject.get("brand").asString,
                    model_name = jsonObject.get("model").asString,
                    model_year = jsonObject.get("year").asString,
                    kilometers = jsonObject.get("totalKm").asInt
                )
            }
            return test
        } else {
            throw Exception("Failed to fetch cars")
        }
    }
    suspend fun getAllUsers(): List<Document> {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response: HttpResponse = client.get("http://localhost:4000/api/users")
        client.close()

        if (response.status == HttpStatusCode.OK) {
            val jsonArray: JsonArray = Gson().fromJson(response.bodyAsText(), JsonArray::class.java)
            return jsonArray.map { jsonElement: JsonElement ->
                Document.parse(jsonElement.toString())
            }
        } else {
            throw Exception("Failed to fetch users")
        }
    }

    suspend fun updateUser(userId: String, username: String="", email: String?, password: String="", profilePicture: BufferedImage?=null): Boolean {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                gson()
            }
        }

        val userUpdateData = JsonObject().apply {
            username?.let { addProperty("username", it) }
            email?.let { addProperty("email", it) }
        }

        val response: HttpResponse = client.put("http://localhost:4000/api/users/$userId") {
            contentType(ContentType.Application.Json)
            setBody(userUpdateData)
        }
        client.close()

        if (response.status == HttpStatusCode.OK) {
            return true
        } else {
            throw Exception("Failed to update user")
        }
    }
}