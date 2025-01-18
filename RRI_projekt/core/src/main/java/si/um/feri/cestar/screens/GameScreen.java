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


    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;


    private Vector3 touchPosition;
    private Geolocation[][] routeCoordinates;
    private List<Geolocation> selectedPoints = new ArrayList<>();
    private List<Geolocation> semaforPoints = new ArrayList<>();


    private TextureRegion startIcon;
    private TextureRegion endIcon;
    private TextureRegion markerIcon;
    private BitmapFont font;
    private Texture[] mapTiles;
    private ZoomXY beginTile;

    private Stage stage;
    private Skin skin;
    private TextButton startButton;
    private TextButton menuButton;

    private boolean routeReady = false;
    private boolean showRouteOnly = false;
    private static final double SEMAFOR_INTERVAL_METERS = 80.0;
    MongoDB mongoDBExample;
    private List<LocationInfo> databasePoints = new ArrayList<>();


    private static final float MOVEMENT_SPEED = 10f;
    private static final float ZOOM_SPEED = 0.5f;

    private List<SelectablePoint> selectablePoints = new ArrayList<>();
    private boolean isEditingMode;
    public static final double CLICK_THRESHOLD_DISTANCE = 50.0;
    InputMultiplexer inputMultiplexer;



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


        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);


        camera.viewportWidth = Gdx.graphics.getWidth();
        camera.viewportHeight = Gdx.graphics.getHeight();
        camera.update();


        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        touchPosition = new Vector3();


        font = new BitmapFont();
        font.getData().setScale(1.5f);

        TextureAtlas gameplayAtlas = assetManager.get(AssetsDescriptor.GAMEPLAY);
        startIcon = gameplayAtlas.findRegion(RegionNames.START);
        endIcon = gameplayAtlas.findRegion(RegionNames.FINISH);
        markerIcon = gameplayAtlas.findRegion(RegionNames.MARKER);



        stage = new Stage(new ScreenViewport());
        skin = assetManager.get(AssetsDescriptor.UI_SKIN);




        startButton = new TextButton("Start", skin);


        float buttonWidth = 150f;
        float buttonHeight = 50f;


        buttonWidth = MathUtils.clamp(buttonWidth, 100f, 200f);
        buttonHeight = MathUtils.clamp(buttonHeight, 40f, 60f);


        startButton.setSize(buttonWidth, buttonHeight);


        float margin = 10f;
        startButton.setPosition(margin, margin);


        startButton.setDisabled(true);


        databasePoints = mongoDBExample.fetchPointsFromDatabase();
        initializeSelectablePoints();

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedPoints.size() == 2) {

                    Dialog nameInputDialog = new Dialog("Enter Your Name", skin);
                    nameInputDialog.getTitleLabel().setAlignment(Align.center);


                    TextField nameField = new TextField("", skin);
                    nameInputDialog.getContentTable().center();
                    nameInputDialog.getContentTable().add("Name: ").left();
                    nameInputDialog.getContentTable().add(nameField).width(200).row();


                    nameInputDialog.button("Enter", true);
                    nameInputDialog.button("Cancel", false);


                    nameInputDialog.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            if (actor instanceof TextButton) {
                                TextButton button = (TextButton) actor;
                                String buttonText = button.getText().toString();

                                if ("Enter".equalsIgnoreCase(buttonText)) {
                                    try {

                                        routeCoordinates = MapRasterTiles.fetchPath(new Geolocation[]{
                                            selectedPoints.get(0),
                                            selectedPoints.get(1)
                                        });

                                        Gdx.app.log("Route Coordinates", Arrays.toString(routeCoordinates));


                                        String playerName = nameField.getText();
                                        if (playerName.isEmpty()) {
                                            Gdx.app.log("Name Input", "Name cannot be empty!");
                                            return;
                                        }


                                        GameManager.getInstance().setCurrentUser(playerName);


                                        generateSemaforPoints();

                                        if (semaforPoints.isEmpty()) {

                                            selectedPoints.clear();
                                            routeCoordinates = null;
                                            routeReady = false;
                                            startButton.setDisabled(true);


                                            Dialog warningDialog = new Dialog("Selected route is too short", skin);

                                            Label warningLabel = new Label("You need to generate at least one semafor point", skin);
                                            warningLabel.setColor(Color.BLACK);

                                            warningDialog.getContentTable().add(warningLabel).pad(10).row();
                                            warningDialog.button("OK");
                                            warningDialog.show(stage);



                                            Gdx.app.log("Semafor Generation", "No semafor points generated! Points have been reset.");
                                            return;
                                        }

                                        routeReady = true;


                                        Timer.schedule(new Timer.Task() {
                                            @Override
                                            public void run() {
                                                game.setScreen(new DetailedRouteScreen(game, routeCoordinates, semaforPoints));
                                            }
                                        }, 2);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if ("Cancel".equalsIgnoreCase(buttonText)) {

                                    selectedPoints.clear();
                                    startButton.setDisabled(true);
                                    routeCoordinates = null;
                                    routeReady = false;
                                    Gdx.app.log("Name Input", "Selection canceled.");
                                }
                            }
                        }
                    });


                    nameInputDialog.show(stage);
                } else {
                    Gdx.app.log("Start Button", "Not enough points selected!");
                }
            }
        });


        //TextButton showDBPointsButton = new TextButton("Show DB Points", skin);


        float dbButtonWidth = Gdx.graphics.getWidth() * 0.2f;
        float dbButtonHeight = Gdx.graphics.getHeight() * 0.05f;
        dbButtonWidth = MathUtils.clamp(dbButtonWidth, 100f, 200f);
        dbButtonHeight = MathUtils.clamp(dbButtonHeight, 40f, 60f);


        //showDBPointsButton.setSize(dbButtonWidth, dbButtonHeight);
        //showDBPointsButton.setPosition(margin, margin + startButton.getHeight() + 10);


        /*showDBPointsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("GameScreen", "Show DB Points button clicked.");
                showDatabasePointsDialog();
            }
        });*/

        TextButton toggleEditModeButton = new TextButton("Edit Mode: OFF", skin);


        float toggleWidth = Gdx.graphics.getWidth() * 0.2f;
        float toggleHeight = Gdx.graphics.getHeight() * 0.05f;
        toggleWidth = MathUtils.clamp(toggleWidth, 100f, 200f);
        toggleHeight = MathUtils.clamp(toggleHeight, 40f, 60f);
        toggleEditModeButton.setSize(toggleWidth, toggleHeight);
        //toggleEditModeButton.setPosition(margin, margin + startButton.getHeight() + showDBPointsButton.getHeight() + 20);
        toggleEditModeButton.setPosition(margin, margin + startButton.getHeight() + 20);


        toggleEditModeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isEditingMode = !isEditingMode;
                toggleEditModeButton.setText("Edit Mode: " + (isEditingMode ? "ON" : "OFF"));
                Gdx.app.log("GameScreen", "Editing mode: " + isEditingMode);
            }
        });


        menuButton = new TextButton("Menu", skin);


        float buttonWidthMenu = 150f;
        float buttonHeightMenu = 50f;


        buttonWidthMenu = MathUtils.clamp(buttonWidthMenu, 100f, 200f);
        buttonHeightMenu = MathUtils.clamp(buttonHeightMenu, 40f, 60f);


        menuButton.setSize(buttonWidthMenu, buttonHeightMenu);
        menuButton.setPosition(margin,margin);


        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenuDialog();
            }
        });



        stage.addActor(toggleEditModeButton);
        stage.addActor(menuButton);

        stage.addActor(startButton);
        //stage.addActor(showDBPointsButton);

        GestureDetector gestureDetector = new GestureDetector(new GestureDetector.GestureAdapter() {
            @Override
            public boolean tap(float x, float y, int count, int button) {
                touchPosition.set(x, y, 0);
                camera.unproject(touchPosition);

                Geolocation geoClicked = screenToGeo(touchPosition.x, touchPosition.y);

                if (isEditingMode) {


                    SelectablePoint closestPoint = findClosestSelectablePoint(geoClicked);
                    if (closestPoint != null) {

                        showEditPointDialog(closestPoint, false);
                    } else {

                        SelectablePoint newPoint = new SelectablePoint(
                            new LocationInfo("New Point", geoClicked.lat, geoClicked.lng, 0, 0, "New Address")
                        );
                        showEditPointDialog(newPoint, true);

                    }
                } else {

                    if (selectedPoints.size() < 2) {

                        selectedPoints.add(geoClicked);

                        if (selectedPoints.size() == 2) {

                            startButton.setDisabled(false);
                        }
                    } else {

                        selectedPoints.clear();
                        startButton.setDisabled(true);
                    }
                }

                return true;
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {

                camera.translate(-deltaX, deltaY);
                return false;
            }

            @Override
            public boolean zoom(float initialDistance, float distance) {

                if (initialDistance > distance) {
                    camera.zoom += ZOOM_SPEED;
                } else {
                    camera.zoom -= ZOOM_SPEED;
                }


                if (camera.zoom < 0.1f) {
                    camera.zoom = 0.1f;
                } else if (camera.zoom > 3f) {
                    camera.zoom = 3f;
                }

                camera.update();
                return true;
            }

        });


        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(gestureDetector);
        inputMultiplexer.addProcessor(createInputProcessor());

        Gdx.input.setInputProcessor(inputMultiplexer);


        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Constants.ZOOM);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);
            beginTile = new ZoomXY(Constants.ZOOM,
                centerTile.x - ((Constants.NUM_TILES - 1) / 2),
                centerTile.y - ((Constants.NUM_TILES - 1) / 2));


            routeCoordinates = MapRasterTiles.fetchPath(new Geolocation[]{CENTER_GEOLOCATION, MARKER_GEOLOCATION});
        } catch (Exception e) {
            e.printStackTrace();
        }


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


    private void initializeSelectablePoints() {
        selectablePoints.clear();
        for (LocationInfo point : databasePoints) {
            selectablePoints.add(new SelectablePoint(point));
        }
        Gdx.app.log("SelectablePoints", "Total points: " + selectablePoints.size());
    }

    private InputProcessor createInputProcessor() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {

                if (keycode == Input.Keys.LEFT) {
                    camera.translate(-MOVEMENT_SPEED, 0);
                } else if (keycode == Input.Keys.RIGHT) {
                    camera.translate(MOVEMENT_SPEED, 0);
                } else if (keycode == Input.Keys.UP) {
                    camera.translate(0, MOVEMENT_SPEED);
                } else if (keycode == Input.Keys.DOWN) {
                    camera.translate(0, -MOVEMENT_SPEED);
                } else if (keycode == Input.Keys.Q) {
                    camera.zoom -= ZOOM_SPEED;
                } else if (keycode == Input.Keys.A) {
                    camera.zoom += ZOOM_SPEED;
                }

                camera.update();
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


                    double semaforLat = start.lat + fraction * (end.lat - start.lat);
                    double semaforLng = start.lng + fraction * (end.lng - start.lng);

                    semaforPoints.add(new Geolocation(semaforLat, semaforLng));


                    nextSemaforDistance += SEMAFOR_INTERVAL_METERS;


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

        if (selectedPoints.size() >= 1) {
            Geolocation start = selectedPoints.get(0);
            Vector2 startPos = MapRasterTiles.getPixelPosition(start.lat, start.lng, beginTile.x, beginTile.y);
            spriteBatch.draw(startIcon, startPos.x - 16, startPos.y - 16, 32, 32);
            font.setColor(Color.GREEN);
            font.draw(spriteBatch, "Start", startPos.x + 20, startPos.y + 20);
        }


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

        drawChargePoints();

        stage.act(delta);
        stage.draw();
    }

    private void drawChargePoints() {
        if (databasePoints.isEmpty()) return;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (LocationInfo point : databasePoints) {

            Vector2 position = MapRasterTiles.getPixelPosition(
                point.getLatitude(),
                point.getLongitude(),
                beginTile.x,
                beginTile.y
            );


            float radius = 20;
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.circle(position.x, position.y, radius);


            float availableRatio = (float) point.getConnectorsAvailable() / point.getConnectors();
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.arc(position.x, position.y, radius, 90, availableRatio * 360);
        }

        shapeRenderer.end();


        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        for (LocationInfo point : databasePoints) {
            Vector2 position = MapRasterTiles.getPixelPosition(
                point.getLatitude(),
                point.getLongitude(),
                beginTile.x,
                beginTile.y
            );

            font.setColor(Color.BLACK);
            font.draw(spriteBatch, point.getLocationName(), position.x - 24 + 1, position.y + 30 - 1);


            font.setColor(Color.LIGHT_GRAY);
            font.getData().setScale(1.3f);
            font.draw(spriteBatch, point.getLocationName(), position.x - 24, position.y + 30);
        }
        spriteBatch.end();
    }


    private Geolocation screenToGeo(float x, float y) {

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

        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();


        stage.getViewport().update(width, height, true);

        float buttonWidth = width * 0.1f;
        float buttonHeight = height * 0.05f;


        buttonWidth = MathUtils.clamp(buttonWidth, 100f, 200f);
        buttonHeight = MathUtils.clamp(buttonHeight, 40f, 60f);


        startButton.setSize(buttonWidth, buttonHeight);
        startButton.setPosition(5f, 5f);

        menuButton.setSize(buttonWidth, buttonHeight);
        menuButton.setPosition(width - menuButton.getWidth() - 5f, height - menuButton.getHeight() - 5f);
    }


    private void showEditPointDialog(SelectablePoint point, boolean isNewPoint) {
        Dialog editDialog = new Dialog(isNewPoint ? "Add New Point" : "Edit Point", skin);
        editDialog.getTitleLabel().setAlignment(Align.center);

        TextField nameField = new TextField(isNewPoint ? "" : point.geolocation.getLocationName(), skin);
        TextField addressField = new TextField(isNewPoint ? "" : point.geolocation.getAddress(), skin);
        TextField connectorsField = new TextField(String.valueOf(isNewPoint ? "" : point.geolocation.getConnectors()),skin);
        TextField connectorsAvailableField = new TextField(String.valueOf(isNewPoint ? "" : point.geolocation.getConnectorsAvailable()),skin);


        final double[] latitude = {point.geolocation.getLatitude()};
        final double[] longitude = {point.geolocation.getLongitude()};


        Table contentTable = editDialog.getContentTable();


        Label nameLabel = new Label("Name: ", skin);
        nameLabel.setColor(Color.BLACK);
        contentTable.add(nameLabel).left().pad(5);
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


        editDialog.button("Save", "SAVE");
        editDialog.button("Cancel", "CANCEL");
        editDialog.button("Reselect Location", "RESELECT_LOCATION");



        editDialog.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor instanceof TextButton) {
                    TextButton button = (TextButton) actor;
                    String buttonText = button.getText().toString();

                    if ("Save".equalsIgnoreCase(buttonText)) {
                        try {

                            String name = nameField.getText();
                            String address = addressField.getText();
                            int connectors = Integer.parseInt(connectorsField.getText());
                            int connectorsAvailable = Integer.parseInt(connectorsAvailableField.getText());

                            if (isNewPoint) {

                                LocationInfo newLocation = new LocationInfo(name, latitude[0], longitude[0], connectors, connectorsAvailable, address);
                                mongoDBExample.addPointToDatabase(newLocation, address);
                                databasePoints.add(newLocation);
                            } else {

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
                            editDialog.hide();
                        }
                    } else if ("Cancel".equalsIgnoreCase(buttonText)) {
                        editDialog.hide();
                    } else if ("Reselect Location".equalsIgnoreCase(buttonText)) {
                        editDialog.hide();


                        InputAdapter locationSelector = new InputAdapter() {
                            @Override
                            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                                Vector3 clickPosition = camera.unproject(new Vector3(screenX, screenY, 0));
                                Geolocation geoClicked = screenToGeo(clickPosition.x, clickPosition.y);


                                double oldLatitude = point.geolocation.getLatitude();
                                double oldLongitude = point.geolocation.getLongitude();
                                double newLatitude = geoClicked.lat;
                                double newLongitude = geoClicked.lng;

                                if (Double.compare(oldLatitude, newLatitude) == 0 && Double.compare(oldLongitude, newLongitude) == 0) {
                                    Gdx.app.log("LocationSelector", "New coordinates are the same as old coordinates.");
                                } else {
                                    Gdx.app.log("LocationSelector", "Coordinates changed. Old: (" + oldLatitude + ", " + oldLongitude +
                                        "), New: (" + newLatitude + ", " + newLongitude + ")");


                                    point.geolocation.setLatitude(newLatitude);
                                    point.geolocation.setLongitude(newLongitude);



                                    Geolocation oldGeo = new Geolocation(oldLatitude, oldLongitude);
                                    LocationInfo newLocation = new LocationInfo(
                                        nameField.getText(),
                                        newLatitude,
                                        newLongitude,
                                        Integer.parseInt(connectorsField.getText()),
                                        Integer.parseInt(connectorsAvailableField.getText()),
                                        addressField.getText()
                                    );

                                    mongoDBExample.updatePointInDatabase(oldGeo,newLocation);


                                    showEditPointDialog(point, isNewPoint);
                                }


                                /*Gdx.input.setInputProcessor(stage);*/
                                Gdx.input.setInputProcessor(inputMultiplexer);
                                return true;
                            }
                        };


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

        Vector3 touchPosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPosition);


        SelectablePoint closestPoint = findClosestSelectablePoint(new Geolocation(touchPosition.x, touchPosition.y));

        if (closestPoint != null) {

            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
        } else {

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

            if (distance < CLICK_THRESHOLD_DISTANCE) {
                minDistance = distance;
                closestPoint = point;
            }
        }

        return closestPoint;
    }

    private void showMenuDialog() {

        Dialog menuDialog = new Dialog("", skin);


        Label titleLabel = new Label("Menu", skin);
        titleLabel.setAlignment(Align.center);
        menuDialog.getTitleTable().add(titleLabel).expandX().center();


        TextButton closeButton = new TextButton("X", skin);
        closeButton.setSize(40f, 40f);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                menuDialog.hide();
            }
        });


        menuDialog.getTitleTable().add(closeButton).padRight(10f).top().right();


        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Menu Dialog", "Settings clicked!");
                menuDialog.hide();

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
                Gdx.app.exit();
            }
        });


        menuDialog.getContentTable().pad(10);
        menuDialog.getContentTable().add(settingsButton).pad(10).fillX().row();
        menuDialog.getContentTable().add(leaderboardButton).pad(10).fillX().row();
        menuDialog.getContentTable().add(quitButton).pad(10).fillX().row();


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
