package si.um.feri.cestar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import si.um.feri.cestar.IgreProjekt;
import si.um.feri.cestar.Utils.Constants;
import si.um.feri.cestar.assets.AssetsDescriptor;
import si.um.feri.cestar.assets.RegionNames;

public class IntroScreen extends ScreenAdapter {

    private final IgreProjekt game;
    private final AssetManager assetManager;
    private ShapeRenderer shapeRenderer;

    private Viewport viewport;
    private TextureAtlas gameplayAtlas;
    private Image fadeOverlay;
    private TextureRegion backgroundImage;

    private boolean isProceeding = false;

    private Stage stage;

    public IntroScreen(IgreProjekt game) {
        this.game = game;
        assetManager = game.getAssetManager();
    }

    @Override
    public void show() {
        // Clear color to black
        Gdx.gl.glClearColor(0, 0, 0, 1);

        viewport = new StretchViewport(Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
        stage = new Stage(viewport, game.getBatch());
        shapeRenderer = new ShapeRenderer();

        // Load assets
        assetManager.load(AssetsDescriptor.UI_SKIN);
        assetManager.load(AssetsDescriptor.GAMEPLAY);
        assetManager.finishLoading();

        gameplayAtlas = assetManager.get(AssetsDescriptor.GAMEPLAY);
        backgroundImage = gameplayAtlas.findRegion(RegionNames.BACKGROUND);

        // Background image setup
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        Image background = new Image(new TextureRegionDrawable(backgroundImage));
        background.setSize(screenWidth, screenHeight);
        background.setPosition(0, 0);
        stage.addActor(background);

        // Add animations to the background
        background.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.moveBy(0.5f, 0, 0.5f),
                    Actions.moveBy(-0.5f, 0, 0.5f)
                )
            )
        );

        // Create fade overlay
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture blackTexture = new Texture(pixmap);
        pixmap.dispose();

        fadeOverlay = new Image(new TextureRegionDrawable(new TextureRegion(blackTexture)));
        fadeOverlay.setSize(Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
        fadeOverlay.setPosition(0, 0);
        fadeOverlay.setColor(1, 1, 1, 1);
        stage.addActor(fadeOverlay);
        fadeOverlay.addAction(Actions.fadeOut(1f));

        // Falling letters effect with phasing
        addPhasedFallingLetters("RENT RIDE");

        // "Press Any Key to Continue" label
        Skin skin = assetManager.get(AssetsDescriptor.UI_SKIN);
        Label continueLabel = new Label("Press Any Key to Continue", skin);
        continueLabel.setFontScale(1f);
        continueLabel.setAlignment(Align.center);
        continueLabel.setSize(Constants.HUD_WIDTH, 30);
        continueLabel.setPosition(0, Constants.HUD_HEIGHT / 2f - 200);
        stage.addActor(continueLabel);

        // Blinking fade in/out animation for the "continue" label
        continueLabel.setColor(1, 1, 1, 0);
        continueLabel.addAction(
            Actions.sequence(
                Actions.fadeIn(1f),
                Actions.forever(
                    Actions.sequence(
                        Actions.fadeOut(1f),
                        Actions.fadeIn(1f)
                    )
                )
            )
        );

        Gdx.input.setInputProcessor(stage);
    }

    private void addPhasedFallingLetters(String text) {
        Skin skin = assetManager.get(AssetsDescriptor.UI_SKIN);
        float centerX = Constants.HUD_WIDTH / 2f;
        float centerY = Constants.HUD_HEIGHT / 2f;

        float letterSpacing = 60; // Space between letters

        for (int i = 0; i < text.length(); i++) {
            final char letter = text.charAt(i);

            // Create a Label for the letter
            Label letterLabel = new Label(String.valueOf(letter), skin);
            letterLabel.setFontScale(2f);
            letterLabel.setAlignment(Align.center);

            // Enable white outline effect
            letterLabel.getStyle().fontColor = Color.WHITE;

            // Calculate the initial position above the screen
            float startX = centerX + (i - text.length() / 2f) * letterSpacing;
            float startY = Constants.HUD_HEIGHT + 100; // Start above the screen

            // Set the position of the letter
            letterLabel.setPosition(startX, startY);

            // Add falling and phasing animation
            letterLabel.addAction(
                Actions.sequence(
                    Actions.delay(i * 0.2f), // Staggered delay for each letter
                    Actions.moveTo(startX, centerY, 1f, Interpolation.bounceOut), // Falling effect
                    Actions.forever( // Phasing effect (fade in/out)
                        Actions.sequence(
                            Actions.alpha(0.5f, 0.5f), // Fade out to 50% opacity
                            Actions.alpha(1f, 0.5f) // Fade back to full opacity
                        )
                    )
                )
            );

            stage.addActor(letterLabel);
        }
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getBatch().setProjectionMatrix(viewport.getCamera().combined);
    }

    private void switchScreen(final ScreenAdapter newScreen) {
        if (isProceeding) return;
        isProceeding = true;

        fadeOverlay.clearActions();
        fadeOverlay.setColor(1, 1, 1, 0);
        fadeOverlay.addAction(
            Actions.sequence(
                Actions.fadeIn(0.5f),
                Actions.run(() -> {
                    game.setScreen(newScreen);
                })
            )
        );
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Switch to GameScreen if any key pressed or touch
        if (!isProceeding && (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched())) {
            switchScreen(new GameScreen(game));
            return;
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        shapeRenderer.dispose();
    }
}
