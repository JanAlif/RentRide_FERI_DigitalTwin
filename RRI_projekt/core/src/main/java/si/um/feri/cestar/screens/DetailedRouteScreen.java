package si.um.feri.cestar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import si.um.feri.cestar.IgreProjekt;
import si.um.feri.cestar.Utils.Car;
import si.um.feri.cestar.Utils.Constants;
import si.um.feri.cestar.Utils.GameManager;
import si.um.feri.cestar.Utils.Geolocation;
import si.um.feri.cestar.Utils.MapRasterTiles;
import si.um.feri.cestar.Utils.MongoDB;
import si.um.feri.cestar.Utils.ZoomXY;
import si.um.feri.cestar.assets.AssetsDescriptor;


public class DetailedRouteScreen extends ScreenAdapter {

    private final IgreProjekt game;
    private final Geolocation[][] routeCoordinates;
    private final List<Geolocation> semaforPoints;

    private PerspectiveCamera camera;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private ModelBatch modelBatch;
    private Skin skin;

    // Map tiles and tile-related attributes
    private Texture[] mapTiles;
    private ZoomXY beginTile;

    // Bounding box coordinates
    private double minLat, maxLat;
    private double minLng, maxLng;
    private Car car;
    private Stage stage;

    private boolean isCameraFollowingCar = true;// Timer for the traffic light pause
    private boolean isPausedAtTrafficLight = false;

    private Vector3 savedCarPosition = null;
    private int savedCarIndex = 0;
    private Set<Geolocation> processedTrafficLights = new HashSet<>();
    MongoDB mongoDBExample;
    private boolean hasFinished = false;
    private final AssetManager assetManager;



    public DetailedRouteScreen(IgreProjekt game, Geolocation[][] routeCoordinates, List<Geolocation> semaforPoints) {
        this.game = game;
        this.routeCoordinates = routeCoordinates;
        this.semaforPoints = semaforPoints;
        this.assetManager = game.getAssetManager();

    }

    @Override
    public void show() {

        mongoDBExample = new MongoDB();
        mongoDBExample.connectToMongoDB();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Initialize perspective camera
        camera = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f;
        camera.far = 2000f;
        skin = assetManager.get(AssetsDescriptor.UI_SKIN);


        // Compute the bounding box for the route
        computeBoundingBox();

        if (savedCarPosition != null && savedCarIndex >= 0) {
            car.setPosition(savedCarPosition);
            car.setCurrentPointIndex(savedCarIndex);
        }

        // Initialize renderers
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        modelBatch = new ModelBatch();




        // Load map tiles and set beginTile
        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(
                routeCoordinates[0][0].lat, routeCoordinates[0][0].lng, Constants.ZOOM
            );
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);

            beginTile = new ZoomXY(
                Constants.ZOOM,
                centerTile.x - ((Constants.NUM_TILES - 1) / 2),
                centerTile.y - ((Constants.NUM_TILES - 1) / 2)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!semaforPoints.isEmpty()) {
            Geolocation startPoint = semaforPoints.get(0);
            if (routeCoordinates.length > 0 && routeCoordinates[0].length > 0 && car == null) {
                car = new Car(routeCoordinates, beginTile, 10f);

                Vector2 startPointPos = MapRasterTiles.getPixelPosition(
                    routeCoordinates[0][0].lat, routeCoordinates[0][0].lng, beginTile.x, beginTile.y
                );
                car.setPosition(new Vector3(startPointPos.x, startPointPos.y, 0));
                car.setCurrentPointIndex(0);
            }

            // Only set camera if car is newly created or first time showing the screen
            Vector2 carStartPos = MapRasterTiles.getPixelPosition(
                startPoint.lat, startPoint.lng, beginTile.x, beginTile.y
            );
            camera.position.set(carStartPos.x, carStartPos.y - 100, 80);
            camera.lookAt(carStartPos.x, carStartPos.y, 0);
            camera.up.set(0, 0, 1);
        }


        // Center and zoom the camera for the full route
        centerAndZoomCamera();
    }









    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        if (isPausedAtTrafficLight) {
            // We simulate a 3-second wait before resuming the journey
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    // After the delay, resume the journey
                    isPausedAtTrafficLight = false;
                    // Optionally, restore the car's position or update other necessary state
                    car.setPosition(savedCarPosition);
                    car.setCurrentPointIndex(savedCarIndex);
                    // Resume movement or other related actions
                }
            }, 3); // 3-second delay before resuming
        }
        else {
            if (isCameraFollowingCar && car != null) {
                car.update(delta);

                Vector3 carPosition = car.getPosition();
                Vector3 carDirection = car.getDirection();

                Vector3 behindCar = carDirection.cpy().scl(-50);
                behindCar.z = 50;

                camera.position.set(carPosition.cpy().add(behindCar));
                camera.lookAt(carPosition.x, carPosition.y, 0);
                camera.up.set(0, 0, 1);
                camera.update();

                // Handle reaching traffic lights
                Geolocation trafficLightGeo = hasCarReachedTrafficLight(carPosition);
                if (trafficLightGeo != null && !processedTrafficLights.contains(trafficLightGeo)) {
                    // Save the car's position and index before pausing at the traffic light
                    savedCarPosition = car.getPosition().cpy();
                    savedCarIndex = car.getCurrentPointIndex();

                    // Pause the journey and show the QuestionScreen
                    isPausedAtTrafficLight = true;

                    // Create and configure the QuestionScreen
                    QuestionScreen questionScreen = new QuestionScreen(game, routeCoordinates, semaforPoints);

                    // Set the callback for when the correct answer is given
                    questionScreen.setOnCorrectAnswer(() -> {
                        // This is the code that will be executed when the user gives the correct answer
                        isPausedAtTrafficLight = true; // Pause the game to simulate the 3 second wait

                        // Use a timer or wait for 3 seconds before resuming
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                isPausedAtTrafficLight = false; // Resume the journey
                                processedTrafficLights.add(trafficLightGeo); // Mark this traffic light as processed
                                game.setScreen(DetailedRouteScreen.this); // Switch back to the DetailedRouteScreen
                            }
                        }, 3); // 3 seconds delay
                    });
                    questionScreen.setOnIncorrectAnswer(() -> {
                        // Delay the screen switch to allow time for feedback to display
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                processedTrafficLights.add(trafficLightGeo);
                                game.setScreen(DetailedRouteScreen.this);  // Switch back to the DetailedRouteScreen
                            }
                        }, 0.2f);  // 0.2 seconds delay for a smoother transition
                    });


                    game.setScreen(questionScreen);
                }

            }


            drawMapTiles();
            drawRoute();
            drawSemafors();
            drawStartAndFinishMarkers();

            if (car != null) {
                Vector3 carPosition = car.getPosition();
                Vector3 direction = car.getDirection();

                drawArrow(carPosition, direction);
            }
        }

        if (car != null && hasCarReachedFinish(car.getPosition()) && !hasFinished) {
            String currentUser = GameManager.getInstance().getCurrentUser();
            int currentScore = GameManager.getInstance().getScore();


            if (currentUser != null && !currentUser.isEmpty()) {
                mongoDBExample.saveUserAndScore(currentUser, currentScore);
                Gdx.app.log("DetailedRouteScreen", "Game finished. User: " + currentUser + ", Score: " + currentScore);

            } else {
                Gdx.app.log("DetailedRouteScreen", "Cannot save score. No user set.");
            }

            // Set the flag to true to prevent repeated saving
            hasFinished = true;
            //game.setScreen(new LeaderboardScreen(game));
            showFinishPopup(currentScore,currentUser,semaforPoints.size());

            // Optionally, transition after a delay
            /*Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    game.setScreen(new SummaryScreen(game, currentUser, currentScore));
                }
            }, 2);// 2 seconds delay before transitioning*/
        }

        stage.act(delta);
        stage.draw();


    }

    private void showFinishPopup(int score, String user, int totalQuestions) {

        // Create a dialog
        Dialog finishDialog = new Dialog("Congratulations " + user + " for completing the game!", skin) {
            @Override
            protected void result(Object object) {
                if (object.equals("PLAY_AGAIN")) {
                    resetGame();
                    game.setScreen(new GameScreen(game));
                } else if (object.equals("QUIT")) {
                    Gdx.app.exit();
                }
            }
        };

        // Add a congratulatory message
        finishDialog.text("You have successfully completed the route!\nYour Score: " + score +
            "\nTotal Questions Answered: " + totalQuestions);
        finishDialog.setColor(Color.BLACK);

        // Add buttons
        finishDialog.button("Play Again", "PLAY_AGAIN");
        finishDialog.button("Quit", "QUIT");

        // Show the dialog
        finishDialog.show(stage);
    }


    private void resetGame() {
        // Reset game state
        hasFinished = false;

        // Reset car to its initial position and state
        car = new Car(routeCoordinates, beginTile, 10f); // Recreate the car object

        // Reset processed traffic lights and other game-related flags
        processedTrafficLights.clear();
        savedCarPosition = null;
        savedCarIndex = -1;
        isPausedAtTrafficLight = false;

        // Reset the score (if applicable)
        GameManager.getInstance().resetScore(); // Or use a custom method if setScore is available

        Gdx.app.log("DetailedRouteScreen", "Game reset.");
    }





    private boolean hasCarReachedFinish(Vector3 carPosition) {
        Geolocation finish = routeCoordinates[routeCoordinates.length - 1]
            [routeCoordinates[routeCoordinates.length - 1].length - 1];
        Vector3 finishPos = getTrafficLightPosition(finish); // Reuse the position logic
        float threshold = 5.0f; // Adjust threshold to a reasonable value (e.g., 1.0f)

        float distance = carPosition.dst(finishPos);
        //Gdx.app.log("DetailedRouteScreen", "Car position: " + carPosition + " Finish position: " + finishPos + " Distance: " + distance);

        return distance < threshold;
    }



    private Geolocation hasCarReachedTrafficLight(Vector3 carPosition) {
        // Define a threshold distance to determine if the car is at a traffic light
        float threshold = 2.5f; // Distance in units

        // Iterate over all traffic lights
        for (Geolocation trafficLightGeo : semaforPoints) {
            Vector3 trafficLightPosition = getTrafficLightPosition(trafficLightGeo);

            // Check if the car is within the threshold distance from the traffic light
            if (carPosition.dst(trafficLightPosition) < threshold) {
                return trafficLightGeo; // Return the traffic light that was reached
            }
        }

        return null; // No traffic light reached
    }

    private Vector3 getTrafficLightPosition(Geolocation trafficLightGeo) {
        // Convert the geolocation of the traffic light to 2D screen coordinates
        Vector2 position2D = MapRasterTiles.getPixelPosition(
            trafficLightGeo.lat, trafficLightGeo.lng, beginTile.x, beginTile.y
        );

        // Convert the 2D position to 3D (with a fixed Z-axis value for height)
        return new Vector3(position2D.x, position2D.y, 0); // Adjust the Z value if needed
    }

    private void computeBoundingBox() {
        minLat = Double.MAX_VALUE;
        maxLat = -Double.MAX_VALUE;
        minLng = Double.MAX_VALUE;
        maxLng = -Double.MAX_VALUE;

        for (Geolocation[] segment : routeCoordinates) {
            for (Geolocation geo : segment) {
                minLat = Math.min(minLat, geo.lat);
                maxLat = Math.max(maxLat, geo.lat);
                minLng = Math.min(minLng, geo.lng);
                maxLng = Math.max(maxLng, geo.lng);
            }
        }
    }

    private void centerAndZoomCamera() {
        if (beginTile == null) {
            throw new IllegalStateException("beginTile is null. Ensure it is initialized before calling centerAndZoomCamera().");
        }

        double centerLat = (minLat + maxLat) / 2.0;
        double centerLng = (minLng + maxLng) / 2.0;
        Vector2 center = MapRasterTiles.getPixelPosition(centerLat, centerLng, beginTile.x, beginTile.y);

        double latSpan = maxLat - minLat;
        double lngSpan = maxLng - minLng;

        float pixelSpanX = (float) (lngSpan * MapRasterTiles.TILE_SIZE / Constants.ZOOM);
        float pixelSpanY = (float) (latSpan * MapRasterTiles.TILE_SIZE / Constants.ZOOM);

        float viewportWidth = Gdx.graphics.getWidth();
        float viewportHeight = Gdx.graphics.getHeight();

        float zoomX = viewportWidth / pixelSpanX;
        float zoomY = viewportHeight / pixelSpanY;

        float desiredZoom = Math.min(zoomX, zoomY) * 0.8f;

        camera.position.set(center.x, center.y - 100, desiredZoom * 500); // Offset for height
        camera.lookAt(center.x, center.y, 0);
        camera.update();
    }

    private void handleInput() {
        // Toggle following car with F key
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            isCameraFollowingCar = !isCameraFollowingCar;
            System.out.println("Camera mode: " + (isCameraFollowingCar ? "Following Car" : "Manual Control"));
        }

        // Manual control if NOT following the car
        if (!isCameraFollowingCar) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.z += 1f;    // Zoom out
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.position.z -= 1f;    // Zoom in
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) camera.translate(-1, 0, 0);
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) camera.translate(1, 0, 0);
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) camera.translate(0, 1, 0);
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) camera.translate(0, -1, 0);

            camera.update();
        }
    }

    private void drawMapTiles() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        int tileIndex = 0;
        for (int j = Constants.NUM_TILES - 1; j >= 0; j--) {
            for (int i = 0; i < Constants.NUM_TILES; i++) {
                if (tileIndex >= mapTiles.length || mapTiles[tileIndex] == null) {
                    tileIndex++;
                    continue;
                }
                float tileDrawX = i * MapRasterTiles.TILE_SIZE;
                float tileDrawY = j * MapRasterTiles.TILE_SIZE;
                spriteBatch.draw(
                    mapTiles[tileIndex++],
                    tileDrawX,
                    tileDrawY,
                    MapRasterTiles.TILE_SIZE,
                    MapRasterTiles.TILE_SIZE
                );
            }
        }
        spriteBatch.end();
    }


    private void drawRoute() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLUE);

        for (Geolocation[] segment : routeCoordinates) {
            if (segment.length < 2) continue;
            for (int i = 0; i < segment.length - 1; i++) {
                Vector2 startPos = MapRasterTiles.getPixelPosition(
                    segment[i].lat, segment[i].lng, beginTile.x, beginTile.y
                );
                Vector2 endPos = MapRasterTiles.getPixelPosition(
                    segment[i + 1].lat, segment[i + 1].lng, beginTile.x, beginTile.y
                );
                shapeRenderer.line(startPos.x, startPos.y, endPos.x, endPos.y);
            }
        }
        shapeRenderer.end();
    }

    private void drawArrow(Vector3 position, Vector3 direction) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Mark the center with a small circle
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.circle(position.x, position.y, 5);

        // Create a tiny triangle for the arrow head
        Vector3 arrowHead = position.cpy().add(direction.nor().scl(10));
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.triangle(
            position.x, position.y,
            arrowHead.x - 2, arrowHead.y - 2,
            arrowHead.x + 2, arrowHead.y + 2
        );
        shapeRenderer.end();
    }


    private void drawSemafors() {
        if (semaforPoints == null || semaforPoints.isEmpty()) return;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Realistic dimensions for the pole and traffic light housing
        float poleWidth = 1.5f;    // Narrow pole width
        float poleHeight = 20f;    // Height of the pole
        float lightBoxWidth = 5f;  // Width of the light housing
        float lightBoxHeight = 2f; // Height for a single light
        float lightSpacing = 0.5f; // Small spacing between lights
        float poleOffsetX = 10f;   // Offset for pole position

        // Total height of the traffic light housing (3 lights + spacings)
        float housingHeight = (lightBoxHeight * 3) + (lightSpacing * 2);

        // Timed color cycle: 6 seconds total, 2s each color
        float stateDuration = 2.0f;
        float stateTime = (System.currentTimeMillis() % (int) (stateDuration * 3000)) / 1000f;
        int currentState = (int) (stateTime / stateDuration) % 3; // 0=Red, 1=Yellow, 2=Green

        for (Geolocation semafor : semaforPoints) {
            Vector2 position2D = MapRasterTiles.getPixelPosition(
                semafor.lat, semafor.lng, beginTile.x, beginTile.y
            );

            // Position for the base of the pole
            float baseYPosition = position2D.y + 5f; // Slightly above the map surface
            float baseZ = 20f; // Position in front of the camera

            // Pole position
            Vector3 polePosition = new Vector3(
                position2D.x + poleOffsetX,
                baseYPosition,
                baseZ
            );

            // Draw the pole
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.box(
                polePosition.x - poleWidth / 2,
                polePosition.y - poleWidth / 2,
                polePosition.z,
                poleWidth,
                poleWidth,
                poleHeight
            );

            // Adjust housingPosition to sit directly on top of the pole
            Vector3 housingPosition = new Vector3(
                polePosition.x,
                polePosition.y,
                polePosition.z // The housing's base directly above the pole's top
            );

// Draw the traffic light housing (light gray box)
            // Draw the traffic light housing (light gray box)
            shapeRenderer.setColor(new Color(Color.LIGHT_GRAY.r, Color.LIGHT_GRAY.g, Color.LIGHT_GRAY.b, 1f)); // Fully opaque
            shapeRenderer.box(
                housingPosition.x - lightBoxWidth / 2,
                housingPosition.y - lightBoxWidth / 2,
                housingPosition.z,
                lightBoxWidth,
                lightBoxWidth,
                housingHeight
            );


            // Calculate positions for the lights within the housing
            float greenLightZ = polePosition.z-5f; // Green light at the base of the housing
            float yellowLightZ = greenLightZ + lightBoxHeight + lightSpacing; // Yellow light above green
            float redLightZ = yellowLightZ + lightBoxHeight + lightSpacing;

            // -- Green Light Box --
            shapeRenderer.setColor(currentState == 2 ? Color.GREEN : Color.DARK_GRAY);
            shapeRenderer.box(
                housingPosition.x - (lightBoxWidth / 3),
                housingPosition.y - (lightBoxWidth / 3),
                greenLightZ,
                lightBoxWidth / 1.5f, lightBoxWidth / 1.5f, lightBoxHeight
            );

            // -- Yellow Light Box --
            shapeRenderer.setColor(currentState == 1 ? Color.YELLOW : Color.DARK_GRAY);
            shapeRenderer.box(
                housingPosition.x - (lightBoxWidth / 3),
                housingPosition.y - (lightBoxWidth / 3),
                yellowLightZ,
                lightBoxWidth / 1.5f, lightBoxWidth / 1.5f, lightBoxHeight
            );

            // -- Red Light Box --
            shapeRenderer.setColor(currentState == 0 ? Color.RED : Color.DARK_GRAY);
            shapeRenderer.box(
                housingPosition.x - (lightBoxWidth / 3),
                housingPosition.y - (lightBoxWidth / 3),
                redLightZ,
                lightBoxWidth / 1.5f, lightBoxWidth / 1.5f, lightBoxHeight
            );
        }
        shapeRenderer.end();
    }

    private void drawStartAndFinishMarkers() {
        // Retrieve the start and finish coordinates
        Geolocation start = routeCoordinates[0][0];
        Geolocation finish = routeCoordinates[routeCoordinates.length - 1]
            [routeCoordinates[routeCoordinates.length - 1].length - 1];

        // Convert geolocations to pixel positions
        Vector2 startPos = MapRasterTiles.getPixelPosition(start.lat, start.lng, beginTile.x, beginTile.y);
        Vector2 finishPos = MapRasterTiles.getPixelPosition(finish.lat, finish.lng, beginTile.x, beginTile.y);

        // Draw circles (optional, to mark positions visually)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.circle(startPos.x, startPos.y, 15);

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(finishPos.x, finishPos.y, 15);

        shapeRenderer.end();

        // Draw "START" and "FINISH" text on the ground
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        BitmapFont font = new BitmapFont(); // You can use a custom font if available
        font.getData().setScale(2f); // Scale the font for better visibility
        font.setColor(Color.WHITE); // Set text color

        // Draw "START" at the start position
        font.draw(spriteBatch, "START", startPos.x - 30, startPos.y - 30);

        // Draw "FINISH" at the finish position
        font.draw(spriteBatch, "FINISH", finishPos.x - 40, finishPos.y - 30);

        spriteBatch.end();

        font.dispose(); // Dispose of the font after use to avoid memory leaks
    }


    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        if (mapTiles != null) {
            for (Texture tile : mapTiles) {
                if (tile != null) tile.dispose();
            }
        }
    }
}



