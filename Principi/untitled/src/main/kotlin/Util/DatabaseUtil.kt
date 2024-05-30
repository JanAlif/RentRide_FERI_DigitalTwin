package Util
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Acl
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.StorageClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.Document
import org.bson.types.ObjectId
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
    fun fetchUsers(): List<Document> {
        return collection.find().toList()
    }
    fun updateUserData(userId: String, username: String="", email: String?, password: String="", profilePicture: BufferedImage?=null): String {
        val objectId: ObjectId = try {
            ObjectId(userId)
        } catch (e: IllegalArgumentException) {
            return "Invalid user ID format."
        }
        val userDocument = collection.find(Filters.eq("_id", objectId)).first() ?: return "User not found."

        username?.let {
            val usernameFilter = Filters.eq("username", it)
            val excludeIdFilter = Filters.ne("_id", objectId)
            val existingUser = collection.find(Filters.and(usernameFilter, excludeIdFilter)).first()
            if (existingUser != null && existingUser["_id"] != userId) {
                return "Username is already in use."
            }
            userDocument["username"] = it
        }

        email?.let {
            val usernameFilter = Filters.eq("email", it)
            val excludeIdFilter = Filters.ne("_id", objectId)
            val existingEmail = collection.find(Filters.and(usernameFilter, excludeIdFilter)).first()
            if (existingEmail != null && existingEmail["_id"] != userId) {
                return "Email is already in use."
            }
            userDocument["email"] = it
        }

        password?.let {
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

        collection.replaceOne(Filters.eq("_id", objectId), userDocument)
        return "User data updated successfully."
    }
}