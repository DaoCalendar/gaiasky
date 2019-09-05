/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaia.cu9.ari.gaiaorbit.scenegraph.camera;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.IFocus;
import gaia.cu9.ari.gaiaorbit.scenegraph.IStarFocus;
import gaia.cu9.ari.gaiaorbit.scenegraph.camera.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public interface ICamera {

    /**
     * Returns the perspective camera.
     * 
     * @return The perspective camera.
     */
    PerspectiveCamera getCamera();

    /**
     * Sets the active camera
     * 
     * @param cam
     */
    void setCamera(PerspectiveCamera cam);

    PerspectiveCamera getCameraStereoLeft();

    PerspectiveCamera getCameraStereoRight();

    void setCameraStereoLeft(PerspectiveCamera cam);

    void setCameraStereoRight(PerspectiveCamera cam);

    PerspectiveCamera[] getFrontCameras();

    ICamera getCurrent();

    float getFovFactor();

    Vector3d getPos();

    void setPos(Vector3d pos);

    void setDirection(Vector3d dir);

    Vector3d getInversePos();

    Vector3d getDirection();

    Vector3d getVelocity();

    Vector3d getUp();

    Vector3d[] getDirections();

    int getNCameras();

    double getTranslateUnits();

    void setShift(Vector3d shift);

    Vector3d getShift();

    /**
     * Updates the camera.
     * 
     * @param dt
     *            The time since the las frame in seconds.
     * @param time
     *            The frame time provider (simulation time).
     */
    void update(double dt, ITimeFrameProvider time);

    void updateMode(CameraMode mode, boolean centerFocus, boolean postEvent);

    CameraMode getMode();

    void updateAngleEdge(int width, int height);

    /**
     * Gets the angle of the edge of the screen, diagonally. It assumes the
     * vertical angle is the field of view and corrects the horizontal using the
     * aspect ratio. It depends on the viewport size and the field of view
     * itself.
     * 
     * @return The angle in radians.
     */
    float getAngleEdge();

    CameraManager getManager();

    void render(int rw, int rh);

    /**
     * Gets the current velocity of the camera in km/h.
     * 
     * @return The velocity in km/h.
     */
    double getSpeed();

    /**
     * Gets the distance from the camera to the centre of our reference frame
     * (Sun)
     * 
     * @return The distance
     */
    double getDistance();

    /**
     * Returns the foucs if any
     * 
     * @return The foucs object if it is in focus mode. Null otherwise
     */
    IFocus getFocus();

    /**
     * Checks if this body is the current focus
     * 
     * @param cb
     *            The body
     * @return Whether the body is focus
     */
    boolean isFocus(IFocus cb);

    /**
     * Called after updating the body's distance to the cam, it updates the
     * closest body in the camera to figure out the camera near
     * 
     * @param  focus
     *            The body to check
     */
    void checkClosestBody(IFocus focus);

    IFocus getClosestBody();

    IFocus getSecondClosestBody();

    boolean isVisible(ITimeFrameProvider time, CelestialBody cb);

    boolean isVisible(ITimeFrameProvider time, double viewAngle, Vector3d pos, double distToCamera);

    void computeGaiaScan(ITimeFrameProvider time, CelestialBody cb);

    void resize(int width, int height);

    /**
     * Gets the current closest star to this camera
     * 
     * @return The closest star
     */
    IStarFocus getClosestStar();

    /**
     * Sets the current closest star to this camera. This will be only set if
     * the given star is closer than the current.
     * 
     * @param star
     *            The candidate star
     */
    void checkClosestStar(IStarFocus star);

    /**
     * Returns the current closest object
     */
    IFocus getClosest();

    /**
     * Sets the closest of all
     * @param focus The new closest object
     */
    void setClosest(IFocus focus);

    void updateFrustumPlanes();

    double getNear();
    double getFar();

}