/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import gaiasky.render.I3DTextRenderable;
import gaiasky.render.IModelRenderable;
import gaiasky.render.RenderingContext;
import gaiasky.render.SceneGraphRenderer.RenderGroup;
import gaiasky.render.system.FontRenderSystem;
import gaiasky.scenegraph.camera.ICamera;
import gaiasky.scenegraph.component.ModelComponent;
import gaiasky.util.Constants;
import gaiasky.util.Logger;
import gaiasky.util.coord.Coordinates;
import gaiasky.util.gdx.IntModelBatch;
import gaiasky.util.gdx.g2d.ExtSpriteBatch;
import gaiasky.util.gdx.shader.ExtShaderProgram;
import gaiasky.util.math.Matrix4d;
import gaiasky.util.math.Vector3d;
import gaiasky.util.time.ITimeFrameProvider;

/**
 * A model which renders as a background, unaffected by the camera. It should
 * usually be a flipped sphere or cube map.
 * 
 * @author tsagrista
 *
 */
public class BackgroundModel extends FadeNode implements IModelRenderable, I3DTextRenderable {
    protected String transformName;
    public ModelComponent mc;
    private boolean label, label2d;

    private RenderGroup renderGroupModel = RenderGroup.MODEL_DIFFUSE;

    public BackgroundModel() {
        super();
        localTransform = new Matrix4();
    }

    @Override
    public void initialize() {
        // Force texture loading
        mc.forceInit = true;
        mc.initialize();
        mc.env.set(new ColorAttribute(ColorAttribute.AmbientLight, cc[0], cc[1], cc[2], 1));
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);

        // Initialize transform.
        localTransform.scl(size);

        if (transformName != null) {
            Class<Coordinates> c = Coordinates.class;
            try {
                Method m = ClassReflection.getMethod(c, transformName);
                Matrix4d trf = (Matrix4d) m.invoke(null);
                Matrix4 aux = trf.putIn(new Matrix4());
                localTransform.mul(aux);
            } catch (ReflectionException e) {
                Logger.getLogger(this.getClass()).error("Error getting/invoking method Coordinates." + transformName + "()");
            }
        } else {
            // Equatorial, nothing
        }

        // Must rotate due to orientation of createCylinder
        localTransform.rotate(0, 1, 0, 90);


        // Model
        mc.doneLoading(manager, localTransform, cc);
        // Disable depth
        //mc.setDepthTest(GL20.GL_NONE, false);

        // Label pos 3D
        if (label && labelPosition != null && !label2d) {
            labelPosition.scl(Constants.PC_TO_U);
        }

    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        // Render group never changes
        // Add to toRender list
        if (opacity > 0) {
            addToRender(this, renderGroupModel);
            if (label) {
                addToRender(this, RenderGroup.FONT_LABEL);
            }
        }
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    /**
     * Model rendering.
     */
    @Override
    public void render(IntModelBatch modelBatch, float alpha, double t, RenderingContext rc) {
        mc.update(alpha * cc[3] * opacity);
        modelBatch.render(mc.instance, mc.env);
    }

    /**
     * Label rendering.
     */
    @Override
    public void render(ExtSpriteBatch batch, ExtShaderProgram shader, FontRenderSystem sys, RenderingContext rc, ICamera camera) {
        if (label2d) {
            render2DLabel(batch, shader, rc, sys.fontDistanceField, camera, text(), (float) labelPosition.x, (float) labelPosition.y, (float) labelPosition.z);
        } else {
            // 3D distance font
            Vector3d pos = aux3d1.get();
            textPosition(camera, pos);
            shader.setUniformf("u_viewAngle", 90f);
            shader.setUniformf("u_viewAnglePow", 1);
            shader.setUniformf("u_thOverFactor", 1);
            shader.setUniformf("u_thOverFactorScl", 1);

            render3DLabel(batch, shader, sys.fontDistanceField, camera, rc, text(), pos, textScale(), textSize() * camera.getFovFactor());
        }

    }

    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

    public void setModel(ModelComponent mc) {
        this.mc = mc;
    }

    public void setLabel(Boolean label) {
        this.label = label;
    }

    public void setLabel2d(Boolean label2d) {
        this.label2d = label2d;
    }

    @Override
    public boolean renderText() {
        return label;
    }

    @Override
    public float[] textColour() {
        return this.cc;
    }

    @Override
    public float textSize() {
        return (float) distToCamera * 2e-3f;
    }

    @Override
    public float textScale() {
        return 1f;
    }

    @Override
    public void textPosition(ICamera cam, Vector3d out) {
        out.set(labelPosition).add(cam.getInversePos());
    }

    @Override
    public String text() {
        return names[0];
    }

    @Override
    public void textDepthBuffer() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
    }

    @Override
    public boolean isLabel() {
        return label;
    }

    @Override
    public float getTextOpacity(){
        return getOpacity();
    }

    public void setRendergroup(String rg) {
        this.renderGroupModel = RenderGroup.valueOf(rg);
    }

    @Override
    public void setSize(Double size) {
        this.size = (float) (size * Constants.DISTANCE_SCALE_FACTOR);
    }

    @Override
    public void setSize(Long size) {
        this.size = (float) (size * Constants.DISTANCE_SCALE_FACTOR);
    }

}
