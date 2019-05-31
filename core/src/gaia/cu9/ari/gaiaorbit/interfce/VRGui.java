package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import gaia.cu9.ari.gaiaorbit.render.ComponentTypes;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.Logger;

public class VRGui implements IGui {

    private IGui right;
    private IGui left;

    public VRGui(Class<? extends IGui> clazz, int hoffset) {
        super();
        try {
            right = clazz.newInstance();
            right.setHoffset(-hoffset);

            left = clazz.newInstance();
            left.setHoffset(hoffset);
        } catch (Exception e) {
            Logger.getLogger(this.getClass()).error(e);
        }

    }

    @Override
    public void dispose() {
        right.dispose();
        left.dispose();
    }

    @Override
    public void initialize(AssetManager assetManager) {
        right.initialize(assetManager);
        left.initialize(assetManager);
    }

    @Override
    public void doneLoading(AssetManager assetManager) {
        right.doneLoading(assetManager);
        left.doneLoading(assetManager);
    }

    @Override
    public void update(double dt) {
        right.update(dt);
        left.update(dt);
    }

    public IGui right() {
        return right;
    }

    public IGui left() {
        return left;
    }

    @Override
    public void render(int rw, int rh) {
    }

    @Override
    public void resize(int width, int height) {
        right.resize(width, height);
        left.resize(width, height);
    }

    @Override
    public void resizeImmediate(int width, int height) {
        right.resizeImmediate(width, height);
        left.resizeImmediate(width, height);
    }

    @Override
    public boolean cancelTouchFocus() {
        return false;
    }

    @Override
    public Stage getGuiStage() {
        return null;
    }

    @Override
    public void setSceneGraph(ISceneGraph sg) {
    }

    @Override
    public void setVisibilityToggles(ComponentTypes.ComponentType[] entities, ComponentTypes visible) {
    }

    @Override
    public Actor findActor(String name) {
        return null;
    }

    @Override
    public void setHoffset(int hoffset) {
        right.setHoffset(hoffset);
        left.setHoffset(hoffset);
    }

}
