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
    private Question currentQuestion; // Store the current question

    // Callback to notify when the answer is correct
    private Runnable onCorrectAnswer;
    private Runnable onIncorrectAnswer;

    public QuestionScreen(IgreProjekt game, Geolocation[][] routeCoordinates, List<Geolocation> semaforPoints) {
        this.game = game;
        this.routeCoordinates = routeCoordinates;
        this.semaforPoints = semaforPoints;
        this.assetManager = game.getAssetManager();
    }

    // Method to set the callback
    public void setOnCorrectAnswer(Runnable callback) {
        this.onCorrectAnswer = callback;
    }

    public void setOnIncorrectAnswer(Runnable callback) {
        this.onIncorrectAnswer = callback;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont(); // Use the default font
        skin = assetManager.get(AssetsDescriptor.UI_SKIN);
        stage = new Stage(); // Initialize the stage
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // If difficulty not chosen yet, draw only traffic light and instructions
        if (selectedDifficulty == null) {

            // 1) Draw traffic light first with ShapeRenderer
            drawTrafficLight();

            // 2) Now draw instructions with SpriteBatch
            batch.begin();

            font.setColor(Color.WHITE);
            font.getData().setScale(1.4f);

            // Center the instructions on screen
            String instructionText = "Click on Red (Hard), Yellow (Medium), or Green (Easy) to select a difficulty";
            GlyphLayout layout = new GlyphLayout(font, instructionText);
            float x = (Gdx.graphics.getWidth() - layout.width) / 2f;
            float y = Gdx.graphics.getHeight() - 50f;  // 50px from top
            font.draw(batch, layout, x, y);

            batch.end();

        } else {
            // If difficulty IS chosen, show everything else (title, score, question, etc.)
            batch.begin();

            // Draw the title
            font.getData().setScale(2);
            font.setColor(Color.WHITE);

            // Draw the score
            font.getData().setScale(1.5f);
            font.draw(batch,
                "Score: " + GameManager.getInstance().getScore(),
                50,
                Gdx.graphics.getHeight() - 50);

            // If first time showing the question
            if (!isQuestionDisplayed) {
                currentQuestion = GameManager.getInstance().getRandomQuestion(selectedDifficulty);
                if (currentQuestion != null) {
                    question = currentQuestion.getQuestionText();
                    feedback = "";
                    showQuestionWithButtons();
                }
                isQuestionDisplayed = true;
            }
            // Otherwise, just show feedback (e.g., “Correct!” or “Incorrect!”)
            else {
                font.getData().setScale(1.2f);
                font.draw(batch,
                    feedback,
                    Gdx.graphics.getWidth() / 2f - 300,
                    Gdx.graphics.getHeight() / 2f - 50);
            }

            batch.end();
        }

        // Render stage for any buttons
        stage.act(delta);
        stage.draw();

        // Handle clicks/keyboard
        handleInput();
    }


    private void showQuestionWithButtons() {
        stage.clear(); // Clear previous buttons

        // Center the question label
        Label questionLabel = new Label(question, skin);
        questionLabel.setFontScale(1.5f);

        // Calculate position for centered question
        questionLabel.setPosition(
            (Gdx.graphics.getWidth() - questionLabel.getWidth()) / 2f,
            Gdx.graphics.getHeight() - Gdx.graphics.getHeight() * 0.1f // 30% from the top
        );
        stage.addActor(questionLabel);

        // Get answers and set up buttons
        List<String> answers = currentQuestion.getAnswers();

        // Dynamically calculate button dimensions
        float buttonWidth = Gdx.graphics.getWidth() * 0.6f;
        float buttonHeight = Gdx.graphics.getHeight() * 0.1f;
        float buttonSpacing = Gdx.graphics.getHeight() * 0.05f;

        // Calculate the total height of all buttons and spacings
        float totalHeight = (buttonHeight + buttonSpacing) * answers.size() - buttonSpacing;

        // Calculate the starting Y position for buttons to be vertically centered
        float startY = (Gdx.graphics.getHeight() - totalHeight) / 2f;

        for (int i = 0; i < answers.size(); i++) {
            String answerText = answers.get(i);

            TextButton answerButton = new TextButton((i + 1) + ". " + answerText, skin);
            answerButton.setSize(buttonWidth, buttonHeight);

            // Position each button centered horizontally and vertically
            answerButton.setPosition(
                (Gdx.graphics.getWidth() - buttonWidth) / 2f, // Center horizontally
                startY + (answers.size() - 1 - i) * (buttonHeight + buttonSpacing) // Adjust vertically
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

            // Timer to handle incorrect answer

            // Do not trigger screen change or re-initiate traffic light
            if (onIncorrectAnswer != null) {
                onIncorrectAnswer.run(); // Execute the callback without resetting traffic light
            }


        }
    }

    private void showFeedbackMessage(String message) {
        // Remove previous feedback label if it exists
        if (feedbackLabel != null) {
            stage.getActors().removeValue(feedbackLabel, true);
        }

        // Create or update the feedback label
        feedbackLabel = new Label(message, skin);
        feedbackLabel.setFontScale(1.2f);

        // Position the feedback label clearly
        feedbackLabel.setPosition(
            (Gdx.graphics.getWidth() - feedbackLabel.getWidth()) / 2f, // Center horizontally
            Gdx.graphics.getHeight() * 0.2f // 20% from the bottom
        );

        // Add the feedback label to the stage
        stage.addActor(feedbackLabel);

        // Optionally, you can add a delay before the feedback disappears:
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Remove the feedback after a short delay
                stage.getActors().removeValue(feedbackLabel, true);
            }
        }, 2.0f);  // Feedback disappears after 2 seconds
    }


    private void drawTrafficLight() {
        ShapeRenderer shapeRenderer = new ShapeRenderer();

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Container dimensions
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
        float mouseY = screenHeight - Gdx.input.getY(); // Flip Y-axis

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Traffic light container
        shapeRenderer.setColor(Color.DARK_GRAY);
        drawRoundedRectangle(shapeRenderer, containerX, containerY, containerWidth, containerHeight, lightRadius);

        // Red
        boolean isHoveringRed = isMouseOverCircle(mouseX, mouseY, containerX + containerWidth / 2f, redCenterY, lightRadius);
        shapeRenderer.setColor(isHoveringRed ? Color.SCARLET : Color.RED);
        shapeRenderer.circle(containerX + containerWidth / 2f, redCenterY, lightRadius);

        // Yellow
        boolean isHoveringYellow = isMouseOverCircle(mouseX, mouseY, containerX + containerWidth / 2f, yellowCenterY, lightRadius);
        shapeRenderer.setColor(isHoveringYellow ? Color.GOLD : Color.YELLOW);
        shapeRenderer.circle(containerX + containerWidth / 2f, yellowCenterY, lightRadius);

        // Green
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

            // Check RED circle
            if (isMouseOverCircle(mouseX, mouseY,
                containerX + containerWidth / 2f,
                containerY + containerHeight - (lightRadius + 20),
                lightRadius)) {
                selectedDifficulty = Difficulty.HARD;
            }
            // Check YELLOW circle
            else if (isMouseOverCircle(mouseX, mouseY,
                containerX + containerWidth / 2f,
                containerY + containerHeight / 2f,
                lightRadius)) {
                selectedDifficulty = Difficulty.MEDIUM;
            }
            // Check GREEN circle
            else if (isMouseOverCircle(mouseX, mouseY,
                containerX + containerWidth / 2f,
                containerY + lightRadius + 20,
                lightRadius)) {
                selectedDifficulty = Difficulty.EASY;
            }

        } else if (selectedDifficulty != null && isQuestionDisplayed) {
            // Keyboard shortcuts 1,2,3 for answers
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
