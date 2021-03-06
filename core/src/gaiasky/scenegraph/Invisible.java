/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import gaiasky.event.EventManager;
import gaiasky.event.Events;
import gaiasky.render.ComponentTypes;
import gaiasky.render.ComponentTypes.ComponentType;
import gaiasky.render.RenderingContext;
import gaiasky.scenegraph.camera.ICamera;
import gaiasky.scenegraph.camera.NaturalCamera;
import gaiasky.util.Constants;
import gaiasky.util.Nature;
import gaiasky.util.coord.Coordinates;
import gaiasky.util.gdx.IntModelBatch;
import gaiasky.util.math.Vector3d;
import gaiasky.util.time.ITimeFrameProvider;

import java.time.Instant;

/**
 * The sole purpose of this class is to act as an invisible focus.
 * It can also optionally include a connection to a ray marching shader, implemented
 * in screen space.
 *
 * @author tsagrista
 */
public class Invisible extends CelestialBody {

    private String raymarchingShader;
    private boolean isOn = false;

    /**
     * Needed for reflection in {@link AbstractPositionEntity#getSimpleCopy()}
     **/
    @SuppressWarnings("unused")
    public Invisible() {
    }

    public Invisible(String name) {
        this(name, 500 * Constants.M_TO_U);
    }

    public Invisible(String name, double size) {
        super();
        this.setName(name);
        this.parentName = "Universe";
        this.size = (float) size;
        this.ct = new ComponentTypes(ComponentType.Invisible);
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);
        if(this.raymarchingShader != null && !this.raymarchingShader.isBlank())
           EventManager.instance.post(Events.RAYMARCHING_CMD, this.getName(), false, coordinates.getEquatorialCartesianCoordinates(Instant.now(), pos), this.raymarchingShader, new float[]{1f, 0f, 0f, 0f});
    }

    @Override
    public void render(IntModelBatch modelBatch, float alpha, double t, RenderingContext rc) {
    }

    @Override
    public double THRESHOLD_NONE() {
        return 0;
    }

    @Override
    public double THRESHOLD_QUAD() {
        return 0;
    }

    @Override
    public double THRESHOLD_POINT() {
        return 0;
    }

    @Override
    public float getInnerRad() {
        return 0;
    }

    @Override
    protected float labelFactor() {
        return 0;
    }

    @Override
    protected float labelMax() {
        return 0;
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if (names != null) {
            camera.checkClosestBody(this);
        }
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
        forceUpdateLocalValues(time, false);
        if(raymarchingShader != null){
            // Check enable/disable
            if(viewAngleApparent >  Math.toRadians(0.001)){
                if(!isOn) {
                    // Turn on
                    EventManager.instance.post(Events.RAYMARCHING_CMD, this.getName(), true, pos);
                    isOn = true;
                }
            } else {
                if(isOn){
                    // Turn off
                    EventManager.instance.post(Events.RAYMARCHING_CMD, this.getName(), false, pos);
                    isOn = false;
                }
            }
        }
    }

    protected void forceUpdateLocalValues(ITimeFrameProvider time, boolean force) {
        if (time.getDt() != 0 || force) {
            Vector3d aux3 = aux3d1.get();
            // Load this objects's equatorial cartesian coordinates into pos
            coordinatesTimeOverflow = coordinates.getEquatorialCartesianCoordinates(time.getTime(), pos) == null;


            // Convert to cartesian coordinates and put them in aux3 vector
            Coordinates.cartesianToSpherical(pos, aux3);
            posSph.set((float) (Nature.TO_DEG * aux3.x), (float) (Nature.TO_DEG * aux3.y));
            // Update angle
            if (rc != null)
                rc.update(time);
        }
    }

	@Override
	public void addHit(Vector3d p0, Vector3d p1, NaturalCamera camera, Array<IFocus> hits) {
	}

	public void setShader(String shader){
        this.raymarchingShader = shader;
    }

}
