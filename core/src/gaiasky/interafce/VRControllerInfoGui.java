package gaiasky.interafce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import gaiasky.event.EventManager;
import gaiasky.event.Events;
import gaiasky.util.GlobalConf;
import gaiasky.util.GlobalResources;

public class VRControllerInfoGui extends AbstractGui {

    protected Container<Table> container;
    protected Table contents;

    public VRControllerInfoGui() {
    }

    @Override
    public void initialize(AssetManager assetManager) {
        // User interface
        float h = GlobalConf.screen.BACKBUFFER_HEIGHT;
        float w = GlobalConf.screen.BACKBUFFER_WIDTH;
        Viewport vp = new ScreenViewport();
        ui = new Stage(vp, GlobalResources.spriteBatch);
        vp.update((int) w, (int) h, true);
        skin = GlobalResources.skin;

        container = new Container<>();
        container.setFillParent(true);
        container.bottom().left();

        contents = new Table();
        Texture vrctrl_tex = new Texture(Gdx.files.internal("img/controller/hud-info-ui.png"));
        vrctrl_tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        Image vrctrl = new Image(vrctrl_tex);
        float texScale = 0.7f * GlobalConf.screen.SCREEN_HEIGHT / 1780f;
        vrctrl.setScale(texScale);
        contents.addActor(vrctrl);

        float tw = vrctrl_tex.getWidth() * texScale;
        float th = vrctrl_tex.getHeight() * texScale;

        container.padLeft((w - tw) / 2f + hoffset);
        container.padBottom((h - th) / 2f);
        container.setActor(contents);
        contents.setVisible(false);

        rebuildGui();

        EventManager.instance.subscribe(this, Events.DISPLAY_VR_CONTROLLER_HINT_CMD);
    }

    @Override
    public void doneLoading(AssetManager assetManager) {

    }

    @Override
    public void update(double dt) {
        super.update(dt);
    }

    @Override
    protected void rebuildGui() {
        if (ui != null) {
            ui.clear();
            if (container != null)
                ui.addActor(container);
        }
    }

    @Override
    public boolean mustDraw(){
        return contents != null && contents.isVisible();
    }

    @Override
    public void notify(final Events event, final Object... data) {

        switch (event) {
            case DISPLAY_VR_CONTROLLER_HINT_CMD:
                contents.setVisible((Boolean) data[0]);
                break;
            default:
                break;
        }
    }
}
