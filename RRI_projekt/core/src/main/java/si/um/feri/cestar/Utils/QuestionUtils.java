package si.um.feri.cestar.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import si.um.feri.cestar.screens.QuestionScreen;

public class QuestionUtils {

    private Map<QuestionScreen.Difficulty, List<Question>> questionMap;

    public QuestionUtils() {
        initializeQuestions(); // Initialize questions in the constructor
    }

    private void initializeQuestions() {
        questionMap = new HashMap<>(); // Explicitly initialize the map

        // Easy questions (50)
        List<Question> easyQuestions = new ArrayList<>();
        easyQuestions.add(new Question("Which class is used for 2D rendering in LibGDX?",
            Arrays.asList("SpriteBatch", "Stage", "ShapeRenderer"), 0));
        easyQuestions.add(new Question("What is the default coordinate origin in LibGDX?",
            Arrays.asList("Top-left", "Center", "Bottom-left"), 2));
        easyQuestions.add(new Question("Which method is used to set the window title in LibGDX?",
            Arrays.asList("setTitle()", "setWindowTitle()", "setAppTitle()"), 0));
        easyQuestions.add(new Question("Which class is used to load textures in LibGDX?",
            Arrays.asList("TextureLoader", "AssetManager", "Pixmap"), 2));
        easyQuestions.add(new Question("What is the default viewport in LibGDX?",
            Arrays.asList("FitViewport", "StretchViewport", "ScreenViewport"), 2));
        easyQuestions.add(new Question("Which method is used to render the screen in LibGDX?",
            Arrays.asList("show()", "render()", "draw()"), 1));
        easyQuestions.add(new Question("Which class is used to represent a 2D texture in LibGDX?",
            Arrays.asList("Texture", "Pixmap", "Sprite"), 0));
        easyQuestions.add(new Question("Which method is used to set the window's resolution in LibGDX?",
            Arrays.asList("setResolution()", "setWindowSize()", "setSize()"), 2));
        easyQuestions.add(new Question("Which class is used for handling input in LibGDX?",
            Arrays.asList("Input", "InputProcessor", "Touchpad"), 1));
        easyQuestions.add(new Question("What does Gdx.gl.glClear() do?",
            Arrays.asList("Clears the screen", "Resets the game state", "Sets the clear color"), 0));

        easyQuestions.add(new Question("Which method in LibGDX handles key input?",
            Arrays.asList("keyDown()", "onKeyPress()", "keyPressed()"), 0));
        easyQuestions.add(new Question("Which method is used to create a new SpriteBatch?",
            Arrays.asList("new SpriteBatch()", "createSpriteBatch()", "initBatch()"), 0));
        easyQuestions.add(new Question("Which class is used to manage audio in LibGDX?",
            Arrays.asList("Sound", "Music", "AudioManager"), 1));
        easyQuestions.add(new Question("What is the default background color for a LibGDX game window?",
            Arrays.asList("White", "Black", "Transparent"), 1));
        easyQuestions.add(new Question("What class in LibGDX is used for vector math?",
            Arrays.asList("Vector2", "MathUtils", "Vector3"), 0));
        easyQuestions.add(new Question("Which class is used to draw lines in LibGDX?",
            Arrays.asList("ShapeRenderer", "SpriteBatch", "LineDrawer"), 0));
        easyQuestions.add(new Question("What method is called to stop rendering in LibGDX?",
            Arrays.asList("dispose()", "finishRendering()", "pause()"), 0));
        easyQuestions.add(new Question("Which method is called to update game logic in LibGDX?",
            Arrays.asList("show()", "render()", "dispose()"), 1));
        easyQuestions.add(new Question("Which method is used to start a LibGDX application?",
            Arrays.asList("start()", "launch()", "LwjglApplication()"), 2));
        easyQuestions.add(new Question("Which class handles 2D transformations in LibGDX?",
            Arrays.asList("Matrix4", "Transform2D", "Matrix3"), 0));
        easyQuestions.add(new Question("Which method is used to load a texture from a file?",
            Arrays.asList("loadTexture()", "getTexture()", "new Texture()"), 2));
        easyQuestions.add(new Question("Which class is used for managing a 3D camera?",
            Arrays.asList("PerspectiveCamera", "OrthographicCamera", "Camera3D"), 0));


        // Medium questions (50)
        List<Question> mediumQuestions = new ArrayList<>();
        mediumQuestions.add(new Question("Which method is called to update game logic in LibGDX?",
            Arrays.asList("show()", "render()", "dispose()"), 1));
        mediumQuestions.add(new Question("Which class is used to load textures in LibGDX?",
            Arrays.asList("TextureLoader", "AssetManager", "Pixmap"), 1));
        mediumQuestions.add(new Question("What is the purpose of `Gdx.gl.glClearColor()`?",
            Arrays.asList("To clear memory", "To set the clear color of the screen", "To reset the game state"), 1));
        mediumQuestions.add(new Question("Which class is used to handle touch input in LibGDX?",
            Arrays.asList("Input", "InputProcessor", "Touchpad"), 1));
        mediumQuestions.add(new Question("What class in LibGDX is used to represent a shape renderer?",
            Arrays.asList("ShapeRenderer", "SpriteBatch", "LineRenderer"), 0));
        mediumQuestions.add(new Question("What class is used to load audio in LibGDX?",
            Arrays.asList("Sound", "Music", "AudioManager"), 1));
        mediumQuestions.add(new Question("Which method is used to pause the game in LibGDX?",
            Arrays.asList("pause()", "stop()", "onPause()"), 0));
        mediumQuestions.add(new Question("What method is used to clear the screen in LibGDX?",
            Arrays.asList("clearScreen()", "glClear()", "reset()"), 1));
        mediumQuestions.add(new Question("What class is used to manage the game state?",
            Arrays.asList("Game", "StateManager", "AppState"), 0));
        mediumQuestions.add(new Question("What does `Gdx.graphics.getWidth()` return?",
            Arrays.asList("Window width", "Screen resolution", "Window height"), 0));

        // Add more medium questions as needed...

        // Hard questions (50)
        List<Question> hardQuestions = new ArrayList<>();
        hardQuestions.add(new Question("Which ShapeRenderer mode is used to draw filled shapes?",
            Arrays.asList("ShapeType.Line", "ShapeType.Filled", "ShapeType.Point"), 1));
        hardQuestions.add(new Question("What is the purpose of `Gdx.gl.glClearColor()`?",
            Arrays.asList("To clear memory", "To set the clear color of the screen", "To reset the game state"), 1));
        hardQuestions.add(new Question("Which class is used for 3D rendering in LibGDX?",
            Arrays.asList("ModelBatch", "SpriteBatch", "ShapeRenderer"), 0));
        hardQuestions.add(new Question("How do you disable depth testing in LibGDX?",
            Arrays.asList("Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)", "Gdx.gl.glDisable(GL20.GL_BLEND)", "Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)"), 0));
        hardQuestions.add(new Question("Which of the following is a LibGDX class for handling 3D models?",
            Arrays.asList("Model", "Mesh", "MeshRenderer"), 0));
        hardQuestions.add(new Question("Which class in LibGDX is used for 3D physics?",
            Arrays.asList("Bullet", "Physics3D", "PhysicsWorld"), 0));
        hardQuestions.add(new Question("Which method is used for frame-by-frame updates in LibGDX?",
            Arrays.asList("update()", "render()", "draw()"), 1));
        hardQuestions.add(new Question("What class is used for managing assets asynchronously in LibGDX?",
            Arrays.asList("AssetManager", "TextureLoader", "AssetHelper"), 0));
        hardQuestions.add(new Question("How do you handle collisions in LibGDX using Box2D?",
            Arrays.asList("World contact listener", "Shape collision manager", "CollisionManager"), 0));
        hardQuestions.add(new Question("What class is used for 3D camera manipulation in LibGDX?",
            Arrays.asList("PerspectiveCamera", "OrthographicCamera", "Camera3D"), 0));

        // Add more hard questions as needed...

        questionMap.put(QuestionScreen.Difficulty.EASY, easyQuestions);
        questionMap.put(QuestionScreen.Difficulty.MEDIUM, mediumQuestions);
        questionMap.put(QuestionScreen.Difficulty.HARD, hardQuestions);
    }

    public Map<QuestionScreen.Difficulty, List<Question>> getQuestionMap() {
        return questionMap;
    }



}
