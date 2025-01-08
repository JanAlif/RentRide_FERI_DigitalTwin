package si.um.feri.cestar;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Logger;

import si.um.feri.cestar.screens.IntroScreen;

/*

public class IgreProjekt extends ApplicationAdapter implements GestureDetector.GestureListener {


    private ShapeRenderer shapeRenderer;
    private Vector3 touchPosition;
    private Geolocation[][] routeCoordinates;
    private List<Geolocation> selectedPoints = new ArrayList<>();
    private List<Geolocation> semaforPoints = new ArrayList<>(); // List of semafor points
    private final Random random = new Random();
    private TextureAtlas gameplayAtlas;
    private AssetManager assetManager;
    private SpriteBatch spriteBatch;


    private TextureRegion startIcon;
    private TextureRegion endIcon;
    private BitmapFont font;



    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private OrthographicCamera camera;

    private Texture[] mapTiles;
    private ZoomXY beginTile;   // top left tile

    // center geolocation
    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.557314, 15.637771);

    // test marker
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);


    @Override
    public void create() {
        assetManager = new AssetManager();
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        assetManager.load(AssetsDescriptor.GAMEPLAY);
        assetManager.finishLoading();

        File cacheDir = new File(MapRasterTiles.cacheDirectory);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.viewportWidth = Constants.MAP_WIDTH / 2f;
        camera.viewportHeight = Constants.MAP_HEIGHT / 2f;
        camera.zoom = 2f;
        camera.update();

        touchPosition = new Vector3();

        gameplayAtlas = assetManager.get(AssetsDescriptor.GAMEPLAY);

        startIcon = gameplayAtlas.findRegion(RegionNames.START);
        endIcon = gameplayAtlas.findRegion(RegionNames.FINISH);

        font = new BitmapFont();
        font.getData().setScale(1.5f);



        try {
            // 1) Fetch & set up our map tiles
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Constants.ZOOM);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);
            beginTile = new ZoomXY(Constants.ZOOM,
                centerTile.x - ((Constants.NUM_TILES - 1) / 2),
                centerTile.y - ((Constants.NUM_TILES - 1) / 2));

            // 2) Fetch the route between two geolocations
            //    Example: from CENTER_GEOLOCATION to MARKER_GEOLOCATION
            routeCoordinates = MapRasterTiles.fetchPath(new Geolocation[]{
                CENTER_GEOLOCATION,
                MARKER_GEOLOCATION
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Build the LibGDX TiledMap from the fetched tile images
        tiledMap = new TiledMap();
        MapLayers layers = tiledMap.getLayers();
        TiledMapTileLayer layer = new TiledMapTileLayer(
            Constants.NUM_TILES,
            Constants.NUM_TILES,
            MapRasterTiles.TILE_SIZE,
            MapRasterTiles.TILE_SIZE);

        int index = 0;
        for (int j = Constants.NUM_TILES - 1; j >= 0; j--) {
            for (int i = 0; i < Constants.NUM_TILES; i++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(
                    new TextureRegion(
                        mapTiles[index],
                        MapRasterTiles.TILE_SIZE,
                        MapRasterTiles.TILE_SIZE
                    )
                ));
                layer.setCell(i, j, cell);
                index++;
            }
        }
        layers.add(layer);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        Gdx.input.setInputProcessor(new GestureDetector(this));
    }


    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();
        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        // 1) Draw your marker(s) as before
        drawMarkers();

        // 2) Draw the route if it exists
        drawRoute();
    }

    private void drawRoute() {
        if (routeCoordinates == null) return;


        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLUE);

        for (Geolocation[] segment : routeCoordinates) {
            if (segment.length < 2) continue;
            for (int i = 0; i < segment.length - 1; i++) {
                Geolocation start = segment[i];
                Geolocation end = segment[i + 1];

                Vector2 startPos = MapRasterTiles.getPixelPosition(
                    start.lat, start.lng, beginTile.x, beginTile.y
                );
                Vector2 endPos = MapRasterTiles.getPixelPosition(
                    end.lat, end.lng, beginTile.x, beginTile.y
                );

                shapeRenderer.line(startPos.x, startPos.y, endPos.x, endPos.y);
            }
        }

        shapeRenderer.end();

        // Draw semafor points
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.YELLOW);
        for (Geolocation semafor : semaforPoints) {
            Vector2 position = MapRasterTiles.getPixelPosition(
                semafor.lat, semafor.lng, beginTile.x, beginTile.y
            );
            shapeRenderer.circle(position.x, position.y, 10);
        }
        shapeRenderer.end();


    }

    private void generateSemaforPoints() {
        semaforPoints.clear();

        if (routeCoordinates == null) return;

        for (Geolocation[] segment : routeCoordinates) {
            if (segment.length < 2) continue;
            for (int i = 0; i < segment.length - 1; i++) {
                if (random.nextDouble() < 0.3) { // 30% chance to place a semafor
                    double lat = (segment[i].lat + segment[i + 1].lat) / 2;
                    double lng = (segment[i].lng + segment[i + 1].lng) / 2;
                    semaforPoints.add(new Geolocation(lat, lng));
                }
            }
        }
    }
    private void drawMarkers() {
        if (selectedPoints.isEmpty()) return;

        spriteBatch.setProjectionMatrix(camera.combined); // Align batch with camera

        spriteBatch.begin();

        // Draw start marker
        if (selectedPoints.size() >= 1) {
            Geolocation start = selectedPoints.get(0);
            Vector2 startPos = MapRasterTiles.getPixelPosition(
                start.lat, start.lng, beginTile.x, beginTile.y
            );

            // Draw start icon
            spriteBatch.draw(startIcon, startPos.x - 16, startPos.y - 16, 32, 32); // Icon size 32x32

            // Draw start label
            font.setColor(Color.GREEN);
            font.draw(spriteBatch, "Start", startPos.x + 20, startPos.y + 20);
        }

        // Draw end marker
        if (selectedPoints.size() == 2) {
            Geolocation end = selectedPoints.get(1);
            Vector2 endPos = MapRasterTiles.getPixelPosition(
                end.lat, end.lng, beginTile.x, beginTile.y
            );

            // Draw end icon
            spriteBatch.draw(endIcon, endPos.x - 16, endPos.y - 16, 32, 32); // Icon size 32x32

            // Draw end label
            font.setColor(Color.RED);
            font.draw(spriteBatch, "End", endPos.x + 20, endPos.y + 20);
        }

        spriteBatch.end();
    }


    @Override
    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        spriteBatch.dispose();
    }


    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        touchPosition.set(x, y, 0);
        camera.unproject(touchPosition);
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        touchPosition.set(x, y, 0);
        camera.unproject(touchPosition);

        // Convert to lat/lon
        Geolocation geoClicked = screenToGeo(touchPosition.x, touchPosition.y);
        selectedPoints.add(geoClicked);

        // If we already had 2 points, reset them before we add the new point
        // so we only ever keep track of the last pair
        if (selectedPoints.size() > 2) {
            routeCoordinates = null;        // stop drawing the old route
            selectedPoints.clear();         // clear old points
            selectedPoints.add(geoClicked); // re-add the newly clicked point
        }

        // If we have 2 points, fetch a new route
        if (selectedPoints.size() == 2) {
            try {
                routeCoordinates = MapRasterTiles.fetchPath(new Geolocation[]{
                    selectedPoints.get(0),
                    selectedPoints.get(1)
                });
                generateSemaforPoints();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }



    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        camera.translate(-deltaX, deltaY);
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (initialDistance >= distance)
            camera.zoom += 0.02;
        else
            camera.zoom -= 0.02;
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.zoom += 0.01;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= 0.01;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3, 0);
        }

        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 2f);

        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, Constants.MAP_WIDTH - effectiveViewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, Constants.MAP_HEIGHT - effectiveViewportHeight / 2f);
    }

    private Geolocation screenToGeo(float x, float y) {
        // 1) Figure out how many pixels offset from the "beginTile" (top-left tile)
        //    because we basically did the reverse of getPixelPosition(...).
        double tileSize = MapRasterTiles.TILE_SIZE;
        double scale = Math.pow(2, Constants.ZOOM);

        // In your getPixelPosition, the formula was roughly:
        //   pixelX = floor(worldX * scale) - (beginTile.x * tileSize)
        //   pixelY = <some math involving MAP_HEIGHT, etc.>
        //
        // So to invert that, we add back (beginTile.x * tileSize), etc.
        double pixelX = x + (beginTile.x * tileSize);
        double pixelY = (Constants.MAP_HEIGHT - y) + (beginTile.y * tileSize) - 1;

        // 2) "worldX" and "worldY" are the scaled coordinates from MapRasterTiles.project(...)
        double worldX = pixelX / scale;
        double worldY = pixelY / scale;

        // 3) Reverse of MapRasterTiles.project(lat, lng, tileSize):
        //
        //    worldX = tileSize * (0.5 + (lng / 360))
        //    worldY = tileSize * (0.5 - ln((1 + siny)/(1 - siny)) / (4 * PI))
        //
        // => Solve for lng:
        //    (worldX / tileSize) - 0.5 = lng / 360
        //    lng = [ (worldX / tileSize) - 0.5 ] * 360
        double lng = ((worldX / tileSize) - 0.5) * 360.0;

        // => Solve for lat:
        //    (worldY / tileSize) - 0.5 = -ln((1 + siny)/(1 - siny)) / (4 * PI)
        //    Let xVal = 0.5 - (worldY / tileSize)
        //    Then:  xVal = ln((1 + siny)/(1 - siny)) / (4 * PI)
        //    e = exp(xVal * 4 * PI) => e = (1 + siny)/(1 - siny)
        //    => e * (1 - siny) = 1 + siny
        //    => e - 1 = siny + e*siny = siny(1 + e)
        //    => siny = (e - 1) / (e + 1)
        //
        double xVal = 0.5 - (worldY / tileSize);
        double e = Math.exp(xVal * 4.0 * Math.PI);
        double siny = (e - 1.0) / (e + 1.0);
        double lat = Math.toDegrees(Math.asin(siny));

        return new Geolocation(lat, lng);
    }


}
*/

public class IgreProjekt extends Game {

    private AssetManager assetManager;
    private SpriteBatch batch;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        assetManager = new AssetManager();
        assetManager.getLogger().setLevel(Logger.DEBUG);

        batch = new SpriteBatch();

        setScreen(new IntroScreen(this));

    }

    @Override
    public void dispose() {
        assetManager.dispose();
        batch.dispose();
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public SpriteBatch getBatch() {
        return batch;
    }


}
