package si.um.feri.cestar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;

import java.util.List;

import si.um.feri.cestar.IgreProjekt;
import si.um.feri.cestar.Utils.GameManager;
import si.um.feri.cestar.Utils.Geolocation;
import si.um.feri.cestar.Utils.Question;
import si.um.feri.cestar.assets.AssetsDescriptor;

public class QuestionScreen extends ScreenAdapter {

    private final IgreProjekt game;
    private final Geolocation[][] routeCoordinates;
    private final List<Geolocation> semaforPoints;
    private final AssetManager assetManager;
    private Skin skin;
    private SpriteBatch batch;
    private BitmapFont font;
    private Label feedbackLabel;


    private Stage stage;
    private String userAnswer = "";
    private String feedback = "";
    private String question = "";

    public enum Difficulty {EASY, MEDIUM, HARD}

    private Difficulty selectedDifficulty = null;

    private boolean isQuestionDisplayed = false;
    private Question currentQuestion;


    private Runnable onCorrectAnswer;
    private Runnable onIncorrectAnswer;

    public QuestionScreen(IgreProjekt game, Geolocation[][] routeCoordinates, List<Geolocation> semaforPoints) {
        this.game = game;
        this.routeCoordinates = routeCoordinates;
        this.semaforPoints = semaforPoints;
        this.assetManager = game.getAssetManager();
    }


    public void setOnCorrectAnswer(Runnable callback) {
        this.onCorrectAnswer = callback;
    }

    public void setOnIncorrectAnswer(Runnable callback) {
        this.onIncorrectAnswer = callback;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        skin = assetManager.get(AssetsDescriptor.UI_SKIN);
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);


        if (selectedDifficulty == null) {


            drawTrafficLight();


            batch.begin();

            font.setColor(Color.WHITE);
            font.getData().setScale(1.4f);


            String instructionText = "Click on Red (Hard), Yellow (Medium), or Green (Easy) to select a difficulty";
            GlyphLayout layout = new GlyphLayout(font, instructionText);
            float x = (Gdx.graphics.getWidth() - layout.width) / 2f;
            float y = Gdx.graphics.getHeight() - 50f;
            font.draw(batch, layout, x, y);

            batch.end();

        } else {

            batch.begin();


            font.getData().setScale(2);
            font.setColor(Color.WHITE);


            font.getData().setScale(1.5f);
            font.draw(batch,
                "Score: " + GameManager.getInstance().getScore(),
                50,
                Gdx.graphics.getHeight() - 50);


            if (!isQuestionDisplayed) {
                currentQuestion = GameManager.getInstance().getRandomQuestion(selectedDifficulty);
                if (currentQuestion != null) {
                    question = currentQuestion.getQuestionText();
                    feedback = "";
                    showQuestionWithButtons();
                }
                isQuestionDisplayed = true;
            }

            else {
                font.getData().setScale(1.2f);
                font.draw(batch,
                    feedback,
                    Gdx.graphics.getWidth() / 2f - 300,
                    Gdx.graphics.getHeight() / 2f - 50);
            }

            batch.end();
        }


        stage.act(delta);
        stage.draw();


        handleInput();
    }


    private void showQuestionWithButtons() {
        stage.clear();


        Label questionLabel = new Label(question, skin);
        questionLabel.setFontScale(1.5f);


        questionLabel.setPosition(
            (Gdx.graphics.getWidth() - questionLabel.getWidth()) / 2f,
            Gdx.graphics.getHeight() - Gdx.graphics.getHeight() * 0.1f
        );
        stage.addActor(questionLabel);


        List<String> answers = currentQuestion.getAnswers();


        float buttonWidth = Gdx.graphics.getWidth() * 0.6f;
        float buttonHeight = Gdx.graphics.getHeight() * 0.1f;
        float buttonSpacing = Gdx.graphics.getHeight() * 0.05f;


        float totalHeight = (buttonHeight + buttonSpacing) * answers.size() - buttonSpacing;


        float startY = (Gdx.graphics.getHeight() - totalHeight) / 2f;

        for (int i = 0; i < answers.size(); i++) {
            String answerText = answers.get(i);

            TextButton answerButton = new TextButton((i + 1) + ". " + answerText, skin);
            answerButton.setSize(buttonWidth, buttonHeight);


            answerButton.setPosition(
                (Gdx.graphics.getWidth() - buttonWidth) / 2f,
                startY + (answers.size() - 1 - i) * (buttonHeight + buttonSpacing)
            );

            final int answerIndex = i;
            answerButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleAnswer(answerIndex);
                }
            });

            stage.addActor(answerButton);
        }
    }

    private void handleAnswer(int selectedAnswerIndex) {
        if (GameManager.getInstance().isAnswerCorrect(selectedAnswerIndex)) {
            GameManager.getInstance().updateScore(selectedDifficulty);
            if (onCorrectAnswer != null) {
                onCorrectAnswer.run();
            }
            showFeedbackMessage("Correct!");
        } else {
            showFeedbackMessage("Incorrect!");




            if (onIncorrectAnswer != null) {
                onIncorrectAnswer.run();
            }


        }
    }

    private void showFeedbackMessage(String message) {

        if (feedbackLabel != null) {
            stage.getActors().removeValue(feedbackLabel, true);
        }


        feedbackLabel = new Label(message, skin);
        feedbackLabel.setFontScale(1.2f);


        feedbackLabel.setPosition(
            (Gdx.graphics.getWidth() - feedbackLabel.getWidth()) / 2f,
            Gdx.graphics.getHeight() * 0.2f
        );


        stage.addActor(feedbackLabel);


        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {

                stage.getActors().removeValue(feedbackLabel, true);
            }
        }, 2.0f);
    }


    private void drawTrafficLight() {
        ShapeRenderer shapeRenderer = new ShapeRenderer();

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();


        float containerWidth = screenWidth * 0.2f;
        float containerHeight = screenHeight * 0.8f;
        float lightRadius = containerWidth * 0.25f;
        float spacing = lightRadius * 1.5f;
        float containerX = (screenWidth - containerWidth) / 2f;
        float containerY = (screenHeight - containerHeight) / 2f;

        float redCenterY = containerY + containerHeight - lightRadius - spacing / 2f;
        float yellowCenterY = containerY + containerHeight / 2f;
        float greenCenterY = containerY + lightRadius + spacing / 2f;

        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);


        shapeRenderer.setColor(Color.DARK_GRAY);
        drawRoundedRectangle(shapeRenderer, containerX, containerY, containerWidth, containerHeight, lightRadius);


        boolean isHoveringRed = isMouseOverCircle(mouseX, mouseY, containerX + containerWidth / 2f, redCenterY, lightRadius);
        shapeRenderer.setColor(isHoveringRed ? Color.SCARLET : Color.RED);
        shapeRenderer.circle(containerX + containerWidth / 2f, redCenterY, lightRadius);


        boolean isHoveringYellow = isMouseOverCircle(mouseX, mouseY, containerX + containerWidth / 2f, yellowCenterY, lightRadius);
        shapeRenderer.setColor(isHoveringYellow ? Color.GOLD : Color.YELLOW);
        shapeRenderer.circle(containerX + containerWidth / 2f, yellowCenterY, lightRadius);


        boolean isHoveringGreen = isMouseOverCircle(mouseX, mouseY, containerX + containerWidth / 2f, greenCenterY, lightRadius);
        shapeRenderer.setColor(isHoveringGreen ? Color.LIME : Color.GREEN);
        shapeRenderer.circle(containerX + containerWidth / 2f, greenCenterY, lightRadius);

        shapeRenderer.end();
        shapeRenderer.dispose();
    }

    private void handleInput() {
        if (selectedDifficulty == null && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float screenHeight = Gdx.graphics.getHeight();
            float screenWidth = Gdx.graphics.getWidth();

            float containerWidth = screenWidth * 0.2f;
            float containerHeight = screenHeight * 0.8f;
            float lightRadius = containerWidth * 0.4f;
            float containerX = (screenWidth - containerWidth) / 2f;
            float containerY = (screenHeight - containerHeight) / 2f;

            float mouseX = Gdx.input.getX();
            float mouseY = screenHeight - Gdx.input.getY();


            if (isMouseOverCircle(mouseX, mouseY,
                containerX + containerWidth / 2f,
                containerY + containerHeight - (lightRadius + 20),
                lightRadius)) {
                selectedDifficulty = Difficulty.HARD;
            }

            else if (isMouseOverCircle(mouseX, mouseY,
                containerX + containerWidth / 2f,
                containerY + containerHeight / 2f,
                lightRadius)) {
                selectedDifficulty = Difficulty.MEDIUM;
            }

            else if (isMouseOverCircle(mouseX, mouseY,
                containerX + containerWidth / 2f,
                containerY + lightRadius + 20,
                lightRadius)) {
                selectedDifficulty = Difficulty.EASY;
            }

        } else if (selectedDifficulty != null && isQuestionDisplayed) {

            for (int i = 1; i <= 3; i++) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + (i - 1))) {
                    userAnswer = String.valueOf(i);
                    if (GameManager.getInstance().isAnswerCorrect(i - 1)) {
                        feedback = "Correct!";
                        GameManager.getInstance().updateScore(selectedDifficulty);
                        if (onCorrectAnswer != null) {
                            onCorrectAnswer.run();
                        }
                    } else {
                        feedback = "Incorrect! Try again.";
                        userAnswer = "";
                    }
                }
            }
        }
    }

    private boolean isMouseOverCircle(float mouseX, float mouseY, float centerX, float centerY, float radius) {
        float dx = mouseX - centerX;
        float dy = mouseY - centerY;
        return dx * dx + dy * dy <= radius * radius;
    }

    private void drawRoundedRectangle(ShapeRenderer shapeRenderer, float x, float y, float width, float height, float cornerRadius) {
        shapeRenderer.rect(x + cornerRadius, y, width - 2 * cornerRadius, height);
        shapeRenderer.rect(x, y + cornerRadius, width, height - 2 * cornerRadius);
        shapeRenderer.circle(x + cornerRadius, y + cornerRadius, cornerRadius);
        shapeRenderer.circle(x + width - cornerRadius, y + cornerRadius, cornerRadius);
        shapeRenderer.circle(x + cornerRadius, y + height - cornerRadius, cornerRadius);
        shapeRenderer.circle(x + width - cornerRadius, y + height - cornerRadius, cornerRadius);
    }


    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        stage.dispose();
    }
}
