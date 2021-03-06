/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.render.system;

import gaiasky.render.IModelRenderable;
import gaiasky.render.IRenderable;
import gaiasky.render.SceneGraphRenderer;
import gaiasky.render.SceneGraphRenderer.RenderGroup;
import gaiasky.scenegraph.camera.ICamera;
import gaiasky.util.comp.ModelComparator;
import gaiasky.util.gdx.IntModelBatch;
import org.lwjgl.opengl.GL41;

import java.util.List;

public class ModelBatchTessellationRenderSystem extends AbstractRenderSystem {
    private IntModelBatch batch;

    /**
     * Creates a new model batch render component.
     *
     * @param rg     The render group.
     * @param alphas The alphas list.
     * @param batch  The model batch.
     */
    public ModelBatchTessellationRenderSystem(RenderGroup rg, float[] alphas, IntModelBatch batch) {
        super(rg, alphas, null);
        this.batch = batch;
        comp = new ModelComparator<>();
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera, double t) {
        if (mustRender()) {
            // Triangles for tessellation
            GL41.glPatchParameteri(GL41.GL_PATCH_VERTICES, 3);
            batch.begin(camera.getCamera());
            renderables.forEach(r ->{
                IModelRenderable s = (IModelRenderable) r;
                s.render(batch, getAlpha(s), t, rc);
            });
            batch.end();

        }
    }

    protected boolean mustRender() {
        return true;
    }

}
