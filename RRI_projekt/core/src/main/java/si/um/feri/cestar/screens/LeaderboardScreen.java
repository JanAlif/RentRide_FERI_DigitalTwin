package si.um.feri.cestar.screens;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import org.bson.Document;
import java.util.List;
import si.um.feri.cestar.IgreProjekt;
import si.um.feri.cestar.Utils.MongoDB;
import si.um.feri.cestar.assets.AssetsDescriptor;
import si.um.feri.cestar.assets.RegionNames;


public class LeaderboardScreen extends ScreenAdapter {

    private final IgreProjekt game;
    private Stage stage;
    private MongoDB mongoDB;
    private Skin skin;
    private final AssetManager assetManager;
    private ShapeRenderer shapeRenderer;
    TextureAtlas gameplayAtlas;
    TextureRegion stopIcon;
    SpriteBatch spriteBatch;
    TextureRegion arrowIcon;
    TextureRegion noUTurn;
    TextureRegion parkingIcon;
    TextureRegion roadWorkIcon;
    TextureRegion leftTurnIcon;
    TextureRegion rightTurnIcon;
    TextureRegion noEntryIcon;
    private float[][] iconPositions;
    private float[][] iconVelocities;




    public LeaderboardScreen(IgreProjekt game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        mongoDB = new MongoDB();
        mongoDB.connectToMongoDB();
        this.assetManager = game.getAssetManager();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        skin = assetManager.get(AssetsDescriptor.UI_SKIN);
        gameplayAtlas = assetManager.get(AssetsDescriptor.GAMEPLAY);

        stopIcon = gameplayAtlas.findRegion(RegionNames.STOPSIGN);
        noUTurn  = gameplayAtlas.findRegion(RegionNames.NOTURN);
        parkingIcon  = gameplayAtlas.findRegion(RegionNames.PARKING);
        roadWorkIcon  = gameplayAtlas.findRegion(RegionNames.ROADWORK);
        leftTurnIcon = gameplayAtlas.findRegion(RegionNames.TURNLEFT);
        rightTurnIcon = gameplayAtlas.findRegion(RegionNames.TURNRIGHT);
        noEntryIcon = gameplayAtlas.findRegion(RegionNames.NOENTRY);

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();

        int numIcons = 6;
        iconPositions = new float[numIcons][2];
        iconVelocities = new float[numIcons][2];
        for (int i = 0; i < numIcons; i++) {

            iconPositions[i][0] = MathUtils.random(0, Gdx.graphics.getWidth());
            iconPositions[i][1] = MathUtils.random(0, Gdx.graphics.getHeight());


            iconVelocities[i][0] = MathUtils.random(-300, 300) * Gdx.graphics.getDeltaTime();
            iconVelocities[i][1] = MathUtils.random(-300, 300) * Gdx.graphics.getDeltaTime();
        }

        displayLeaderboard();
        addBackButton();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        for (int i = 0; i < iconPositions.length; i++) {
            iconPositions[i][0] += iconVelocities[i][0];
            iconPositions[i][1] += iconVelocities[i][1];


            if (iconPositions[i][0] <= 0 || iconPositions[i][0] >= Gdx.graphics.getWidth() - 50) {
                iconVelocities[i][0] = -iconVelocities[i][0];
            }
            if (iconPositions[i][1] <= 0 || iconPositions[i][1] >= Gdx.graphics.getHeight() - 50) {
                iconVelocities[i][1] = -iconVelocities[i][1];
            }
        }


        spriteBatch.begin();
        TextureRegion[] icons = {stopIcon, noUTurn, parkingIcon, roadWorkIcon, leftTurnIcon, rightTurnIcon, noEntryIcon};
        for (int i = 0; i < iconPositions.length; i++) {
            TextureRegion icon = icons[i % icons.length];
            spriteBatch.draw(icon, iconPositions[i][0], iconPositions[i][1], 50, 50);
        }

        spriteBatch.end();


        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        mongoDB.closeConnection();
    }

    private void displayLeaderboard() {

        Table table = new Table();
        table.setFillParent(true);


        Label title = new Label("Leaderboard", skin);
        title.setFontScale(2);
        table.add(title).colspan(3).padBottom(20).center().row();


        table.add(new Label("Rank", skin)).pad(10).center();
        table.add(new Label("Username", skin)).pad(10).center();
        table.add(new Label("Score", skin)).pad(10).center();
        table.row();


        List<Document> leaderboard = mongoDB.fetchLeaderboard();


        for (int i = 0; i < leaderboard.size(); i++) {
            Document entry = leaderboard.get(i);
            String username = entry.getString("username");
            int score = entry.getInteger("score");

            table.add(new Label(String.valueOf(i + 1), skin)).pad(10).center();
            table.add(new Label(username, skin)).pad(10).center();
            table.add(new Label(String.valueOf(score), skin)).pad(10).center();
            table.row();
        }


        ScrollPane scrollPane = new ScrollPane(table, skin);
        scrollPane.setScrollingDisabled(true, false);


        Table container = new Table();
        container.setFillParent(true);


        float containerWidth = Gdx.graphics.getWidth() * 0.8f;
        float containerHeight = Gdx.graphics.getHeight() * 0.6f;
        container.add(scrollPane).width(containerWidth).height(containerHeight).center();


        stage.addActor(container);
    }

    private void addBackButton() {

        TextButton backButton = new TextButton("Back", skin);


        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });


        Table backButtonTable = new Table();
        backButtonTable.setFillParent(true);
        backButtonTable.bottom().padBottom(20);
        backButtonTable.add(backButton).width(150).height(50).center();


        stage.addActor(backButtonTable);
    }


}
