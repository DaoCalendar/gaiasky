/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.interafce;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import gaiasky.util.GlobalConf;
import gaiasky.util.I18n;
import gaiasky.util.scene2d.OwnLabel;
import gaiasky.util.scene2d.TextureWidget;

public class MinimapWindow extends GenericDialog {
    private OwnLabel mapName;
    private MinimapWidget minimap;


    public MinimapWindow(Stage stage, Skin skin) {
        super(I18n.txt("gui.minimap.title"), skin, stage);
        minimap = new MinimapWidget(skin);

        setModal(false);
        setCancelText(I18n.txt("gui.close"));

        // Build
        buildSuper();
        // Pack
        pack();
    }

    @Override
    protected void build() {
        float pb = 10 * GlobalConf.UI_SCALE_FACTOR;
        mapName = new OwnLabel("", skin, "header");
        OwnLabel headerSide = new OwnLabel(I18n.txt("gui.minimap.side"), skin);
        Container<TextureWidget> mapSide = new Container<>();
        mapSide.setActor(minimap.getSideProjection());
        OwnLabel headerTop = new OwnLabel(I18n.txt("gui.minimap.top"), skin);
        Container<TextureWidget> mapTop = new Container<>();
        mapTop.setActor(minimap.getTopProjection());

        content.add(mapName).left().padBottom(pad10).row();

        content.add(headerSide).left().padBottom(pb).row();
        content.add(mapSide).left().padBottom(pb).row();

        content.add(headerTop).left().padBottom(pb).row();
        content.add(mapTop).left();

    }

    @Override
    protected void accept() {
    }

    @Override
    protected void cancel() {
    }

    private void updateMapName(String mapName){
        if(this.mapName != null)
            this.mapName.setText(mapName);
    }

    public void act(float delta) {
        super.act(delta);
        if(minimap != null) {
            minimap.update();
            String mapName = minimap.getCurrentName();
            if (!mapName.equals(this.mapName.getName())) {
                updateMapName(mapName);
            }
        }
    }


}
