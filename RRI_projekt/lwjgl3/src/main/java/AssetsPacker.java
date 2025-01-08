import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class AssetsPacker {

    private static final boolean DRAW_DEBUG_OUTLINE = false;

    public static void main(String[] args) {

        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.debug = DRAW_DEBUG_OUTLINE;
        settings.maxWidth = 4096;
        settings.maxHeight = 4096;


        TexturePacker.process(
            settings,
            "core/assets-raw/Images",
            "assets",
            "game-atlas"
        );

    }

}
