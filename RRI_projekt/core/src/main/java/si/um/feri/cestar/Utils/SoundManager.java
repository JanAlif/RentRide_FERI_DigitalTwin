package si.um.feri.cestar.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {

    private static SoundManager instance;

    private Music menuMusic;
    private Music gameMusic;

    private Sound clickSound;
    private Sound errorSound;

    private static final String MUSIC_ENABLED_KEY = "musicEnabled";
    private static final String SOUND_ENABLED_KEY = "soundEnabled";

    private Preferences preferences;

    private SoundManager() {
        preferences = Gdx.app.getPreferences("GameSettings");
        initializePreferences();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void initializePreferences() {
        if (!preferences.contains(MUSIC_ENABLED_KEY)) {
            preferences.putBoolean(MUSIC_ENABLED_KEY, true);
        }
        if (!preferences.contains(SOUND_ENABLED_KEY)) {
            preferences.putBoolean(SOUND_ENABLED_KEY, true);
        }
        preferences.flush();
    }


    public void playMenuMusic() {
        if (!isMusicEnabled()) return;

        if (menuMusic == null) {

            menuMusic.setLooping(true);
            menuMusic.setVolume(getMusicVolume());
        }
        if (!menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }

    public void stopMenuMusic() {
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.stop();
        }
    }

    public void playGameMusic() {
        if (!isMusicEnabled()) return;

        if (gameMusic == null) {
            gameMusic = Gdx.audio.newMusic(Gdx.files.internal("Sound/backgroundMusic.mp3"));
            gameMusic.setLooping(true);
            gameMusic.setVolume(getMusicVolume());
        }
        if (!gameMusic.isPlaying()) {
            gameMusic.play();
        }
    }

    public void stopGameMusic() {
        if (gameMusic != null && gameMusic.isPlaying()) {
            gameMusic.stop();
        }
    }


    public void loadSoundEffects() {
        if (!isSoundEnabled()) return;

        if (clickSound == null) {
            clickSound = Gdx.audio.newSound(Gdx.files.internal("audio/click.wav"));
        }
        if (errorSound == null) {
            errorSound = Gdx.audio.newSound(Gdx.files.internal("audio/error.wav"));
        }
    }

    public void playClickSound() {
        if (!isSoundEnabled()) return;

        if (clickSound == null) {
            loadSoundEffects();
        }
        if (clickSound != null) {
            clickSound.play(getSoundVolume());
        }
    }

    public void playErrorSound() {
        if (!isSoundEnabled()) return;

        if (errorSound == null) {
            loadSoundEffects();
        }
        if (errorSound != null) {
            errorSound.play(getSoundVolume());
        }
    }


    public boolean isMusicEnabled() {
        return preferences.getBoolean(MUSIC_ENABLED_KEY, true);
    }

    public void setMusicEnabled(boolean enabled) {
        preferences.putBoolean(MUSIC_ENABLED_KEY, enabled);
        preferences.flush();

        if (!enabled) {
            stopMenuMusic();
            stopGameMusic();
        }
    }

    public boolean isSoundEnabled() {
        return preferences.getBoolean(SOUND_ENABLED_KEY, true);
    }

    public void setSoundEnabled(boolean enabled) {
        preferences.putBoolean(SOUND_ENABLED_KEY, enabled);
        preferences.flush();

        if (!enabled) {
            disposeSoundEffects();
        } else {
            loadSoundEffects();
        }
    }


    private float getMusicVolume() {
        return 0.8f;
    }

    private float getSoundVolume() {
        return 1.0f;
    }


    public void disposeSoundEffects() {
        if (clickSound != null) {
            clickSound.dispose();
            clickSound = null;
        }
        if (errorSound != null) {
            errorSound.dispose();
            errorSound = null;
        }
    }

    public void disposeAll() {
        disposeMenuMusic();
        disposeGameMusic();
        disposeSoundEffects();
    }

    public void disposeMenuMusic() {
        if (menuMusic != null) {
            menuMusic.dispose();
            menuMusic = null;
        }
    }

    public void disposeGameMusic() {
        if (gameMusic != null) {
            gameMusic.dispose();
            gameMusic = null;
        }
    }

}
