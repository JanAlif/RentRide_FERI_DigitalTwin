package Util
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.StorageClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.time.Instant
import javax.imageio.ImageIO

object DatabaseUtil {
    val mongoClient: MongoClient = MongoClients.create("mongodb+srv://admin:PK3TipQ3Mj9Ji6N@projektnipraktikum.epnifwl.mongodb.net/RentRideApp")

    // Get database and collection
    val database = mongoClient.getDatabase("RentRideApp")
    val collection: MongoCollection<Document> = database.getCollection("users")

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
        val imageName = "${System.currentTimeMillis()}.png"
        val blob = bucket.create(
            imageName,
            ByteArrayInputStream(convertBufferedImageToByteArray(image)),
            "image/png"
        )
        return blob.mediaLink
    }

    private fun convertBufferedImageToByteArray(image: BufferedImage): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
    fun saveUserData(username: String, email: String, password: String, profilePicture: BufferedImage?): String {
        // Check if username or email already exists
        val existingUser = collection.find(Filters.eq("username", username)).first()
        val existingEmail = collection.find(Filters.eq("email", email)).first()
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
        val profilePicUrl = profilePicture?.let { uploadImageToFirebase(it) } ?: ""

        userDocument.append("username", username)
            .append("email", email)
            .append("password", hashedPassword)
            .append("profilepic", profilePicUrl)
            .append("previousRides", emptyList<String>())
            .append("createdAt", timestamp)
            .append("updatedAt", timestamp)
            .append("__v", 0)

        collection.insertOne(userDocument)
        return "User data saved to MongoDB"
    }
    }