package si.um.feri.cestar.Utils;

import com.badlogic.gdx.Gdx;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.UpdateResult;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MongoDB {

    private MongoClient mongoClient;
    private MongoDatabase database;

    @SuppressWarnings("AuthLeak")
    public void connectToMongoDB() {
        try {
            // Create a MongoClient, daj v constane(gitignore)
            mongoClient = MongoClients.create("mongodb+srv://admin:PK3TipQ3Mj9Ji6N@projektnipraktikum.epnifwl.mongodb.net/RentRideApp"); // Replace with your MongoDB URI

            // Access the database
            database = mongoClient.getDatabase("RentRideApp");

            System.out.println("Connected to MongoDB!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPointToDatabase(LocationInfo location, String address) {
        try {
            MongoCollection<Document> collection = database.getCollection("chargepoints");

            // Create the nested location object
            Document locationObject = new Document()
                .append("type", "Point")
                .append("coordinates", Arrays.asList(location.getLongitude(), location.getLatitude())); // GeoJSON requires [lng, lat]

            // Create the main document
            Document newPoint = new Document()
                .append("locationName", location.getLocationName())
                .append("address", address)
                .append("location", locationObject)
                .append("connectors", location.getConnectors())
                .append("connectorsAvailable", location.getConnectorsAvailable())
                .append("createdAt", new Date()) // Automatically add creation time
                .append("updatedAt", new Date()); // Automatically add update time

            // Insert the new point into the database
            collection.insertOne(newPoint);
            Gdx.app.log("MongoDB", "Point added to database: " + newPoint);
        } catch (Exception e) {
            e.printStackTrace();
            Gdx.app.log("MongoDB", "Failed to add point: " + e.getMessage());
        }
    }




    public List<LocationInfo> fetchPointsFromDatabase() {
        List<LocationInfo> locations = new ArrayList<>();
        try {
            // Log connection attempt to MongoDB
            Gdx.app.log("MongoDB", "Attempting to access the 'cars' collection");

            // Access the "cars" collection
            MongoCollection<Document> collection = database.getCollection("chargepoints");

            // Check if collection exists and if there are any documents
            long count = collection.countDocuments();
            Gdx.app.log("MongoDB", "Number of documents in 'cars' collection: " + count);

            // Iterate through the documents in the collection
            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();

                // Log document contents (to ensure fields exist)
                Gdx.app.log("MongoDB", "Document fetched: " + doc.toJson());

                // Retrieve the 'locationName' and 'location' fields
                String locationName = doc.getString("locationName");
                Document location = (Document) doc.get("location");

                if (location != null && location.containsKey("coordinates")) {
                    // Extract coordinates as a list of Doubles
                    List<Double> coordinates = (List<Double>) location.get("coordinates");

                    // Check if coordinates exist and are of the expected size (2)
                    if (coordinates != null && coordinates.size() == 2) {
                        Number lngValue = coordinates.get(0); // Longitude
                        Number latValue = coordinates.get(1); // Latitude

                        double lng = lngValue.doubleValue();
                        double lat = latValue.doubleValue(); // Latitude

                        // Extract 'connectors' and 'connectorsAvailable' fields
                        int connectors = doc.getInteger("connectors", 0); // Default to 0 if not present
                        int connectorsAvailable = doc.getInteger("connectorsAvailable", 0); // Default to 0 if not present
                        String address = doc.getString("address");
                        // Create a LocationInfo object
                        LocationInfo locationInfo = new LocationInfo(locationName, lat, lng, connectors, connectorsAvailable,address);

                        // Log the LocationInfo object for debugging
                        Gdx.app.log("MongoDB", "LocationInfo fetched: " + locationInfo);

                        // Add the LocationInfo object to the list
                        locations.add(locationInfo);
                    } else {
                        Gdx.app.error("MongoDB", "Invalid coordinates in document: " + doc.toJson());
                    }
                } else {
                    Gdx.app.error("MongoDB", "Missing or invalid location data in document: " + doc.toJson());
                }
            }
            cursor.close();
        } catch (Exception e) {
            Gdx.app.error("MongoDB", "Error fetching location info from MongoDB", e);
        }

        // Log the number of locations fetched
        Gdx.app.log("MongoDB", "Total locations fetched: " + locations.size());

        return locations;
    }




    /*public void updatePointInDatabase(Geolocation oldLocation, LocationInfo newLocation) {
        try {
            MongoCollection<Document> collection = database.getCollection("chargepoints");

            // Query to find the old point
            Document query = new Document("location", new Document("$geoIntersects",
                new Document("$geometry", new Document("type", "Point")
                    .append("coordinates", Arrays.asList(oldLocation.lng, oldLocation.lat)))));

            // Updated point data
            Document updatedPoint = new Document()
                .append("locationName", newLocation.getLocationName())
                .append("address", newLocation.getAddress())
                .append("location", new Document("type", "Point")
                    .append("coordinates", Arrays.asList(newLocation.getLongitude(), newLocation.getLatitude())))
                .append("connectors", newLocation.getConnectors())
                .append("connectorsAvailable", newLocation.getConnectorsAvailable());

            // Update the point in the database
            collection.updateOne(query, new Document("$set", updatedPoint));
            Gdx.app.log("MongoDB", "Point updated successfully: " + updatedPoint);
        } catch (Exception e) {
            e.printStackTrace();
            Gdx.app.log("MongoDB", "Error updating point: " + e.getMessage());
        }
    }*/
    public void updatePointInDatabase(Geolocation oldLocation, LocationInfo newLocation) {
        try {
            MongoCollection<Document> collection = database.getCollection("chargepoints");

            // Query to find the old point based on geo-coordinates (longitude and latitude)
            Document query = new Document("location", new Document("$geoIntersects",
                new Document("$geometry", new Document("type", "Point")
                    .append("coordinates", Arrays.asList(oldLocation.getLng(), oldLocation.getLat())))));

            // Updated point data from the newLocation
            Document updatedPoint = new Document()
                .append("locationName", newLocation.getLocationName())  // Update location name
                .append("address", newLocation.getAddress())  // Update address
                .append("location", new Document("type", "Point")
                    .append("coordinates", Arrays.asList(newLocation.getLongitude(), newLocation.getLatitude())))  // Update coordinates
                .append("connectors", newLocation.getConnectors())  // Update connectors
                .append("connectorsAvailable", newLocation.getConnectorsAvailable());  // Update available connectors

            // Update the point in the database
            UpdateResult result = collection.updateOne(query, new Document("$set", updatedPoint));

            // Log the result of the update
            Gdx.app.log("MongoDB", "Point updated successfully. Modified count: " + result.getModifiedCount());
        } catch (Exception e) {
            e.printStackTrace();
            Gdx.app.log("MongoDB", "Error updating point: " + e.getMessage());
        }
    }

    public void saveUserAndScore(String username, int score) {
        if (username == null || username.isEmpty()) {
            Gdx.app.log("MongoDB", "Cannot save user and score. Username is null or empty.");
            return;
        }

        try {
            // Check if the user already exists in the database
            Document existingUser = database.getCollection("game")
                .find(new Document("username", username))
                .first();

            if (existingUser != null) {
                // Username already exists, so don't save it again
                Gdx.app.log("MongoDB", "User already exists: " + username + ". Score not saved.");
                return;
            }

            // Create a document for the user and score
            Document userScore = new Document("username", username)
                .append("score", score)
                .append("timestamp", System.currentTimeMillis());

            // Insert the document into the "game" collection
            database.getCollection("game").insertOne(userScore);

            Gdx.app.log("MongoDB", "User and score saved: " + username + ", Score: " + score);
        } catch (Exception e) {
            Gdx.app.log("MongoDB", "Failed to save user and score: " + e.getMessage());
        }
    }

    public List<Document> fetchLeaderboard() {
        List<Document> leaderboard = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection("game");

            // Fetch leaderboard data sorted by score in descending order
            FindIterable<Document> results = collection.find().sort(Sorts.descending("score"));

            for (Document doc : results) {
                leaderboard.add(doc);
            }

            Gdx.app.log("MongoDB", "Fetched leaderboard: " + leaderboard);
        } catch (Exception e) {
            Gdx.app.log("MongoDB", "Error fetching leaderboard: " + e.getMessage());
        }
        return leaderboard;
    }







    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("Connection to MongoDB closed!");
        }
    }

}
