/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import gaiasky.GaiaSky;
import gaiasky.render.ComponentTypes.ComponentType;
import gaiasky.render.I3DTextRenderable;
import gaiasky.render.IModelRenderable;
import gaiasky.render.RenderingContext;
import gaiasky.render.SceneGraphRenderer.RenderGroup;
import gaiasky.render.system.FontRenderSystem;
import gaiasky.scenegraph.camera.ICamera;
import gaiasky.scenegraph.component.ITransform;
import gaiasky.scenegraph.component.ModelComponent;
import gaiasky.util.Constants;
import gaiasky.util.Logger;
import gaiasky.util.coord.Coordinates;
import gaiasky.util.gdx.IntModelBatch;
import gaiasky.util.gdx.g2d.ExtSpriteBatch;
import gaiasky.util.gdx.shader.ExtShaderProgram;
import gaiasky.util.math.Vector3d;
import gaiasky.util.time.ITimeFrameProvider;

public class MeshObject extends FadeNode implements IModelRenderable, I3DTextRenderable, IVisibilitySwitch {

    private String description;
    private String transformName;
    private Matrix4 coordinateSystem;
    private Vector3 scale, axis, translate;
    private float degrees;
    // Additive blending or opaque
    private boolean additiveBlending = true;

    /** MODEL **/
    public ModelComponent mc;

    /** TRANSFORMATIONS - are applied each cycle **/
    public ITransform[] transformations;

    // Aux array
    private float[] auxArray;

    public MeshObject() {
        super();
        localTransform = new Matrix4();
        auxArray = new float[3];
    }

    public void initialize() {
        if (mc != null) {
            mc.initialize(true);
        }
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);
        if (mc != null) {
            try {
                mc.doneLoading(manager, localTransform, cc, true);
                if (additiveBlending) {
                    mc.setDepthTest(GL20.GL_NONE, false);
                }
            } catch (Exception e) {
                mc = null;
            }
        }

        recomputePositioning();

    }

    private void recomputePositioning(){
        if (mc != null) {
            if (coordinateSystem == null)
                coordinateSystem = new Matrix4();
            else
                coordinateSystem.idt();


            // REFSYS ROTATION
            if (transformName != null) {
                Class<Coordinates> c = Coordinates.class;
                try {
                    Method m = ClassReflection.getMethod(c, transformName);
                    Matrix4 trf = (Matrix4) m.invoke(null);
                    coordinateSystem.mul(trf);
                } catch (ReflectionException e) {
                    Logger.getLogger(this.getClass()).error("Error getting/invoking method Coordinates." + transformName + "()");
                }
            } else {
                // Equatorial, nothing
            }

            // ROTATION
            if (axis != null) {
                coordinateSystem.rotate(axis, degrees);
            }

            // TRANSLATION
            if (translate != null) {
                pos.set(translate);
                coordinateSystem.translate(translate.x, translate.y, translate.z);
            }

            // SCALE
            if (scale != null) {
                coordinateSystem.scale(scale.x, scale.y, scale.z);
            }

        }
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        super.updateLocal(time, camera);
        // Update light with global position
        if (mc != null) {
            mc.dLight.direction.set(1f, 0f, 0f);
            mc.dLight.color.set(1f, 1f, 1f, 1f);

            updateLocalTransform();
        }
    }

    /**
     * Update the local transform with the transform and the rotations/scales
     * necessary. Override if your model contains more than just the position
     * and size.
     */
    protected void updateLocalTransform() {
        setToLocalTransform(localTransform, true);
    }

    public void setToLocalTransform(Matrix4 localTransform, boolean forceUpdate) {
        if (forceUpdate) {
            float[] trn = translation.valuesf(auxArray);
            localTransform.idt().translate(trn[0], trn[1], trn[2]).scl(size).mul(coordinateSystem);
        } else {
            localTransform.set(this.localTransform);
        }

        // Apply transformations
        if (transformations != null)
            for (ITransform tc : transformations)
                tc.apply(localTransform);
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if (GaiaSky.instance.isInitialised() && GaiaSky.instance.isOn(ct) & opacity > 0) {
            if (!additiveBlending)
                addToRender(this, RenderGroup.MODEL_PIX_DUST);
            else
                addToRender(this, RenderGroup.MODEL_VERT_ADDITIVE);

            addToRender(this, RenderGroup.FONT_LABEL);
        }

    }

    /**
     * Model rendering
     */
    @Override
    public void render(IntModelBatch modelBatch, float alpha, double t, RenderingContext rc) {
        if (mc != null) {
            if (additiveBlending) {
                mc.update(localTransform, alpha * opacity, GL20.GL_ONE, GL20.GL_ONE);
                mc.setDepthTest(GL20.GL_NONE, false);
            } else {
                mc.update(localTransform, alpha * opacity);
            }
            // Render
            if (mc.instance != null)
                modelBatch.render(mc.instance, mc.env);
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setModel(ModelComponent mc) {
        this.mc = mc;
    }

    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    public void setTranslate(double[] tr) {
        translate = new Vector3((float) tr[0], (float) tr[1], (float) tr[2]);
    }

    public void setRotate(double[] rt) {
        axis = new Vector3((float) rt[0], (float) rt[1], (float) rt[2]);
        degrees = (float) rt[3];
    }

    public void setScale(double[] sc) {
        scale = new Vector3((float) sc[0], (float) sc[1], (float) sc[2]);
    }

    public void setAdditiveblending(Boolean additive) {
        this.additiveBlending = additive;
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

    @Override
    public boolean renderText() {
        return names != null && GaiaSky.instance.isOn(ComponentType.Labels) && this.opacity > 0;
    }

    /**
     * Label rendering
     */
    @Override
    public void render(ExtSpriteBatch batch, ExtShaderProgram shader, FontRenderSystem sys, RenderingContext rc, ICamera camera) {
        Vector3d pos = aux3d1.get();
        textPosition(camera, pos);
        shader.setUniformf("u_viewAngle", 90f);
        shader.setUniformf("u_viewAnglePow", 1f);
        shader.setUniformf("u_thOverFactor", 1f);
        shader.setUniformf("u_thOverFactorScl", 1f);
        render3DLabel(batch, shader, sys.fontDistanceField, camera, rc, text(), pos, textScale() * camera.getFovFactor(), textSize() * camera.getFovFactor());
    }

    @Override
    public float[] textColour() {
        return labelcolor;
    }

    @Override
    public float textSize() {
        return (float) distToCamera * .8e-3f;
    }

    @Override
    public float textScale() {
        return 0.2f;
    }

    @Override
    public void textPosition(ICamera cam, Vector3d out) {
        if (labelPosition != null)
            out.set(labelPosition).add(cam.getInversePos());
        else
            out.set(pos).add(cam.getInversePos());
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
        return true;
    }

    @Override
    public void setSize(Double size) {
        this.size = (float) (size * Constants.DISTANCE_SCALE_FACTOR);
    }

    @Override
    public void setSize(Long size) {
        this.size = (float) (size.doubleValue() * Constants.DISTANCE_SCALE_FACTOR);
    }

    @Override
    public float getTextOpacity() {
        return getOpacity();
    }

}
