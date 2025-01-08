package si.um.feri.cestar.Utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MapRasterTiles {

    public static String cacheDirectory = "tileCache";
    static String mapServiceUrl = "https://maps.geoapify.com/v1/tile/";
    static String token = "?&apiKey=" + Keys.GEOAPIFY;
    static String tilesetId = "klokantech-basic";
    static String format = "@2x.png";


    final static public int TILE_SIZE = 512;

    public static Texture getRasterTile(int zoom, int x, int y) throws IOException {
        // Check if the tile is cached
        File cachedFile = getCachedTileFile(zoom, x, y);
        if (cachedFile.exists()) {
            // Load tile from cache
            return getTextureFromFile(cachedFile);
        }

        // Fetch the tile from the API
        URL url = new URL(mapServiceUrl + tilesetId + "/" + zoom + "/" + x + "/" + y + format + token);
        ByteArrayOutputStream bis = fetchTile(url);

        // Save the tile to cache
        saveTileToCache(bis.toByteArray(), cachedFile);

        // Return the tile texture
        return getTexture(bis.toByteArray());
    }

    private static File getCachedTileFile(int zoom, int x, int y) {
        String fileName = "tile_" + zoom + "_" + x + "_" + y + ".png";
        return new File(cacheDirectory, fileName);
    }

    private static void saveTileToCache(byte[] data, File file) throws IOException {
        File dir = new File(cacheDirectory);
        if (!dir.exists()) {
            dir.mkdirs(); // Create cache directory if it doesn't exist
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }


    private static Texture getTextureFromFile(File file) {
        Pixmap pixmap;
        try {
            FileHandle fileHandle = new FileHandle(file);
            pixmap = new Pixmap(fileHandle);
        } catch (Exception e) {
            throw new RuntimeException("Error loading pixmap from file: " + file.getAbsolutePath(), e);
        }
        return new Texture(pixmap);
    }

    public static Texture[] getRasterTileZone(ZoomXY zoomXY, int size) throws IOException {
        Texture[] array = new Texture[size * size];
        int[] factorY = new int[size * size]; //if size is 3 {-1, -1, -1, 0, 0, 0, 1, 1, 1};
        int[] factorX = new int[size * size]; //if size is 3 {-1, 0, 1, -1, 0, 1, -1, 0, 1};

        int value = (size - 1) / -2;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                factorY[i * size + j] = value;
                factorX[i + j * size] = value;
            }
            value++;
        }

        for (int i = 0; i < size * size; i++) {
            array[i] = getRasterTile(zoomXY.zoom, zoomXY.x + factorX[i], zoomXY.y + factorY[i]);
            System.out.println(zoomXY.zoom + "/" + (zoomXY.x + factorX[i]) + "/" + (zoomXY.y + factorY[i]));
        }
        return array;
    }

    public static ByteArrayOutputStream fetchTile(URL url) throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        byte[] bytebuff = new byte[4096];
        int n;

        while ((n = is.read(bytebuff)) > 0) {
            bis.write(bytebuff, 0, n);
        }
        return bis;
    }

    public static Texture getTexture(byte[] array) {
        return new Texture(new Pixmap(array, 0, array.length));
    }

    public static ZoomXY getTileNumber(final double lat, final double lon, final int zoom) {
        int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        if (xtile < 0)
            xtile = 0;
        if (xtile >= (1 << zoom))
            xtile = ((1 << zoom) - 1);
        if (ytile < 0)
            ytile = 0;
        if (ytile >= (1 << zoom))
            ytile = ((1 << zoom) - 1);
        return new ZoomXY(zoom, xtile, ytile);
    }

    public static double[] project(double lat, double lng, int tileSize) {
        double siny = Math.sin((lat * Math.PI) / 180);

        // Truncating to 0.9999 effectively limits latitude to 89.189. This is
        // about a third of a tile past the edge of the world tile.
        siny = Math.min(Math.max(siny, -0.9999), 0.9999);

        return new double[]{
            tileSize * (0.5 + lng / 360),
            tileSize * (0.5 - Math.log((1 + siny) / (1 - siny)) / (4 * Math.PI))
        };
    }

    public static Vector2 getPixelPosition(double lat, double lng, int beginTileX, int beginTileY) {
        double[] worldCoordinate = project(lat, lng, MapRasterTiles.TILE_SIZE);
        // Scale to fit our image
        double scale = Math.pow(2, Constants.ZOOM);

        // Apply scale to world coordinates to get image coordinates
        return new Vector2(
            (int) (Math.floor(worldCoordinate[0] * scale) - (beginTileX * MapRasterTiles.TILE_SIZE)),
            Constants.MAP_HEIGHT - (int) (Math.floor(worldCoordinate[1] * scale) - (beginTileY * MapRasterTiles.TILE_SIZE) - 1)
        );
    }

    public static Geolocation[][] fetchPath(Geolocation[] geolocations){
        // Example coordinates (longitude, latitude)
        double[][] coordinatesArray = {
            {-122.42, 37.78}, // San Francisco
            {-121.89, 37.33}, // San Jose
            {-122.08, 37.39}  // Palo Alto
        };

        double[][] coordinates = new double[geolocations.length][2];
        for(int i=0; i<geolocations.length; i++){
            coordinates[i] = new double[]{geolocations[i].lat, geolocations[i].lng};
        }

        try {
            return getRouteFromCoordinates(coordinates);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Geolocation[][] getRouteFromCoordinates(double[][] coordinates) throws Exception {
        // Build the coordinates string for the URL
        StringBuilder coordinatesPath = new StringBuilder();
        for (int i = 0; i < coordinates.length; i++) {
            coordinatesPath.append(coordinates[i][0]).append(",").append(coordinates[i][1]);
            if (i < coordinates.length - 1) coordinatesPath.append("|");
        }

        // Construct the URL
        String urlString = "https://api.geoapify.com/v1/routing?waypoints=" + coordinatesPath.toString() +
            "&mode=" + "drive" + "&apiKey=" + Keys.GEOAPIFY;

        // Open connection
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Check the response code
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
        }

        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Geolocation[][] geolocations;

        // Parse the JSON response
        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray features = jsonResponse.getJSONArray("features");
        if (features.length() > 0) {
            JSONObject geometry = features.getJSONObject(0).getJSONObject("geometry");
            JSONArray coordinatesArray = geometry.getJSONArray("coordinates");

            geolocations = new Geolocation[coordinatesArray.length()][];
            // Print each coordinate in the path
            for (int i = 0; i < coordinatesArray.length(); i++) {
                JSONArray coord = coordinatesArray.getJSONArray(i);

                Geolocation[] geol = new Geolocation[coord.length()];
                for (int j = 0; j < coord.length(); j++) {
                    JSONArray c = coord.getJSONArray(j);
                    double lon = c.getDouble(0);
                    double lat = c.getDouble(1);
                    System.out.println("Longitude: " + lon + ", Latitude: " + lat);
                    geol[j] = new Geolocation(lat, lon);
                }
                geolocations[i] = geol;
            }

            return geolocations;
        } else {
            return null;
        }
    }
}
