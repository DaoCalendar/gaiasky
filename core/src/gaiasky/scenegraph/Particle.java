/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import gaiasky.GaiaSky;
import gaiasky.event.EventManager;
import gaiasky.event.Events;
import gaiasky.event.IObserver;
import gaiasky.render.*;
import gaiasky.render.ComponentTypes.ComponentType;
import gaiasky.render.SceneGraphRenderer.RenderGroup;
import gaiasky.render.system.LineRenderSystem;
import gaiasky.scenegraph.camera.FovCamera;
import gaiasky.scenegraph.camera.ICamera;
import gaiasky.util.Constants;
import gaiasky.util.GlobalConf;
import gaiasky.util.Nature;
import gaiasky.util.color.ColorUtils;
import gaiasky.util.coord.AstroUtils;
import gaiasky.util.gdx.IntModelBatch;
import gaiasky.util.math.Vector2d;
import gaiasky.util.math.Vector3d;
import gaiasky.util.time.ITimeFrameProvider;
import net.jafama.FastMath;

import java.util.Random;

/**
 * A single point particle.
 *
 * @deprecated Only the Sun uses this via the Star subclass. Move to star group.
 * @author Toni Sagrista
 */
@Deprecated
public class Particle extends CelestialBody implements IStarFocus, ILineRenderable {

    private static final float DISC_FACTOR = 1.5f;

    private static Random rnd = new Random();

    protected static float thpointTimesFovfactor;
    protected static float thupOverFovfactor;
    protected static float thdownOverFovfactor;
    protected static float innerRad;
    protected static float fovFactor;
    protected static ParamUpdater paramUpdater;

    protected static class ParamUpdater implements IObserver {
        public ParamUpdater() {
            super();
            EventManager.instance.subscribe(this, Events.FOV_CHANGE_NOTIFICATION, Events.STAR_POINT_SIZE_CMD);
        }

        @Override
        public void notify(final Events event, final Object... data) {
            switch (event) {
                case FOV_CHANGE_NOTIFICATION:
                    fovFactor = (Float) data[1];
                    thpointTimesFovfactor = (float) GlobalConf.scene.STAR_THRESHOLD_POINT * fovFactor;
                    thupOverFovfactor = (float) Constants.THRESHOLD_UP / fovFactor;
                    thdownOverFovfactor = (float) Constants.THRESHOLD_DOWN / fovFactor;
                    break;
                case STAR_POINT_SIZE_CMD:
                    innerRad = (0.004f * DISC_FACTOR + (Float) data[0] * 0.008f) * 1.5f;
                    break;
                default:
                    break;
            }
        }
    }

    static {
        if (GaiaSky.instance != null) {
            fovFactor = GaiaSky.instance.getCameraManager().getFovFactor();
        } else {
            fovFactor = 1f;
        }
        thpointTimesFovfactor = (float) GlobalConf.scene.STAR_THRESHOLD_POINT * fovFactor;
        thupOverFovfactor = (float) Constants.THRESHOLD_UP / fovFactor;
        thdownOverFovfactor = (float) Constants.THRESHOLD_DOWN / fovFactor;
        float psize = GlobalConf.scene.STAR_POINT_SIZE < 0 ? 8 : GlobalConf.scene.STAR_POINT_SIZE;
        innerRad = (0.004f * DISC_FACTOR + psize * 0.008f) * 1.5f;
        paramUpdater = new ParamUpdater();
    }

    @Override
    public double THRESHOLD_NONE() {
        return (float) GlobalConf.scene.STAR_THRESHOLD_NONE;
    }

    @Override
    public double THRESHOLD_POINT() {
        return (float) GlobalConf.scene.STAR_THRESHOLD_POINT;
    }

    @Override
    public double THRESHOLD_QUAD() {
        return (float) GlobalConf.scene.STAR_THRESHOLD_QUAD;
    }

    /**
     * Must be updated every cycle
     **/
    public static boolean renderOn = false;

    /**
     * Proper motion in cartesian coordinates [U/yr]
     **/
    public Vector3 pm;
    /**
     * MuAlpha [mas/yr], Mudelta [mas/yr], radvel [km/s]
     **/
    public Vector3 pmSph;

    /**
     * Source of this star:
     * <ul>
     * <li>-1: Unknown</li>
     * <li>1: Gaia</li>
     * <li>2: Hipparcos (HYG)</li>
     * <li>3: Tycho</li>
     * </ul>
     */
    public byte catalogSource = -1;

    public double computedSize;
    double radius;
    boolean randomName = false;
    boolean hasPm = false;

    public Particle() {
        this.parentName = ROOT_NAME;
    }

    /**
     * Creates a new star.
     *
     * @param pos     Cartesian position, in equatorial coordinates and in internal
     *                units.
     * @param appmag  Apparent magnitude.
     * @param absmag  Absolute magnitude.
     * @param colorbv The B-V color index.
     * @param names    The labels or names.
     * @param starid  The star unique id.
     */
    public Particle(Vector3d pos, float appmag, float absmag, float colorbv, String[] names, long starid) {
        this();
        this.pos = pos;
        this.names = names;
        this.appmag = appmag;
        this.absmag = absmag;
        this.colorbv = colorbv;
        this.id = starid;

        if (this.names == null || this.names.length == 0) {
            randomName = true;
            this.setName("star_" + rnd.nextInt(10000000));
        }
        this.pm = new Vector3();
        this.pmSph = new Vector3();
    }

    public Particle(Vector3d pos, float appmag, float absmag, float colorbv, String[] names, float ra, float dec, long starid) {
        this(pos, appmag, absmag, colorbv, names, starid);
        this.posSph = new Vector2d(ra, dec);

    }

    public Particle(Vector3d pos, Vector3 pm, Vector3 pmSph, float appmag, float absmag, float colorbv, String[] names, float ra, float dec, long starid) {
        this(pos, appmag, absmag, colorbv, names, starid);
        this.posSph = new Vector2d(ra, dec);
        this.pm.set(pm);
        this.pmSph.set(pmSph);
        this.hasPm = this.pm.len2() != 0;

    }

    @Override
    public void initialize() {
        setDerivedAttributes();
        ct = new ComponentTypes(ComponentType.Galaxies);
        // Relation between our star size and actual star size (normalized for
        // the Sun, 695700 Km of radius
        radius = size * Constants.STAR_SIZE_FACTOR;
    }

    protected void setDerivedAttributes() {
        double flux = Math.pow(10, -absmag / 2.5f);
        setRGB(colorbv);

        // Calculate size - This contains arbitrary boundary values to make
        // things nice on the render side
        size = (float) (Math.min((Math.pow(flux, 0.5f) * Constants.PC_TO_U * 0.16f), 1e9f) / DISC_FACTOR);
        computedSize = 0;
    }

    @Override
    public void update(ITimeFrameProvider time, final Vector3d parentTransform, ICamera camera) {
        update(time, parentTransform, camera, 1f);
    }

    /**
     * Re-implementation of update method of {@link CelestialBody} and
     * {@link SceneGraphNode}.
     */
    @Override
    public void update(ITimeFrameProvider time, final Vector3d parentTransform, ICamera camera, float opacity) {
        if (appmag <= GlobalConf.runtime.LIMIT_MAG_RUNTIME) {
            this.opacity = opacity;
            translation.set(parentTransform).add(pos);
            if (hasPm) {
                Vector3d pmv = aux3d1.get().set(pm).scl(AstroUtils.getMsSince(time.getTime(), AstroUtils.JD_J2015_5) * Nature.MS_TO_Y);
                translation.add(pmv);
            }
            distToCamera = translation.len();

            if (!copy) {
                addToRender(this, RenderGroup.POINT_STAR);

                viewAngle = (radius / distToCamera) / camera.getFovFactor();
                viewAngleApparent = viewAngle * GlobalConf.scene.STAR_BRIGHTNESS;

                addToRenderLists(camera);
            }

            // Compute nested
            if (children != null) {
                for (int i = 0; i < children.size; i++) {
                    SceneGraphNode child = children.get(i);
                    child.update(time, parentTransform, camera, opacity);
                }
            }
        }

        innerRad = 0.01f * DISC_FACTOR + GlobalConf.scene.STAR_POINT_SIZE * 0.016f;
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if(GaiaSky.instance.isOn(ct)) {
            if (camera.getCurrent() instanceof FovCamera) {
                // Render as point, do nothing
            } else {

                if (viewAngleApparent >= thpointTimesFovfactor) {
                    addToRender(this, RenderGroup.BILLBOARD_STAR);
                }
                if (viewAngleApparent >= thpointTimesFovfactor / GlobalConf.scene.PM_NUM_FACTOR && this.hasPm) {
                    addToRender(this, RenderGroup.LINE);
                }
            }
            if (renderText() && camera.isVisible(GaiaSky.instance.time, this)) {
                addToRender(this, RenderGroup.FONT_LABEL);
            }
        }
    }

    protected boolean addToRender(IRenderable renderable, RenderGroup rg) {
        if (renderOn) {
            SceneGraphRenderer.render_lists.get(rg.ordinal()).add(renderable);
            return true;
        }
        return false;
    }

    /**
     * Model rendering
     */
    @Override
    public void render(IntModelBatch modelBatch, float alpha, double t, RenderingContext rc) {
        // Void
    }

    /**
     * Sets the color
     *
     * @param bv B-V color index
     */
    protected void setRGB(float bv) {
        if (cc == null)
            cc = ColorUtils.BVtoRGB(bv);
        setColor2Data();
    }

    @Override
    public float getInnerRad() {
        return innerRad;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    public boolean isStar() {
        return true;
    }

    @Override
    public boolean renderText() {
        return computedSize > 0 && GaiaSky.instance.isOn(ComponentType.Labels) && viewAngleApparent >= (TH_OVER_FACTOR / GaiaSky.instance.cam.getFovFactor());
    }

    @Override
    public float labelSizeConcrete() {
        float textSize = (float) (FastMath.tanh(viewAngle) * distToCamera * 1e5d);
        float alpha = Math.min((float) FastMath.atan(textSize / distToCamera), 1.e-3f);
        textSize = (float) (FastMath.tan(alpha) * distToCamera * 0.5d);
        return textSize * 1e2f;
    }

    @Override
    public float textScale() {
        return 0.2f;
    }

    @Override
    protected float labelFactor() {
        return 1.3e-1f;
    }

    @Override
    protected float labelMax() {
        return 0.01f;
    }

    public float getFuzzyRenderSize(ICamera camera) {
        computedSize = this.size;
        if (viewAngle > thdownOverFovfactor) {
            double dist = distToCamera;
            if (viewAngle > thupOverFovfactor) {
                dist = (float) radius / Constants.THRESHOLD_UP;
            }
            computedSize *= (dist / this.radius) * Constants.THRESHOLD_DOWN;
        }

        computedSize *= GlobalConf.scene.STAR_BRIGHTNESS * 0.1f;
        return (float) computedSize;
    }

    @Override
    public void doneLoading(AssetManager manager) {
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    @Override
    public int getStarCount() {
        return 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends SceneGraphNode> T getSimpleCopy() {
        Particle copy = super.getSimpleCopy();
        copy.pm = this.pm;
        copy.hasPm = this.hasPm;
        return (T) copy;
    }

    /**
     * Line renderer. Renders proper motion
     *
     * @param renderer
     * @param camera
     * @param alpha
     */
    @Override
    public void render(LineRenderSystem renderer, ICamera camera, float alpha) {
        Vector3 p1 = translation.setVector3(aux3f1.get());
        Vector3 ppm = aux3f2.get().set(pm).scl(GlobalConf.scene.PM_LEN_FACTOR);
        Vector3 p2 = ppm.add(p1);

        // Mualpha -> red channel
        // Mudelta -> green channel
        // Radvel -> blue channel
        // Min value per channel = 0.2
        final double mumin = -80;
        final double mumax = 80;
        final double maxmin = mumax - mumin;
        renderer.addLine(this, p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, (float) ((pmSph.x - mumin) / maxmin) * 0.8f + 0.2f, (float) ((pmSph.y - mumin) / maxmin) * 0.8f + 0.2f, pmSph.z * 0.8f + 0.2f, alpha * this.opacity);
    }

    @Override
    public int getGlPrimitive() {
        return GL20.GL_LINES;
    }

    protected float getThOverFactorScl() {
        return fovFactor;
    }

    @Override
    protected boolean checkHitCondition() {
        return this.octant == null || this.octant.observed;
    }

    @Override
    public int getCatalogSource() {
        return catalogSource;
    }

    @Override
    public int getHip() {
        return -1;
    }

    @Override
    public double getClosestDistToCamera() {
        return this.distToCamera;
    }

    @Override
    public String getClosestName() {
        return this.getName();
    }

    @Override
    public Vector3d getClosestPos(Vector3d out) {
        return translation.put(out);
    }

    @Override
    public Vector3d getClosestAbsolutePos(Vector3d out) {
        return getAbsolutePosition(out);
    }

    @Override
    public float[] getClosestCol() {
        return cc;
    }

    @Override
    public double getClosestSize() {
        return this.size;
    }

    @Override
    public double getMuAlpha() {
        if (this.pmSph != null)
            return this.pmSph.x;
        else
            return 0;
    }

    @Override
    public double getMuDelta() {
        if (this.pmSph != null)
            return this.pmSph.y;
        else
            return 0;
    }

    @Override
    public double getRadialVelocity() {
        if (this.pmSph != null)
            return this.pmSph.z;
        else
            return 0;
    }

    @Override
    public float getLineWidth() {
        return 1;
    }

}
