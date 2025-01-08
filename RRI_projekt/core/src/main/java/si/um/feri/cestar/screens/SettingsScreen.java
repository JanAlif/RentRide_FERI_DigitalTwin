package si.um.feri.cestar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import si.um.feri.cestar.IgreProjekt;
import si.um.feri.cestar.Utils.SoundManager;
import si.um.feri.cestar.assets.AssetsDescriptor;

public class SettingsScreen extends ScreenAdapter {

    private final IgreProjekt game;
    private final SoundManager soundManager;
    private Stage stage;
    private Skin skin;
    private final AssetManager assetManager;

    public SettingsScreen(IgreProjekt game) {
        this.game = game;
        this.soundManager = SoundManager.getInstance();
        this.assetManager = game.getAssetManager();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = assetManager.get(AssetsDescriptor.UI_SKIN);

        // Create the settings layout
        createSettingsLayout();
    }

    private void createSettingsLayout() {
        Table table = new Table();
        table.setFillParent(true);

        // Title Label
        Label titleLabel = new Label("Settings", skin);
        titleLabel.setFontScale(2f);
        table.add(titleLabel).colspan(2).padBottom(30).row();

        // Music Toggle
        Label musicLabel = new Label("Music: ", skin);
        TextButton musicToggle = new TextButton(soundManager.isMusicEnabled() ? "ON" : "OFF", skin);
        musicToggle.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                boolean musicEnabled = !soundManager.isMusicEnabled();
                soundManager.setMusicEnabled(musicEnabled);
                musicToggle.setText(musicEnabled ? "ON" : "OFF");
            }
        });

        table.add(musicLabel).pad(10);
        table.add(musicToggle).pad(10).row();

        // Sound Effects Toggle
        Label soundLabel = new Label("Sound Effects: ", skin);
        TextButton soundToggle = new TextButton(soundManager.isSoundEnabled() ? "ON" : "OFF", skin);
        soundToggle.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                boolean soundEnabled = !soundManager.isSoundEnabled();
                soundManager.setSoundEnabled(soundEnabled);
                soundToggle.setText(soundEnabled ? "ON" : "OFF");
            }
        });

        table.add(soundLabel).pad(10);
        table.add(soundToggle).pad(10).row();

        // Back Button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game)); // Navigate back to the menu
            }
        });

        table.add(backButton).colspan(2).padTop(30).center().row();

        // Add the table to the stage
        stage.addActor(table);
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
    }




}
