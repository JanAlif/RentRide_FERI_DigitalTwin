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

            mongoClient = MongoClients.create("mongodb+srv://admin:PK3TipQ3Mj9Ji6N@projektnipraktikum.epnifwl.mongodb.net/RentRideApp"); // Replace with your MongoDB URI


            database = mongoClient.getDatabase("RentRideApp");

            System.out.println("Connected to MongoDB!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPointToDatabase(LocationInfo location, String address) {
        try {
            MongoCollection<Document> collection = database.getCollection("chargepoints");


            Document locationObject = new Document()
                .append("type", "Point")
                .append("coordinates", Arrays.asList(location.getLongitude(), location.getLatitude()));


            Document newPoint = new Document()
                .append("locationName", location.getLocationName())
                .append("address", address)
                .append("location", locationObject)
                .append("connectors", location.getConnectors())
                .append("connectorsAvailable", location.getConnectorsAvailable())
                .append("createdAt", new Date())
                .append("updatedAt", new Date());


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

            Gdx.app.log("MongoDB", "Attempting to access the 'cars' collection");


            MongoCollection<Document> collection = database.getCollection("chargepoints");


            long count = collection.countDocuments();
            Gdx.app.log("MongoDB", "Number of documents in 'cars' collection: " + count);


            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();


                Gdx.app.log("MongoDB", "Document fetched: " + doc.toJson());


                String locationName = doc.getString("locationName");
                Document location = (Document) doc.get("location");

                if (location != null && location.containsKey("coordinates")) {

                    List<Double> coordinates = (List<Double>) location.get("coordinates");


                    if (coordinates != null && coordinates.size() == 2) {
                        Number lngValue = coordinates.get(0);
                        Number latValue = coordinates.get(1);

                        double lng = lngValue.doubleValue();
                        double lat = latValue.doubleValue();


                        int connectors = doc.getInteger("connectors", 0);
                        int connectorsAvailable = doc.getInteger("connectorsAvailable", 0);
                        String address = doc.getString("address");

                        LocationInfo locationInfo = new LocationInfo(locationName, lat, lng, connectors, connectorsAvailable,address);


                        Gdx.app.log("MongoDB", "LocationInfo fetched: " + locationInfo);


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


        Gdx.app.log("MongoDB", "Total locations fetched: " + locations.size());

        return locations;
    }

    public void updatePointInDatabase(Geolocation oldLocation, LocationInfo newLocation) {
        try {
            MongoCollection<Document> collection = database.getCollection("chargepoints");


            Document query = new Document("location", new Document("$geoIntersects",
                new Document("$geometry", new Document("type", "Point")
                    .append("coordinates", Arrays.asList(oldLocation.getLng(), oldLocation.getLat())))));


            Document updatedPoint = new Document()
                .append("locationName", newLocation.getLocationName())
                .append("address", newLocation.getAddress())
                .append("location", new Document("type", "Point")
                    .append("coordinates", Arrays.asList(newLocation.getLongitude(), newLocation.getLatitude())))
                .append("connectors", newLocation.getConnectors())
                .append("connectorsAvailable", newLocation.getConnectorsAvailable());


            UpdateResult result = collection.updateOne(query, new Document("$set", updatedPoint));


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

            Document existingUser = database.getCollection("game")
                .find(new Document("username", username))
                .first();

            if (existingUser != null) {

                Gdx.app.log("MongoDB", "User already exists: " + username + ". Score not saved.");
                return;
            }


            Document userScore = new Document("username", username)
                .append("score", score)
                .append("timestamp", System.currentTimeMillis());


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
