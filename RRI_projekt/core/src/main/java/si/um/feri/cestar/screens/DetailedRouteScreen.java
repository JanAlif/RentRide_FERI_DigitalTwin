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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import si.um.feri.cestar.IgreProjekt;
import si.um.feri.cestar.Utils.Car;
import si.um.feri.cestar.Utils.Confetti;
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


    private Texture[] mapTiles;
    private ZoomXY beginTile;


    private double minLat, maxLat;
    private double minLng, maxLng;
    private Car car;
    private Stage stage;

    private boolean isCameraFollowingCar = true;
    private boolean isPausedAtTrafficLight = false;

    private Vector3 savedCarPosition = null;
    private int savedCarIndex = 0;
    private Set<Geolocation> processedTrafficLights = new HashSet<>();
    MongoDB mongoDBExample;
    private boolean hasFinished = false;
    private final AssetManager assetManager;
    private List<Confetti> confettiList;
    private boolean showConfetti = false;




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


        camera = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f;
        camera.far = 2000f;
        skin = assetManager.get(AssetsDescriptor.UI_SKIN);



        computeBoundingBox();

        if (savedCarPosition != null && savedCarIndex >= 0) {
            car.setPosition(savedCarPosition);
            car.setCurrentPointIndex(savedCarIndex);
        }


        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        modelBatch = new ModelBatch();





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


            Vector2 carStartPos = MapRasterTiles.getPixelPosition(
                startPoint.lat, startPoint.lng, beginTile.x, beginTile.y
            );
            camera.position.set(carStartPos.x, carStartPos.y - 100, 80);
            camera.lookAt(carStartPos.x, carStartPos.y, 0);
            camera.up.set(0, 0, 1);
        }



        centerAndZoomCamera();
    }









    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        if (isPausedAtTrafficLight) {

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {

                    isPausedAtTrafficLight = false;

                    car.setPosition(savedCarPosition);
                    car.setCurrentPointIndex(savedCarIndex);

                }
            }, 3);
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


                Geolocation trafficLightGeo = hasCarReachedTrafficLight(carPosition);
                if (trafficLightGeo != null && !processedTrafficLights.contains(trafficLightGeo)) {

                    savedCarPosition = car.getPosition().cpy();
                    savedCarIndex = car.getCurrentPointIndex();


                    isPausedAtTrafficLight = true;


                    QuestionScreen questionScreen = new QuestionScreen(game, routeCoordinates, semaforPoints);


                    questionScreen.setOnCorrectAnswer(() -> {

                        isPausedAtTrafficLight = true;


                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                isPausedAtTrafficLight = false;
                                processedTrafficLights.add(trafficLightGeo);
                                game.setScreen(DetailedRouteScreen.this);
                            }
                        }, 3);
                    });
                    questionScreen.setOnIncorrectAnswer(() -> {

                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                processedTrafficLights.add(trafficLightGeo);
                                game.setScreen(DetailedRouteScreen.this);
                            }
                        }, 0.2f);
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

        if (showConfetti) {
            drawConfetti(delta);
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


            hasFinished = true;

            showFinishPopup(currentScore,currentUser,semaforPoints.size());

        }

        stage.act(delta);
        stage.draw();


    }

    private void drawConfetti(float delta) {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Confetti confetti : confettiList) {
            confetti.update(delta);
            shapeRenderer.setColor(confetti.color);
            shapeRenderer.circle(confetti.x, confetti.y, 5);
        }
        shapeRenderer.end();
    }

    private void showFinishPopup(int score, String user, int totalQuestions) {


        Dialog finishDialog = new Dialog("", skin);


        Table contentTable = new Table();
        contentTable.setFillParent(true);
        contentTable.pad(20);


        Label titleLabel = new Label("Congratulations, " + user + "!", skin, "title");
        titleLabel.setFontScale(2f);
        titleLabel.setAlignment(Align.center);
        contentTable.add(titleLabel).expandX().center().padBottom(15).row();


        Label messageLabel = new Label(
            "You have successfully completed the route!\nYour Score: " + score +
                "\nTotal Questions Answered: " + totalQuestions, skin
        );
        messageLabel.setColor(Color.BLACK);
        messageLabel.setAlignment(Align.center);
        contentTable.add(messageLabel).expandX().center().padBottom(20).row();

        initializeConfetti();
        showConfetti = true;

        Table buttonTable = new Table();
        TextButton playAgainButton = new TextButton("Play Again", skin);
        TextButton quitButton = new TextButton("Quit", skin);

        playAgainButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resetGame();
                game.setScreen(new GameScreen(game));
            }
        });

        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });


        buttonTable.add(playAgainButton).padRight(20);
        buttonTable.add(quitButton);
        contentTable.add(buttonTable).expandX().center().padTop(10);


        finishDialog.getContentTable().add(contentTable).fill().expand();


        finishDialog.show(stage);


        finishDialog.setColor(Color.WHITE);
        finishDialog.setMovable(false);
    }

    private void initializeConfetti() {
        confettiList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            float x = MathUtils.random(0, Gdx.graphics.getWidth());
            float y = MathUtils.random(Gdx.graphics.getHeight() / 2f, Gdx.graphics.getHeight());
            float dx = MathUtils.random(-50, 50);
            float dy = MathUtils.random(-150, 150);
            Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1f);
            confettiList.add(new Confetti(x, y, dx, dy, color));
        }
    }


    private void resetGame() {

        hasFinished = false;


        car = new Car(routeCoordinates, beginTile, 10f);


        processedTrafficLights.clear();
        savedCarPosition = null;
        savedCarIndex = -1;
        isPausedAtTrafficLight = false;


        GameManager.getInstance().resetScore();

        Gdx.app.log("DetailedRouteScreen", "Game reset.");
    }





    private boolean hasCarReachedFinish(Vector3 carPosition) {
        Geolocation finish = routeCoordinates[routeCoordinates.length - 1]
            [routeCoordinates[routeCoordinates.length - 1].length - 1];
        Vector3 finishPos = getTrafficLightPosition(finish);
        float threshold = 5.0f;

        float distance = carPosition.dst(finishPos);


        return distance < threshold;
    }



    private Geolocation hasCarReachedTrafficLight(Vector3 carPosition) {

        float threshold = 2.5f;


        for (Geolocation trafficLightGeo : semaforPoints) {
            Vector3 trafficLightPosition = getTrafficLightPosition(trafficLightGeo);


            if (carPosition.dst(trafficLightPosition) < threshold) {
                return trafficLightGeo;
            }
        }

        return null;
    }

    private Vector3 getTrafficLightPosition(Geolocation trafficLightGeo) {

        Vector2 position2D = MapRasterTiles.getPixelPosition(
            trafficLightGeo.lat, trafficLightGeo.lng, beginTile.x, beginTile.y
        );


        return new Vector3(position2D.x, position2D.y, 0);
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

        camera.position.set(center.x, center.y - 100, desiredZoom * 500);
        camera.lookAt(center.x, center.y, 0);
        camera.update();
    }

    private void handleInput() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            isCameraFollowingCar = !isCameraFollowingCar;
            System.out.println("Camera mode: " + (isCameraFollowingCar ? "Following Car" : "Manual Control"));
        }


        if (!isCameraFollowingCar) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.z += 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.position.z -= 1f;
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


        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.circle(position.x, position.y, 5);


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


        float poleWidth = 1.5f;
        float poleHeight = 20f;
        float lightBoxWidth = 5f;
        float lightBoxHeight = 2f;
        float lightSpacing = 0.5f;
        float poleOffsetX = 10f;


        float housingHeight = (lightBoxHeight * 3) + (lightSpacing * 2);


        float stateDuration = 2.0f;
        float stateTime = (System.currentTimeMillis() % (int) (stateDuration * 3000)) / 1000f;
        int currentState = (int) (stateTime / stateDuration) % 3;

        for (Geolocation semafor : semaforPoints) {
            Vector2 position2D = MapRasterTiles.getPixelPosition(
                semafor.lat, semafor.lng, beginTile.x, beginTile.y
            );


            float baseYPosition = position2D.y + 5f;
            float baseZ = 20f;


            Vector3 polePosition = new Vector3(
                position2D.x + poleOffsetX,
                baseYPosition,
                baseZ
            );


            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.box(
                polePosition.x - poleWidth / 2,
                polePosition.y - poleWidth / 2,
                polePosition.z,
                poleWidth,
                poleWidth,
                poleHeight
            );


            Vector3 housingPosition = new Vector3(
                polePosition.x,
                polePosition.y,
                polePosition.z
            );



            shapeRenderer.setColor(new Color(Color.LIGHT_GRAY.r, Color.LIGHT_GRAY.g, Color.LIGHT_GRAY.b, 1f));
            shapeRenderer.box(
                housingPosition.x - lightBoxWidth / 2,
                housingPosition.y - lightBoxWidth / 2,
                housingPosition.z,
                lightBoxWidth,
                lightBoxWidth,
                housingHeight
            );



            float greenLightZ = polePosition.z-5f;
            float yellowLightZ = greenLightZ + lightBoxHeight + lightSpacing;
            float redLightZ = yellowLightZ + lightBoxHeight + lightSpacing;


            shapeRenderer.setColor(currentState == 2 ? Color.GREEN : Color.DARK_GRAY);
            shapeRenderer.box(
                housingPosition.x - (lightBoxWidth / 3),
                housingPosition.y - (lightBoxWidth / 3),
                greenLightZ,
                lightBoxWidth / 1.5f, lightBoxWidth / 1.5f, lightBoxHeight
            );


            shapeRenderer.setColor(currentState == 1 ? Color.YELLOW : Color.DARK_GRAY);
            shapeRenderer.box(
                housingPosition.x - (lightBoxWidth / 3),
                housingPosition.y - (lightBoxWidth / 3),
                yellowLightZ,
                lightBoxWidth / 1.5f, lightBoxWidth / 1.5f, lightBoxHeight
            );


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

/*

    private void drawStartAndFinishMarkers() {

        Geolocation start = routeCoordinates[0][0];
        Geolocation finish = routeCoordinates[routeCoordinates.length - 1]
            [routeCoordinates[routeCoordinates.length - 1].length - 1];


        Vector2 startPos = MapRasterTiles.getPixelPosition(start.lat, start.lng, beginTile.x, beginTile.y);
        Vector2 finishPos = MapRasterTiles.getPixelPosition(finish.lat, finish.lng, beginTile.x, beginTile.y);


        Vector2 routeDirection = finishPos.cpy().sub(startPos).nor();


        Vector2 perpendicularStart = new Vector2(-routeDirection.y, routeDirection.x);
        Vector2 perpendicularFinish = new Vector2(routeDirection.y, -routeDirection.x);


        float lineLength = 20f;


        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);


        shapeRenderer.line(
            startPos.x + perpendicularStart.x * lineLength,
            startPos.y + perpendicularStart.y * lineLength,
            startPos.x - perpendicularStart.x * lineLength,
            startPos.y - perpendicularStart.y * lineLength
        );


        shapeRenderer.setColor(Color.RED);


        shapeRenderer.line(
            finishPos.x + perpendicularFinish.x * lineLength,
            finishPos.y + perpendicularFinish.y * lineLength,
            finishPos.x - perpendicularFinish.x * lineLength,
            finishPos.y - perpendicularFinish.y * lineLength
        );

        shapeRenderer.end();


        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        spriteBatch.end();
    }

*/


    private void drawStartAndFinishMarkers() {

        Geolocation start = routeCoordinates[0][0];
        Geolocation finish = routeCoordinates[routeCoordinates.length - 1]
            [routeCoordinates[routeCoordinates.length - 1].length - 1];


        Vector2 startPos = MapRasterTiles.getPixelPosition(start.lat, start.lng, beginTile.x, beginTile.y);
        Vector2 finishPos = MapRasterTiles.getPixelPosition(finish.lat, finish.lng, beginTile.x, beginTile.y);


        float lineLength = 20f;


        Vector2 routeDirection = finishPos.cpy().sub(startPos).nor();


        Vector2 perpendicularStart = new Vector2(-routeDirection.y, routeDirection.x);
        Vector2 perpendicularFinish = new Vector2(routeDirection.y, -routeDirection.x);


        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);


        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rectLine(
            startPos.x + perpendicularStart.x * lineLength,
            startPos.y + perpendicularStart.y * lineLength,
            startPos.x - perpendicularStart.x * lineLength,
            startPos.y - perpendicularStart.y * lineLength,
            8f
        );


        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rectLine(
            finishPos.x + perpendicularFinish.x * lineLength,
            finishPos.y + perpendicularFinish.y * lineLength,
            finishPos.x - perpendicularFinish.x * lineLength,
            finishPos.y - perpendicularFinish.y * lineLength,
            5f
        );

        shapeRenderer.end();

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



