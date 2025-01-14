package si.um.feri.cestar.screens;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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

public class LeaderboardScreen extends ScreenAdapter {

    private final IgreProjekt game;
    private Stage stage;
    private MongoDB mongoDB;
    private Skin skin;
    private final AssetManager assetManager;
    private ShapeRenderer shapeRenderer;



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

        shapeRenderer = new ShapeRenderer();

        displayLeaderboard();
        addBackButton();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
