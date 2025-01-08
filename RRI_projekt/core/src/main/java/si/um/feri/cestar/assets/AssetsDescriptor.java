package si.um.feri.cestar.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AssetsDescriptor {

    // Texture Atlases
    public static final AssetDescriptor<TextureAtlas> GAMEPLAY =
        new AssetDescriptor<>(AssetsPaths.GAME_ATLAS, TextureAtlas.class);

    public static final AssetDescriptor<Skin> UI_SKIN =
        new AssetDescriptor<>(AssetsPaths.SKIN_JSON, Skin.class);

    private AssetsDescriptor() {
    }

}
