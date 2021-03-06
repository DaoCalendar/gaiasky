/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.render.system;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import gaiasky.render.IRenderable;
import gaiasky.render.IShapeRenderable;
import gaiasky.render.SceneGraphRenderer;
import gaiasky.render.SceneGraphRenderer.RenderGroup;
import gaiasky.scenegraph.camera.ICamera;

import java.util.List;

public class ShapeRenderSystem extends AbstractRenderSystem {

    private ShapeRenderer shapeRenderer;

    public ShapeRenderSystem(RenderGroup rg, float[] alphas) {
        super(rg, alphas, null);
        this.shapeRenderer = new ShapeRenderer();

    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera, double t) {
        shapeRenderer.begin(ShapeType.Line);
        renderables.forEach(r ->{
            IShapeRenderable sr = (IShapeRenderable) r;
            sr.render(shapeRenderer, rc, getAlpha(r), camera);
        });
        shapeRenderer.end();
    }

    @Override
    public void resize(int w, int h) {
        super.resize(w, h);
        updateBatchSize(w, h);
    }

    @Override
    public void updateBatchSize(int w, int h) {
        shapeRenderer.setProjectionMatrix(shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h));
    }

}
