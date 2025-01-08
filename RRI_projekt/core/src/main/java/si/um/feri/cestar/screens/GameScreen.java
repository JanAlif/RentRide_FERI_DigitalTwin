package si.um.feri.cestar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import si.um.feri.cestar.IgreProjekt;
import si.um.feri.cestar.Utils.Constants;
import si.um.feri.cestar.Utils.GameManager;
import si.um.feri.cestar.Utils.Geolocation;
import si.um.feri.cestar.Utils.LocationInfo;
import si.um.feri.cestar.Utils.MapRasterTiles;
import si.um.feri.cestar.Utils.MongoDB;
import si.um.feri.cestar.Utils.SoundManager;
import si.um.feri.cestar.Utils.ZoomXY;
import si.um.feri.cestar.assets.AssetsDescriptor;
import si.um.feri.cestar.assets.RegionNames;

class SelectablePoint {
    LocationInfo geolocation;
    boolean isHighlighted = false;

    SelectablePoint(LocationInfo geolocation) {
        this.geolocation = geolocation;
    }
}

public class GameScreen extends ScreenAdapter {

    private final IgreProjekt game;
    private final AssetManager assetManager;

    // Map and rendering-related objects
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;

    // Game elements
    private Vector3 touchPosition;
    private Geolocation[][] routeCoordinates;
    private List<Geolocation> selectedPoints = new ArrayList<>();
    private List<Geolocation> semaforPoints = new ArrayList<>();

    // Assets
    private TextureRegion startIcon;
    private TextureRegion endIcon;
    private BitmapFont font;
    private Texture[] mapTiles;
    private ZoomXY beginTile;

    private Stage stage;
    private Skin skin;
    private TextButton startButton;
    private TextButton menuButton;

    private boolean routeReady = false;
    private boolean showRouteOnly = false;  // Flag to control visibility of markers
    private static final double SEMAFOR_INTERVAL_METERS = 80.0; // Interval in meters
    MongoDB mongoDBExample;
    private List<LocationInfo> databasePoints = new ArrayList<>();

    // Constants for movement and zoom speed
    private static final float MOVEMENT_SPEED = 10f; // Movement speed
    private static final float ZOOM_SPEED = 0.5f; // Zoom speed

    private List<SelectablePoint> selectablePoints = new ArrayList<>();
    private boolean isEditingMode;
    public static final double CLICK_THRESHOLD_DISTANCE = 50.0; // In meters



    // Constants for geolocation
    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.557314, 15.637771);
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);

    public GameScreen(IgreProjekt game) {
        this.game = game;
        this.assetManager = game.getAssetManager();
    }

    @Override
    public void show() {

        SoundManager.getInstance().playGameMusic();

        mongoDBExample = new MongoDB();
        mongoDBExample.connectToMongoDB();

        // Initialize the camera and set it to the current screen dimensions
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);

        // Update camera for full screen mode (when the window size changes)
        camera.viewportWidth = Gdx.graphics.getWidth();
        camera.viewportHeight = Gdx.graphics.getHeight();
        camera.update();

        // Initialize rendering objects
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        touchPosition = new Vector3();

        // Load assets (e.g., fonts, textures, etc.)
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        TextureAtlas gameplayAtlas = assetManager.get(AssetsDescriptor.GAMEPLAY);
        startIcon = gameplayAtlas.findRegion(RegionNames.START);
        endIcon = gameplayAtlas.findRegion(RegionNames.FINISH);


        // Initialize stage for UI (buttons, labels, etc.)
        stage = new Stage(new ScreenViewport());
        skin = assetManager.get(AssetsDescriptor.UI_SKIN);



        // Create and initialize the Start button
        startButton = new TextButton("Start", skin);

        // Calculate the button size relative to the screen dimensions with limits
        float buttonWidth = 150f; // Fixed width for all buttons
        float buttonHeight = 50f; // Fixed height for all buttons

        // Ensure the button size is within the defined limits
        buttonWidth = MathUtils.clamp(buttonWidth, 100f, 200f);
        buttonHeight = MathUtils.clamp(buttonHeight, 40f, 60f);

        // Set the final button size
        startButton.setSize(buttonWidth, buttonHeight);

        // Adjust position to lower-left corner with smaller margins
        float margin = 10f;  // Smaller margin
        startButton.setPosition(margin, margin);// 10px padding from the left and bottom edges

        // Initially disable the button (you'll enable it once the points are selected)
        startButton.setDisabled(true);

        // Fetch points from MongoDB (if needed)
        databasePoints = mongoDBExample.fetchPointsFromDatabase();
        initializeSelectablePoints();

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedPoints.size() == 2) {
                    // Create a dialog for name input
                    Dialog nameInputDialog = new Dialog("Enter Your Name", skin);

                    // Create a text field for name input
                    TextField nameField = new TextField("", skin);
                    nameInputDialog.getContentTable().add("Name: ").left();
                    nameInputDialog.getContentTable().add(nameField).width(200).row();

                    // Add buttons for Enter and Cancel
                    nameInputDialog.button("Enter", true); // The true value represents a positive result
                    nameInputDialog.button("Cancel", false); // The false value represents a negative result

                    // Handle dialog result
                    nameInputDialog.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            if (actor instanceof TextButton) {
                                TextButton button = (TextButton) actor;
                                String buttonText = button.getText().toString();

                                if ("Enter".equalsIgnoreCase(buttonText)) {
                                    try {
                                        // Fetch the route based on the selected points
                                        routeCoordinates = MapRasterTiles.fetchPath(new Geolocation[]{
                                            selectedPoints.get(0),
                                            selectedPoints.get(1)
                                        });

                                        Gdx.app.log("Route Coordinates", Arrays.toString(routeCoordinates));

                                        // Save the user's name to the database
                                        String playerName = nameField.getText();
                                        if (playerName.isEmpty()) {
                                            Gdx.app.log("Name Input", "Name cannot be empty!");
                                            return;
                                        }

                                        // Set the user in GameManager
                                        GameManager.getInstance().setCurrentUser(playerName);

                                        // Generate semafor points (if needed)
                                        generateSemaforPoints();
                                        routeReady = true;

                                        // Transition to the new screen after a delay (e.g., 2 seconds)
                                        Timer.schedule(new Timer.Task() {
                                            @Override
                                            public void run() {
                                                game.setScreen(new DetailedRouteScreen(game, routeCoordinates, semaforPoints));
                                            }
                                        }, 2);  // 2-second delay before transitioning
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if ("Cancel".equalsIgnoreCase(buttonText)) {
                                    // Clear the selected points and reset state
                                    selectedPoints.clear();
                                    startButton.setDisabled(true); // Disable the Start button
                                    routeCoordinates = null;
                                    routeReady = false;
                                    Gdx.app.log("Name Input", "Selection canceled.");
                                }
                            }
                        }
                    });

                    // Show the dialog
                    nameInputDialog.show(stage);
                } else {
                    Gdx.app.log("Start Button", "Not enough points selected!");
                }
            }
        });


        TextButton showDBPointsButton = new TextButton("Show DB Points", skin);

        // Calculate button size and position similarly to startButton
        float dbButtonWidth = Gdx.graphics.getWidth() * 0.2f;
        float dbButtonHeight = Gdx.graphics.getHeight() * 0.05f;
        dbButtonWidth = MathUtils.clamp(dbButtonWidth, 100f, 200f);
        dbButtonHeight = MathUtils.clamp(dbButtonHeight, 40f, 60f);

        // Set the final button size and position
        showDBPointsButton.setSize(dbButtonWidth, dbButtonHeight);
        showDBPointsButton.setPosition(margin, margin + startButton.getHeight() + 10);

        // Add click listener to show database points
        showDBPointsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("GameScreen", "Show DB Points button clicked.");
                showDatabasePointsDialog();
            }
        });

        TextButton toggleEditModeButton = new TextButton("Edit Mode: OFF", skin);

// Set size and position
        float toggleWidth = Gdx.graphics.getWidth() * 0.2f;
        float toggleHeight = Gdx.graphics.getHeight() * 0.05f;
        toggleWidth = MathUtils.clamp(toggleWidth, 100f, 200f);
        toggleHeight = MathUtils.clamp(toggleHeight, 40f, 60f);
        toggleEditModeButton.setSize(toggleWidth, toggleHeight);
        toggleEditModeButton.setPosition(margin, margin + startButton.getHeight() + showDBPointsButton.getHeight() + 20);

// Add a click listener to toggle editing mode
        toggleEditModeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isEditingMode = !isEditingMode;
                toggleEditModeButton.setText("Edit Mode: " + (isEditingMode ? "ON" : "OFF"));
                Gdx.app.log("GameScreen", "Editing mode: " + isEditingMode);
            }
        });

        // Create the menu button
        menuButton = new TextButton("Menu", skin);

        // Dynamically calculate button size
        float buttonWidthMenu = 150f; // 15% of the screen width
        float buttonHeightMenu = 50f; // 10% of the screen height

        // Clamp the button size to ensure it doesn't get too big or too small
        buttonWidthMenu = MathUtils.clamp(buttonWidthMenu, 100f, 200f); // Min: 100px, Max: 200px
        buttonHeightMenu = MathUtils.clamp(buttonHeightMenu, 40f, 60f); // Min: 40px, Max: 80px

        // Set the size and position of the menu button (initial position)
        menuButton.setSize(buttonWidthMenu, buttonHeightMenu);
        menuButton.setPosition(margin,margin); // Top-right corner

        // Add a click listener to open the menu dialog
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenuDialog(); // Open the menu dialog
            }
        });



        stage.addActor(toggleEditModeButton);
        stage.addActor(menuButton);
        // Add the start button to the stage (UI container)
        stage.addActor(startButton);
        stage.addActor(showDBPointsButton);
        // Add input processors for handling user input (e.g., gestures, touch events)
        GestureDetector gestureDetector = new GestureDetector(new GestureDetector.GestureAdapter() {
            @Override
            public boolean tap(float x, float y, int count, int button) {
                touchPosition.set(x, y, 0);
                camera.unproject(touchPosition);

                Geolocation geoClicked = screenToGeo(touchPosition.x, touchPosition.y);

                if (isEditingMode) {
                    // Editing mode: Add or modify points
                    // Check if clicked near an existing point
                    SelectablePoint closestPoint = findClosestSelectablePoint(geoClicked);
                    if (closestPoint != null) {
                        // Edit the existing point
                        showEditPointDialog(closestPoint, false);
                    } else {
                        // Add a new point, but do NOT add to database until dialog is saved
                        SelectablePoint newPoint = new SelectablePoint(
                            new LocationInfo("New Point", geoClicked.lat, geoClicked.lng, 0, 0, "New Address")
                        );
                        showEditPointDialog(newPoint, true);
                        // Don't add to database here, wait for the "Save" button in the dialog
                    }
                } else {
                    // Non-editing mode: Select start/end points and other functionality
                    if (selectedPoints.size() < 2) {
                        // Add the clicked point as start or end point
                        selectedPoints.add(geoClicked);

                        if (selectedPoints.size() == 2) {
                            // Enable the Start button if exactly two points are selected
                            startButton.setDisabled(false);
                        }
                    } else {
                        // Clear selections if two points are already selected
                        selectedPoints.clear();
                        startButton.setDisabled(true);
                    }
                }

                return true;
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                // Allow camera movement with drag (pan)
                camera.translate(-deltaX, deltaY);
                return false;
            }

            @Override
            public boolean zoom(float initialDistance, float distance) {
                // Allow zoom in and zoom out based on pinch distance
                if (initialDistance > distance) {
                    camera.zoom += ZOOM_SPEED;  // Zoom out
                } else {
                    camera.zoom -= ZOOM_SPEED;  // Zoom in
                }

                // Ensure zoom limits (min and max)
                if (camera.zoom < 0.1f) {
                    camera.zoom = 0.1f;
                } else if (camera.zoom > 3f) {
                    camera.zoom = 3f;
                }

                camera.update();  // Apply the new zoom
                return true;  // Stop further processing
            }

        });

        // Combine input processors to handle touch events, gestures, and UI interactions
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);  // For UI interaction (buttons)
        inputMultiplexer.addProcessor(gestureDetector);  // For map gestures (tap, pan, zoom)
        inputMultiplexer.addProcessor(createInputProcessor());  // For manual camera movement (keyboard)

        Gdx.input.setInputProcessor(inputMultiplexer);

        // Load map tiles and initialize the tiled map renderer
        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Constants.ZOOM);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);
            beginTile = new ZoomXY(Constants.ZOOM,
                centerTile.x - ((Constants.NUM_TILES - 1) / 2),
                centerTile.y - ((Constants.NUM_TILES - 1) / 2));

            // Fetch initial route
            routeCoordinates = MapRasterTiles.fetchPath(new Geolocation[]{CENTER_GEOLOCATION, MARKER_GEOLOCATION});
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize the tiled map renderer
        tiledMap = new TiledMap();
        MapLayers layers = tiledMap.getLayers();
        TiledMapTileLayer layer = new TiledMapTileLayer(
            Constants.NUM_TILES, Constants.NUM_TILES,
            MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);

        int index = 0;
        for (int j = Constants.NUM_TILES - 1; j >= 0; j--) {
            for (int i = 0; i < Constants.NUM_TILES; i++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(new TextureRegion(
                    mapTiles[index], MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE)));
                layer.setCell(i, j, cell);
                index++;
            }
        }
        layers.add(layer);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

/*

    private void showDatabasePointsDialog() {
        // Create a table to hold the points
        Table table = new Table();
        table.top().left();
        table.pad(5); // Reduced padding for compact layout

        boolean hasDropdown = false; // Flag to check if dropdowns exist

        // Add the points to the table
        for (int i = 0; i < databasePoints.size(); i++) {
            LocationInfo point = databasePoints.get(i);

            // Create a label for the point
            String label = "Point " + (i + 1) + ": " + point.getLocationName();
            Label pointLabel = new Label(label, skin);
            pointLabel.setColor(Color.BLACK);
            table.add(pointLabel).padBottom(2).fillX().row(); // Reduced spacing

            // Check if dropdowns are needed by checking for details
            boolean hasDetails = point.getConnectors() > 0 || point.getConnectorsAvailable() > 0;
            if (hasDetails) {
                hasDropdown = true;

                // Create a dropdown container for details
                Table dropdownContent = new Table();
                dropdownContent.setVisible(false); // Start with the dropdown hidden

                dropdownContent.add(new Label("Location Name: " + point.getLocationName(), skin)).padBottom(2).fillX().row();
                dropdownContent.add(new Label("Latitude: " + point.getLatitude(), skin)).padBottom(2).fillX().row();
                dropdownContent.add(new Label("Longitude: " + point.getLongitude(), skin)).padBottom(2).fillX().row();
                dropdownContent.add(new Label("Connectors: " + point.getConnectors(), skin)).padBottom(2).fillX().row();
                dropdownContent.add(new Label("Connectors Available: " + point.getConnectorsAvailable(), skin)).padBottom(2).fillX().row();

                // Add click listener to toggle dropdown visibility
                pointLabel.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        dropdownContent.setVisible(!dropdownContent.isVisible()); // Toggle visibility
                        table.invalidate(); // Update the table layout
                    }
                });

                // Add dropdown content to the table
                table.add(dropdownContent).padLeft(15).padBottom(5).fillX().row();
            }

            // Add click listener to highlight the point
            final int index = i; // Capture the index of the point
            pointLabel.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Highlight the selected point
                    highlightSelectedPoint(index);

                    // Reset the highlight after a delay
                    scheduleHighlightReset();
                }
            });
        }

        // Dynamically determine the dialog size based on whether dropdowns are present
        float dialogWidth = stage.getWidth() * (hasDropdown ? 0.6f : 0.4f); // Wider if dropdowns exist
        float dialogHeight = stage.getHeight() * (hasDropdown ? 0.7f : 0.5f); // Taller if dropdowns exist

        // Create the dialog
        Dialog dialog = new Dialog("Database Points", skin);
        dialog.setSize(dialogWidth, dialogHeight);
        dialog.setPosition((stage.getWidth() - dialogWidth) / 2, (stage.getHeight() - dialogHeight) / 2); // Center the dialog

        // Wrap the table in a scroll pane only if dropdowns exist
        if (hasDropdown) {
            ScrollPane scrollPane = new ScrollPane(table, skin);
            scrollPane.setScrollingDisabled(true, false); // Allow only vertical scrolling
            dialog.getContentTable().add(scrollPane).expand().fill().pad(10);
        } else {
            dialog.getContentTable().add(table).expand().fill().pad(10);
        }

        // Add a close button to the dialog
        dialog.button("Close", true);

        // Show the dialog
        dialog.show(stage);
    }

*/




    private void showDatabasePointsDialog() {
        // Create a table to hold the points
        Table table = new Table();
        table.top().left();
        table.pad(5); // Reduced padding for compact layout

        boolean hasDropdown = false; // Flag to check if dropdowns exist

        // Add the points to the table
        for (int i = 0; i < databasePoints.size(); i++) {
            LocationInfo point = databasePoints.get(i);

            // Create a label for the point
            String label = "Point " + (i + 1) + ": " + point.getLocationName();
            Label pointLabel = new Label(label, skin);
            pointLabel.setColor(Color.BLACK); // Set the font color to black
            table.add(pointLabel).padBottom(2).fillX().row(); // Reduced spacing

            // Check if dropdowns are needed by checking for details
            boolean hasDetails = point.getConnectors() > 0 || point.getConnectorsAvailable() > 0;
            if (hasDetails) {
                hasDropdown = true;

                // Create a dropdown container for details
                Table dropdownContent = new Table();
                dropdownContent.setVisible(false); // Start with the dropdown hidden

                Label locationNameLabel = new Label("Location Name: " + point.getLocationName(), skin);
                locationNameLabel.setColor(Color.BLACK);
                dropdownContent.add(locationNameLabel).padBottom(2).fillX().row();

                Label latitudeLabel = new Label("Latitude: " + point.getLatitude(), skin);
                latitudeLabel.setColor(Color.BLACK);
                dropdownContent.add(latitudeLabel).padBottom(2).fillX().row();

                Label longitudeLabel = new Label("Longitude: " + point.getLongitude(), skin);
                longitudeLabel.setColor(Color.BLACK);
                dropdownContent.add(longitudeLabel).padBottom(2).fillX().row();

                Label connectorsLabel = new Label("Connectors: " + point.getConnectors(), skin);
                connectorsLabel.setColor(Color.BLACK);
                dropdownContent.add(connectorsLabel).padBottom(2).fillX().row();

                Label connectorsAvailableLabel = new Label("Connectors Available: " + point.getConnectorsAvailable(), skin);
                connectorsAvailableLabel.setColor(Color.BLACK);
                dropdownContent.add(connectorsAvailableLabel).padBottom(2).fillX().row();

                // Add click listener to toggle dropdown visibility
                pointLabel.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        dropdownContent.setVisible(!dropdownContent.isVisible()); // Toggle visibility
                        table.invalidate(); // Update the table layout
                    }
                });

                // Add dropdown content to the table
                table.add(dropdownContent).padLeft(15).padBottom(5).fillX().row();
            }

            // Add click listener to highlight the point
            final int index = i; // Capture the index of the point
            pointLabel.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Highlight the selected point
                    highlightSelectedPoint(index);

                    // Reset the highlight after a delay
                    scheduleHighlightReset();
                }
            });
        }

        // Dynamically determine the dialog size based on whether dropdowns are present
        float dialogWidth = stage.getWidth() * (hasDropdown ? 0.6f : 0.4f); // Wider if dropdowns exist
        float dialogHeight = stage.getHeight() * (hasDropdown ? 0.7f : 0.5f); // Taller if dropdowns exist

        // Create the dialog
        Dialog dialog = new Dialog("Database Points", skin);
        dialog.setSize(dialogWidth, dialogHeight);
        dialog.setPosition((stage.getWidth() - dialogWidth) / 2, (stage.getHeight() - dialogHeight) / 2); // Center the dialog

        // Wrap the table in a scroll pane only if dropdowns exist
        if (hasDropdown) {
            ScrollPane scrollPane = new ScrollPane(table, skin);
            scrollPane.setScrollingDisabled(true, false); // Allow only vertical scrolling
            dialog.getContentTable().add(scrollPane).expand().fill().pad(10);
        } else {
            dialog.getContentTable().add(table).expand().fill().pad(10);
        }

        // Add a close button to the dialog
        dialog.button("Close", true);

        // Show the dialog
        dialog.show(stage);
    }






    private void initializeSelectablePoints() {
        selectablePoints.clear();
        for (LocationInfo point : databasePoints) {
            selectablePoints.add(new SelectablePoint(point));
        }
        Gdx.app.log("SelectablePoints", "Total points: " + selectablePoints.size());
    }


    private void highlightSelectedPoint(int index) {
        // Reset previous highlights
        for (SelectablePoint selectablePoint : selectablePoints) {
            selectablePoint.isHighlighted = false;
        }

        // Set the selected point as highlighted
        if (index >= 0 && index < selectablePoints.size()) {
            selectablePoints.get(index).isHighlighted = true;
        }

        // Trigger a reset of the highlight after a brief moment (e.g., 2 seconds)
        scheduleHighlightReset();
    }

    // Function to reset the highlight after a delay
    private void scheduleHighlightReset() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Reset the highlight after the delay
                for (SelectablePoint selectablePoint : selectablePoints) {
                    selectablePoint.isHighlighted = false;
                }
            }
        }, 2);  // 2-second delay
    }


    private InputProcessor createInputProcessor() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                // Handle movement with arrow keys and zooming with '+' and '-'
                if (keycode == Input.Keys.LEFT) {
                    camera.translate(-MOVEMENT_SPEED, 0);
                } else if (keycode == Input.Keys.RIGHT) {
                    camera.translate(MOVEMENT_SPEED, 0);
                } else if (keycode == Input.Keys.UP) {
                    camera.translate(0, MOVEMENT_SPEED);
                } else if (keycode == Input.Keys.DOWN) {
                    camera.translate(0, -MOVEMENT_SPEED);
                } else if (keycode == Input.Keys.Q) {
                    camera.zoom -= ZOOM_SPEED;  // Zoom in
                } else if (keycode == Input.Keys.A) {
                    camera.zoom += ZOOM_SPEED;  // Zoom out
                }

                camera.update(); // Update the camera after zoom or movement change
                return false;
            }

        };
    }

    private void generateSemaforPoints() {
        semaforPoints.clear();
        if (routeCoordinates == null) return;

        double accumulatedDistance = 0.0;
        double nextSemaforDistance = SEMAFOR_INTERVAL_METERS;

        for (Geolocation[] segment : routeCoordinates) {
            if (segment.length < 2) continue;

            for (int i = 0; i < segment.length - 1; i++) {
                Geolocation start = segment[i];
                Geolocation end = segment[i + 1];
                double segmentDistance = Geolocation.distanceBetween(start, end);

                while (accumulatedDistance + segmentDistance >= nextSemaforDistance) {
                    double remainingDistance = nextSemaforDistance - accumulatedDistance;
                    double fraction = remainingDistance / segmentDistance;

                    // Interpolate to find the exact semafor position
                    double semaforLat = start.lat + fraction * (end.lat - start.lat);
                    double semaforLng = start.lng + fraction * (end.lng - start.lng);

                    semaforPoints.add(new Geolocation(semaforLat, semaforLng));

                    // Update for the next semafor
                    nextSemaforDistance += SEMAFOR_INTERVAL_METERS;

                    // Update the start point for the next iteration
                    start = new Geolocation(semaforLat, semaforLng);
                    segmentDistance -= remainingDistance;
                }

                accumulatedDistance += segmentDistance;
            }
        }
    }

    private void drawRoute() {
        if (routeCoordinates == null) return;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);

        // Draw the route as a series of lines between coordinates
        for (Geolocation[] segment : routeCoordinates) {
            if (segment.length < 2) continue;

            for (int i = 0; i < segment.length - 1; i++) {
                Geolocation start = segment[i];
                Geolocation end = segment[i + 1];
                Vector2 startPos = MapRasterTiles.getPixelPosition(start.lat, start.lng, beginTile.x, beginTile.y);
                Vector2 endPos = MapRasterTiles.getPixelPosition(end.lat, end.lng, beginTile.x, beginTile.y);

                shapeRenderer.line(startPos.x, startPos.y, endPos.x, endPos.y);
            }
        }

        shapeRenderer.end();

        // Draw semaphores
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        for (Geolocation semafor : semaforPoints) {
            Vector2 position = MapRasterTiles.getPixelPosition(semafor.lat, semafor.lng, beginTile.x, beginTile.y);
            shapeRenderer.circle(position.x, position.y, 5);
        }
        shapeRenderer.end();
    }

    private void drawMarkers() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // Draw the selectable (database) points
        for (int i = 0; i < selectablePoints.size(); i++) {
            SelectablePoint selectablePoint = selectablePoints.get(i);
            LocationInfo point = selectablePoint.geolocation;
            Vector2 position = MapRasterTiles.getPixelPosition(point.getLatitude(), point.getLongitude(), beginTile.x, beginTile.y);

            // Use a different color or size if the point is highlighted
            if (selectablePoint.isHighlighted) {
                spriteBatch.draw(startIcon, position.x - 12, position.y - 12, 24, 24);  // Larger icon for highlight
                font.setColor(Color.YELLOW);
            } else {
                spriteBatch.draw(startIcon, position.x - 8, position.y - 8, 16, 16);  // Normal size
                font.setColor(Color.BLUE);
            }
            font.draw(spriteBatch, point.getLocationName(), position.x + 10, position.y + 10);
        }

        // Draw the start point if available
        if (selectedPoints.size() >= 1) {
            Geolocation start = selectedPoints.get(0);
            Vector2 startPos = MapRasterTiles.getPixelPosition(start.lat, start.lng, beginTile.x, beginTile.y);
            spriteBatch.draw(startIcon, startPos.x - 16, startPos.y - 16, 32, 32);
            font.setColor(Color.GREEN);
            font.draw(spriteBatch, "Start", startPos.x + 20, startPos.y + 20);
        }

        // Draw the end point if available
        if (selectedPoints.size() == 2) {
            Geolocation end = selectedPoints.get(1);
            Vector2 endPos = MapRasterTiles.getPixelPosition(end.lat, end.lng, beginTile.x, beginTile.y);
            spriteBatch.draw(endIcon, endPos.x - 16, endPos.y - 16, 32, 32);
            font.setColor(Color.RED);
            font.draw(spriteBatch, "End", endPos.x + 20, endPos.y + 20);
        }

        spriteBatch.end();
    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        handleCursorChange();

        drawMarkers();

        if (routeReady && !showRouteOnly) {
            drawRoute();
        }

        stage.act(delta);
        stage.draw();
    }

    private Geolocation screenToGeo(float x, float y) {
        // Convert screen coordinates to geolocation (same logic as IgreProjekt)
        double tileSize = MapRasterTiles.TILE_SIZE;
        double scale = Math.pow(2, Constants.ZOOM);
        double pixelX = x + (beginTile.x * tileSize);
        double pixelY = (Constants.MAP_HEIGHT - y) + (beginTile.y * tileSize) - 1;
        double worldX = pixelX / scale;
        double worldY = pixelY / scale;
        double lng = ((worldX / tileSize) - 0.5) * 360.0;
        double xVal = 0.5 - (worldY / tileSize);
        double e = Math.exp(xVal * 4.0 * Math.PI);
        double siny = (e - 1.0) / (e + 1.0);
        double lat = Math.toDegrees(Math.asin(siny));
        return new Geolocation(lat, lng);
    }

    @Override
    public void resize(int width, int height) {
        // Update camera size
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        // Update the stage viewport
        stage.getViewport().update(width, height, true);

        float buttonWidth = width * 0.1f;  // 10% of new screen width
        float buttonHeight = height * 0.05f;  // 5% of new screen height

        // Clamp the button size
        buttonWidth = MathUtils.clamp(buttonWidth, 100f, 200f);
        buttonHeight = MathUtils.clamp(buttonHeight, 40f, 60f);

        // Set the size and position
        startButton.setSize(buttonWidth, buttonHeight);
        startButton.setPosition(5f, 5f);

        menuButton.setSize(buttonWidth, buttonHeight);
        menuButton.setPosition(width - menuButton.getWidth() - 5f, height - menuButton.getHeight() - 5f);
    }


    private void showEditPointDialog(SelectablePoint point, boolean isNewPoint) {
        Dialog editDialog = new Dialog(isNewPoint ? "Add New Point" : "Edit Point", skin);

        // Create fields for editing
        TextField nameField = new TextField(isNewPoint ? "New Point" : point.geolocation.getLocationName(), skin);
        TextField addressField = new TextField(isNewPoint ? "New Address" : point.geolocation.getAddress(), skin);
        TextField connectorsField = new TextField(String.valueOf(isNewPoint ? "" : point.geolocation.getConnectors()),skin);
        TextField connectorsAvailableField = new TextField(String.valueOf(isNewPoint ? "" : point.geolocation.getConnectorsAvailable()),skin);

        // Latitude and longitude for re-selection
        final double[] latitude = {point.geolocation.getLatitude()};
        final double[] longitude = {point.geolocation.getLongitude()};

        // Create a uniform layout without excessive padding
        Table contentTable = editDialog.getContentTable();

// Adjust the alignment, padding, and width for consistent layout
        Label nameLabel = new Label("Name: ", skin);
        nameLabel.setColor(Color.BLACK);
        contentTable.add(nameLabel).left().pad(5); // Add some consistent padding
        contentTable.add(nameField).width(200).fillX().pad(5).row();

        Label addressLabel = new Label("Address: ", skin);
        addressLabel.setColor(Color.BLACK);
        contentTable.add(addressLabel).left().pad(5).padRight(10);
        contentTable.add(addressField).width(200).fillX().pad(5).row();

        Label connectorsLabel = new Label("Connectors: ", skin);
        connectorsLabel.setColor(Color.BLACK);
        contentTable.add(connectorsLabel).left().pad(5).padRight(10);
        contentTable.add(connectorsField).width(200).fillX().pad(5).row();

        Label connectorsAvailableLabel = new Label("Connectors Available: ", skin);
        connectorsAvailableLabel.setColor(Color.BLACK);
        contentTable.add(connectorsAvailableLabel).left().pad(5).padRight(10);
        contentTable.add(connectorsAvailableField).width(200).fillX().pad(5).row();

// Add buttons to the dialog
        editDialog.button("Save", "SAVE");
        editDialog.button("Cancel", "CANCEL");
        editDialog.button("Reselect Location", "RESELECT_LOCATION");


        // Add a listener for the dialog result
        editDialog.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor instanceof TextButton) {
                    TextButton button = (TextButton) actor;
                    String buttonText = button.getText().toString();

                    if ("Save".equalsIgnoreCase(buttonText)) {
                        try {
                            // Get updated data from fields
                            String name = nameField.getText();
                            String address = addressField.getText();
                            int connectors = Integer.parseInt(connectorsField.getText());
                            int connectorsAvailable = Integer.parseInt(connectorsAvailableField.getText());

                            if (isNewPoint) {
                                // Add a new point to the database and list
                                LocationInfo newLocation = new LocationInfo(name, latitude[0], longitude[0], connectors, connectorsAvailable, address);
                                mongoDBExample.addPointToDatabase(newLocation, address); // Add new point to DB
                                databasePoints.add(newLocation); // Add to local list
                            } else {
                                // Update an existing point
                                Geolocation oldLocation = new Geolocation(
                                    point.geolocation.getLatitude(),
                                    point.geolocation.getLongitude()
                                );

                                LocationInfo updatedLocation = new LocationInfo(
                                    name, latitude[0], longitude[0], connectors, connectorsAvailable, address
                                );

                                mongoDBExample.updatePointInDatabase(oldLocation, updatedLocation);
                            }

                            databasePoints = mongoDBExample.fetchPointsFromDatabase();
                            initializeSelectablePoints();

                            Gdx.app.log("Dialog", isNewPoint ? "New point added." : "Point updated.");
                        } catch (Exception e) {
                            Gdx.app.log("Dialog", "Error saving point: " + e.getMessage());
                        } finally {
                            editDialog.hide(); // Close dialog after save
                        }
                    } else if ("Cancel".equalsIgnoreCase(buttonText)) {
                        editDialog.hide();
                    } else if ("Reselect Location".equalsIgnoreCase(buttonText)) {
                        editDialog.hide(); // Close dialog for re-selection

                        // Add a temporary listener to capture new location
                        InputAdapter locationSelector = new InputAdapter() {
                            @Override
                            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                                Vector3 clickPosition = camera.unproject(new Vector3(screenX, screenY, 0));
                                Geolocation geoClicked = screenToGeo(clickPosition.x, clickPosition.y);

                                // Compare old and new coordinates
                                double oldLatitude = point.geolocation.getLatitude();
                                double oldLongitude = point.geolocation.getLongitude();
                                double newLatitude = geoClicked.lat;
                                double newLongitude = geoClicked.lng;

                                if (Double.compare(oldLatitude, newLatitude) == 0 && Double.compare(oldLongitude, newLongitude) == 0) {
                                    Gdx.app.log("LocationSelector", "New coordinates are the same as old coordinates.");
                                } else {
                                    Gdx.app.log("LocationSelector", "Coordinates changed. Old: (" + oldLatitude + ", " + oldLongitude +
                                        "), New: (" + newLatitude + ", " + newLongitude + ")");

                                    // Update the point's geolocation
                                    point.geolocation.setLatitude(newLatitude);
                                    point.geolocation.setLongitude(newLongitude);

                                    // Call the update method to save the new location in the database
                                    // Here, you will need to pass the old location (or some identifier) and the new point to update it
                                    Geolocation oldGeo = new Geolocation(oldLatitude, oldLongitude);
                                    LocationInfo newLocation = new LocationInfo(
                                        nameField.getText(),// Assuming point has a locationName
                                        newLatitude,             // New Latitude
                                        newLongitude,            // New Longitude
                                        Integer.parseInt(connectorsField.getText()),   // Assuming point has connectors data
                                        Integer.parseInt(connectorsAvailableField.getText()),// Assuming point has connectorsAvailable data
                                        addressField.getText()    // Assuming point has an address
                                    );

                                    mongoDBExample.updatePointInDatabase(oldGeo,newLocation);

                                    // Reopen the dialog with updated coordinates
                                    showEditPointDialog(point, isNewPoint);
                                }

                                // Remove the temporary input processor
                                Gdx.input.setInputProcessor(stage);
                                return true;
                            }
                        };

                        // Temporarily set input processor for location selection
                        Gdx.input.setInputProcessor(locationSelector);
                        databasePoints = mongoDBExample.fetchPointsFromDatabase();
                        initializeSelectablePoints();
                    }

                }
            }
        });

        editDialog.show(stage);
    }





    private void handleCursorChange() {
        // Get the current mouse position
        Vector3 touchPosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPosition); // Convert to world coordinates

        // Check if the mouse is over a point
        SelectablePoint closestPoint = findClosestSelectablePoint(new Geolocation(touchPosition.x, touchPosition.y));

        if (closestPoint != null) {
            // Change cursor to hand pointer when hovering over a point
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
        } else {
            // Revert to default cursor when not hovering over a point
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
    }


    private SelectablePoint findClosestSelectablePoint(Geolocation clickedPoint) {
        SelectablePoint closestPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (SelectablePoint point : selectablePoints) {
            double distance = Geolocation.distanceBetween(
                clickedPoint,
                new Geolocation(point.geolocation.getLatitude(), point.geolocation.getLongitude())
            );

            if (distance < CLICK_THRESHOLD_DISTANCE) { // Define a small threshold
                minDistance = distance;
                closestPoint = point;
            }
        }

        return closestPoint;
    }

    private void showMenuDialog() {
        // Create a dialog for the menu
        Dialog menuDialog = new Dialog("", skin);

        // Title Label (centered)
        Label titleLabel = new Label("Menu", skin);
        titleLabel.setAlignment(Align.center);
        menuDialog.getTitleTable().add(titleLabel).expandX().center();

        // Close Button
        TextButton closeButton = new TextButton("X", skin);
        closeButton.setSize(40f, 40f); // Size for the close button
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                menuDialog.hide(); // Close the dialog
            }
        });

        // Add close button to the title table (top-right)
        menuDialog.getTitleTable().add(closeButton).padRight(10f).top().right();

        // Add buttons for Settings, Leaderboard, and Quit
        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Menu Dialog", "Settings clicked!");
                menuDialog.hide();
                //showSettings(); // Open the settings screen or logic
                game.setScreen(new SettingsScreen(game));
            }
        });

        TextButton leaderboardButton = new TextButton("Leaderboard", skin);
        leaderboardButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Menu Dialog", "Leaderboard clicked!");
                menuDialog.hide();
                game.setScreen(new LeaderboardScreen(game));

            }
        });

        TextButton quitButton = new TextButton("Quit", skin);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Menu Dialog", "Quit clicked!");
                menuDialog.hide();
                Gdx.app.exit(); // Quit the game
            }
        });

        // Add buttons to the dialog content table
        menuDialog.getContentTable().pad(10);
        menuDialog.getContentTable().add(settingsButton).pad(10).fillX().row();
        menuDialog.getContentTable().add(leaderboardButton).pad(10).fillX().row();
        menuDialog.getContentTable().add(quitButton).pad(10).fillX().row();

        // Show the dialog
        menuDialog.show(stage);
    }




    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
        stage.dispose();
        skin.dispose();
        if (mongoDBExample != null) {
            mongoDBExample.closeConnection();
        }
    }
}
