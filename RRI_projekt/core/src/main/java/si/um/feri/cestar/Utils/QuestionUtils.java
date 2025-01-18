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
        initializeQuestions();
    }

    private void initializeQuestions() {
        questionMap = new HashMap<>();


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
        easyQuestions.add(new Question("What class is used to create a 2D shape like a rectangle or circle in LibGDX?",
            Arrays.asList("ShapeRenderer", "SpriteBatch", "Texture"), 0));
        easyQuestions.add(new Question("What method is used to clear the screen before drawing in LibGDX?",
            Arrays.asList("glClear()", "clear()", "reset()"), 0));
        easyQuestions.add(new Question("What class in LibGDX handles basic touch input events?",
            Arrays.asList("Input", "Touchpad", "GestureDetector"), 0));
        easyQuestions.add(new Question("Which class is used to store the data for textures and images in LibGDX?",
            Arrays.asList("Texture", "Pixmap", "TextureAtlas"), 0));
        easyQuestions.add(new Question("Which method is used to update the game state continuously in LibGDX?",
            Arrays.asList("show()", "update()", "render()"), 2));
        easyQuestions.add(new Question("What does the `dispose()` method do in LibGDX?",
            Arrays.asList("Stops the game", "Cleans up resources", "Clears the screen"), 1));
        easyQuestions.add(new Question("What class in LibGDX is used for playing short sound effects?",
            Arrays.asList("Sound", "Music", "AudioPlayer"), 0));
        easyQuestions.add(new Question("Which class is used to play long background music in LibGDX?",
            Arrays.asList("Sound", "Music", "AudioManager"), 1));
        easyQuestions.add(new Question("What is the default input method in LibGDX for handling mouse events?",
            Arrays.asList("MouseListener", "InputProcessor", "Touchpad"), 1));
        easyQuestions.add(new Question("What is the default coordinate origin for LibGDX when using a `Stage`?",
            Arrays.asList("Top-left", "Center", "Bottom-left"), 0));
        easyQuestions.add(new Question("What class would you use to handle keyboard input in LibGDX?",
            Arrays.asList("InputProcessor", "Touchpad", "Keyboard"), 0));
        easyQuestions.add(new Question("Which class in LibGDX helps you organize and manage game screens?",
            Arrays.asList("Game", "ScreenManager", "Stage"), 0));
        easyQuestions.add(new Question("What class is used for creating simple 2D animated sprites in LibGDX?",
            Arrays.asList("Sprite", "Texture", "SpriteBatch"), 0));



        List<Question> mediumQuestions = new ArrayList<>();
        mediumQuestions.add(new Question("Which method in LibGDX is responsible for continuously updating the game logic in the main loop?",
            Arrays.asList("show()", "render()", "dispose()"), 1));

        mediumQuestions.add(new Question("What is the preferred way to load assets asynchronously in LibGDX to avoid blocking the main thread?",
            Arrays.asList("TextureLoader", "AssetManager", "Pixmap"), 1));

        mediumQuestions.add(new Question("What does `Gdx.gl.glClearColor()` do in LibGDX?",
            Arrays.asList("To clear memory", "To set the clear color of the screen", "To reset the game state"), 1));

        mediumQuestions.add(new Question("Which class should be implemented to handle multi-touch input events in LibGDX?",
            Arrays.asList("Input", "InputProcessor", "Touchpad"), 1));

        mediumQuestions.add(new Question("Which class in LibGDX is used for rendering primitive shapes like lines and rectangles?",
            Arrays.asList("ShapeRenderer", "SpriteBatch", "LineRenderer"), 0));

        mediumQuestions.add(new Question("Which class in LibGDX is responsible for handling background music and sound effects?",
            Arrays.asList("Sound", "Music", "AudioManager"), 1));

        mediumQuestions.add(new Question("Which method is used to pause the game when it loses focus in LibGDX?",
            Arrays.asList("pause()", "stop()", "onPause()"), 0));

        mediumQuestions.add(new Question("Which method in LibGDX should be called to clear the screen before rendering new content?",
            Arrays.asList("clearScreen()", "glClear()", "reset()"), 1));

        mediumQuestions.add(new Question("Which class in LibGDX is responsible for managing different game states (like menu, play, etc.)?",
            Arrays.asList("Game", "StateManager", "AppState"), 0));

        mediumQuestions.add(new Question("What does `Gdx.graphics.getWidth()` return in LibGDX?",
            Arrays.asList("Window width", "Screen resolution", "Window height"), 0));

        mediumQuestions.add(new Question("What class is used to represent the camera in LibGDX?",
            Arrays.asList("PerspectiveCamera", "OrthographicCamera", "Camera"), 1));

        mediumQuestions.add(new Question("What is the purpose of the `Stage` class in LibGDX?",
            Arrays.asList("To handle 2D rendering", "To manage UI elements", "To handle input events"), 1));

        mediumQuestions.add(new Question("Which method is used to draw a sprite on the screen in LibGDX?",
            Arrays.asList("draw()", "render()", "display()"), 0));

        mediumQuestions.add(new Question("Which method is used to start a LibGDX application?",
            Arrays.asList("start()", "launch()", "LwjglApplication()"), 2));

        mediumQuestions.add(new Question("What class in LibGDX is used to represent textures?",
            Arrays.asList("Texture", "Pixmap", "TextureRegion"), 0));

        mediumQuestions.add(new Question("Which method is used to update the game logic in LibGDX?",
            Arrays.asList("show()", "render()", "dispose()"), 1));

        mediumQuestions.add(new Question("Which class is used to manage audio in LibGDX?",
            Arrays.asList("Sound", "Music", "AudioManager"), 1));

        mediumQuestions.add(new Question("Which method is used to create a new SpriteBatch?",
            Arrays.asList("new SpriteBatch()", "createSpriteBatch()", "initBatch()"), 0));

        mediumQuestions.add(new Question("Which method is used to set the window title in LibGDX?",
            Arrays.asList("setTitle()", "setWindowTitle()", "setAppTitle()"), 0));

        mediumQuestions.add(new Question("Which method is used to load a texture from a file in LibGDX?",
            Arrays.asList("loadTexture()", "getTexture()", "new Texture()"), 2));

        mediumQuestions.add(new Question("What is the default coordinate origin in LibGDX?",
            Arrays.asList("Top-left", "Center", "Bottom-left"), 2));

        mediumQuestions.add(new Question("Which class in LibGDX is used to represent a 2D texture?",
            Arrays.asList("Texture", "Pixmap", "Sprite"), 0));

        mediumQuestions.add(new Question("Which method is used to pause the game in LibGDX?",
            Arrays.asList("pause()", "stop()", "onPause()"), 0));

        mediumQuestions.add(new Question("Which class in LibGDX is used to handle touch input?",
            Arrays.asList("Touchpad", "InputProcessor", "Input"), 1));

        mediumQuestions.add(new Question("What method is used to set the window's resolution in LibGDX?",
            Arrays.asList("setResolution()", "setWindowSize()", "setSize()"), 2));

        mediumQuestions.add(new Question("What method is used to stop rendering in LibGDX?",
            Arrays.asList("dispose()", "finishRendering()", "pause()"), 0));

        mediumQuestions.add(new Question("Which method in LibGDX handles key input?",
            Arrays.asList("keyDown()", "onKeyPress()", "keyPressed()"), 0));

        mediumQuestions.add(new Question("Which class in LibGDX is used to handle input?",
            Arrays.asList("Input", "InputProcessor", "Touchpad"), 1));

        mediumQuestions.add(new Question("What does `Gdx.gl.glClear()` do?",
            Arrays.asList("Clears the screen", "Resets the game state", "Sets the clear color"), 0));

        mediumQuestions.add(new Question("Which class is used to manage a 3D camera in LibGDX?",
            Arrays.asList("PerspectiveCamera", "OrthographicCamera", "Camera3D"), 0));

        mediumQuestions.add(new Question("Which class in LibGDX is used for handling user interface (UI) elements?",
            Arrays.asList("Skin", "Table", "Stage"), 2));

        mediumQuestions.add(new Question("What is the default background color for a LibGDX game window?",
            Arrays.asList("White", "Black", "Transparent"), 1));

        mediumQuestions.add(new Question("Which class is used to handle 3D model rendering in LibGDX?",
            Arrays.asList("ModelBatch", "SpriteBatch", "MeshBatch"), 0));

        mediumQuestions.add(new Question("Which method is used to update the viewport in LibGDX?",
            Arrays.asList("update()", "resize()", "setViewport()"), 1));

        mediumQuestions.add(new Question("Which class is used to manage screen transitions in LibGDX?",
            Arrays.asList("Screen", "Game", "StateManager"), 0));





        List<Question> hardQuestions = new ArrayList<>();
        hardQuestions.add(new Question("Which method in LibGDX allows you to manage OpenGL state changes directly?",
            Arrays.asList("Gdx.gl.glEnable()", "Gdx.gl.glState()", "Gdx.gl.glPushMatrix()"), 0));
        hardQuestions.add(new Question("Which class in LibGDX allows you to compile and manage custom shaders?",
            Arrays.asList("ShaderProgram", "GLShader", "ShaderManager"), 0));
        hardQuestions.add(new Question("How can you set a custom texture filtering mode in LibGDX for a `Texture`?",
            Arrays.asList("setFilter()", "setTextureFilter()", "Gdx.gl.glTexParameteri()"), 2));
        hardQuestions.add(new Question("What is the correct way to apply a custom shader to a `SpriteBatch` in LibGDX?",
            Arrays.asList("batch.setShader()", "batch.beginShader()", "batch.applyShader()"), 0));
        hardQuestions.add(new Question("Which OpenGL feature in LibGDX can be used to improve rendering performance by reducing state changes?",
            Arrays.asList("Vertex Array Objects (VAOs)", "Framebuffers", "VBOs"), 0));
        hardQuestions.add(new Question("What method is used to set the depth function for 3D rendering in LibGDX?",
            Arrays.asList("Gdx.gl.glDepthFunc()", "Gdx.gl.glDepthTest()", "Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)"), 0));
        hardQuestions.add(new Question("Which class is responsible for handling perspective matrix transformations in 3D LibGDX rendering?",
            Arrays.asList("PerspectiveCamera", "Matrix4", "ModelBatch"), 1));
        hardQuestions.add(new Question("Which method in LibGDX allows you to perform manual depth buffer clearing?",
            Arrays.asList("glClearDepthf()", "glClearColor()", "glClear()"), 0));
        hardQuestions.add(new Question("How do you enable wireframe mode in LibGDX?",
            Arrays.asList("Gdx.gl.glPolygonMode(GL20.GL_FRONT_AND_BACK, GL20.GL_LINE)", "Gdx.gl.glWireframeMode()", "Gdx.gl.glEnable(GL20.GL_WIREFRAME_MODE)"), 0));
        hardQuestions.add(new Question("What method is used to disable depth testing in LibGDX?",
            Arrays.asList("Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)", "Gdx.gl.glDepthFunc(GL20.GL_EQUAL)", "Gdx.gl.glDisable(GL20.GL_BLEND)"), 0));
        hardQuestions.add(new Question("Which class in LibGDX is used to handle custom model batch rendering in 3D?",
            Arrays.asList("ModelBatch", "SpriteBatch", "MeshBatch"), 0));
        hardQuestions.add(new Question("What is the effect of using a `ShaderProgram` in LibGDX when working with 3D models?",
            Arrays.asList("It allows you to apply custom shaders to vertices and fragments", "It provides automatic lighting effects", "It optimizes texture mapping for 3D models"), 0));
        hardQuestions.add(new Question("What method would you use to manipulate the view matrix in LibGDX for 3D camera transformations?",
            Arrays.asList("camera.update()", "camera.setView()", "camera.apply()"), 0));
        hardQuestions.add(new Question("How do you perform frustum culling in LibGDX for 3D models?",
            Arrays.asList("Camera.frustum.update()", "Camera.calculateViewMatrix()", "Camera.applyFrustum()"), 0));
        hardQuestions.add(new Question("What is the correct function to create a `Sprite` from a `TextureRegion` in LibGDX?",
            Arrays.asList("new Sprite(textureRegion)", "Sprite.createFromRegion(textureRegion)", "new TextureRegionSprite(textureRegion)"), 0));
        hardQuestions.add(new Question("Which class in LibGDX is specifically designed for 2D physics using Box2D?",
            Arrays.asList("World", "PhysicsWorld", "Box2DWorld"), 0));
        hardQuestions.add(new Question("How can you enable continuous collision detection in LibGDX's Box2D physics?",
            Arrays.asList("body.setContinuousCollision(true)", "world.setContinuousCollisions(true)", "world.setAutoSleep(false)"), 0));
        hardQuestions.add(new Question("Which method in Box2D allows you to prevent objects from sleeping when not moving?",
            Arrays.asList("world.setAutoSleep(false)", "world.setFixedTimeStep(true)", "world.setSleepingAllowed(false)"), 0));
        hardQuestions.add(new Question("How do you set the time step for the Box2D world simulation in LibGDX?",
            Arrays.asList("world.step(timeStep, velocityIterations, positionIterations)", "world.setFixedTimeStep(timeStep)", "world.setSimulationTime(timeStep)"), 0));
        hardQuestions.add(new Question("What is the main advantage of using `ModelBatch` over `SpriteBatch` in LibGDX?",
            Arrays.asList("It is optimized for rendering 3D models", "It provides a more efficient way to render 2D images", "It reduces texture binding overhead"), 0));
        hardQuestions.add(new Question("Which method is used to load assets asynchronously in LibGDX for optimized resource management?",
            Arrays.asList("AssetManager.load()", "Assets.load()", "ResourceManager.load()"), 0));
        hardQuestions.add(new Question("Which class handles the configuration of frame buffers in LibGDX?",
            Arrays.asList("FrameBuffer", "FboManager", "GLFramebuffer"), 0));
        hardQuestions.add(new Question("How do you handle lighting and shadows in a 3D scene using LibGDX?",
            Arrays.asList("By using shaders with custom lighting models", "By applying directional lights directly to models", "By enabling built-in shadow maps in the camera class"), 0));
        hardQuestions.add(new Question("Which class in LibGDX allows you to manage OpenGL frame buffers?",
            Arrays.asList("FrameBuffer", "FBO", "GLFramebuffer"), 0));
        hardQuestions.add(new Question("What method can be used to set a camera's field of view (FOV) in LibGDX?",
            Arrays.asList("camera.setFieldOfView()", "camera.setPerspective()", "camera.fieldOfView = fov"), 1));
        hardQuestions.add(new Question("Which class in LibGDX is responsible for managing textures and rendering in a 3D scene?",
            Arrays.asList("Texture", "TextureRegion", "ModelBatch"), 2));
        hardQuestions.add(new Question("Which method is used to update the `Gdx.graphics` context in LibGDX when creating a custom renderer?",
            Arrays.asList("Gdx.gl.glFlush()", "Gdx.graphics.update()", "Gdx.gl.glFinish()"), 0));
        hardQuestions.add(new Question("Which method in Box2D is used to retrieve the contact list between two bodies?",
            Arrays.asList("world.getContacts()", "body.getContacts()", "world.getContactList()"), 0));
        hardQuestions.add(new Question("Which class in LibGDX is used for handling mesh deformation in 3D models?",
            Arrays.asList("ModelInstance", "MeshDeformer", "VertexBuffer"), 0));
        hardQuestions.add(new Question("What feature in LibGDX allows for the simulation of complex 3D collisions using mesh data?",
            Arrays.asList("Mesh collision detection", "Bounding volume hierarchy (BVH)", "Convex hull collision"), 2));
        hardQuestions.add(new Question("Which class is used to load 3D models asynchronously in LibGDX?",
            Arrays.asList("AssetManager", "ModelLoader", "ModelBatch"), 0));
        hardQuestions.add(new Question("Which OpenGL feature in LibGDX allows you to modify the depth range for a specific object in 3D rendering?",
            Arrays.asList("glDepthRange()", "glDepthFunc()", "glEnable(GL20.GL_DEPTH_TEST)"), 0));
        hardQuestions.add(new Question("How would you efficiently render large numbers of 3D models in LibGDX with minimal CPU overhead?",
            Arrays.asList("By using instancing techniques", "By enabling back-face culling", "By using model batching only"), 0));
        hardQuestions.add(new Question("Which method in LibGDX is used to update the camera's projection matrix after changes to field of view?",
            Arrays.asList("camera.updateProjectionMatrix()", "camera.updateViewMatrix()", "camera.applyProjection()"), 0));
        hardQuestions.add(new Question("How do you use normal maps in LibGDX to add more detailed surface lighting effects?",
            Arrays.asList("By using a custom fragment shader that samples the normal map", "By using the built-in `NormalMapShader` class", "By applying the normal map directly to `SpriteBatch`"), 0));





        questionMap.put(QuestionScreen.Difficulty.EASY, easyQuestions);
        questionMap.put(QuestionScreen.Difficulty.MEDIUM, mediumQuestions);
        questionMap.put(QuestionScreen.Difficulty.HARD, hardQuestions);
    }

    public Map<QuestionScreen.Difficulty, List<Question>> getQuestionMap() {
        return questionMap;
    }



}
