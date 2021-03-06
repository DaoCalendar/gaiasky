/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.interafce;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntSet;
import gaiasky.desktop.util.SysUtils;
import gaiasky.event.EventManager;
import gaiasky.event.Events;
import gaiasky.event.IObserver;
import gaiasky.scenegraph.camera.CameraManager;
import gaiasky.scenegraph.camera.NaturalCamera;
import gaiasky.util.GlobalConf;
import gaiasky.util.Logger;
import gaiasky.util.Logger.Log;

import java.nio.file.Files;
import java.nio.file.Path;

public class NaturalControllerListener implements ControllerListener, IObserver, IInputListener {
    private static final Log logger = Logger.getLogger(NaturalControllerListener.class);

    private NaturalCamera cam;
    private IControllerMappings mappings;
    private EventManager em;

    private IntSet pressedKeys;

    public NaturalControllerListener(NaturalCamera cam, String mappingsFile) {
        this.cam = cam;
        this.em = EventManager.instance;
        this.pressedKeys = new IntSet();
        updateControllerMappings(mappingsFile);

        em.subscribe(this, Events.RELOAD_CONTROLLER_MAPPINGS);
    }

    public void addPressedKey(int keycode) {
        pressedKeys.add(keycode);
    }

    public void removePressedKey(int keycode) {
        pressedKeys.remove(keycode);
    }

    public boolean isKeyPressed(int keycode) {
        return pressedKeys.contains(keycode);
    }

    /**
     * Returns true if all keys are pressed
     *
     * @param keys The keys to test
     * @return True if all are pressed
     */
    public boolean allPressed(int... keys) {
        for (int k : keys) {
            if (!pressedKeys.contains(k))
                return false;
        }
        return true;
    }

    /**
     * Returns true if any of the keys are pressed
     *
     * @param keys The keys to test
     * @return True if any is pressed
     */
    public boolean anyPressed(int... keys) {
        for (int k : keys) {
            if (pressedKeys.contains(k))
                return true;
        }
        return false;
    }

    public IControllerMappings getMappings() {
        return mappings;
    }

    public boolean updateControllerMappings(String mappingsFile) {
        // We look for OS-specific mappings for the given inputListener. If not found, it defaults to the base
        String os = SysUtils.getOSFamily();
        int extensionStart = mappingsFile.lastIndexOf('.');
        String pre = mappingsFile.substring(0, extensionStart); //-V6009
        String post = mappingsFile.substring(extensionStart + 1);

        String osMappingsFile = pre + "." + os + "." + post;
        if (Files.exists(Path.of(osMappingsFile))) {
            mappingsFile = osMappingsFile;
            logger.info("Controller mappings file set to " + mappingsFile);
        }

        if (Files.exists(Path.of(mappingsFile)))
            mappings = new ControllerMappings(null, Path.of(mappingsFile));
        return false;
    }

    @Override
    public void connected(Controller controller) {
        logger.info("Controller connected: " + controller.getName());
        em.post(Events.CONTROLLER_CONNECTED_INFO, controller.getName());
    }

    @Override
    public void disconnected(Controller controller) {
        logger.info("Controller disconnected: " + controller.getName());
        em.post(Events.CONTROLLER_DISCONNECTED_INFO, controller.getName());
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        logger.debug("button down [inputListener/code]: " + controller.getName() + " / " + buttonCode);

        cam.setInputByController(true);

        addPressedKey(buttonCode);

        return true;
    }

    @Override
    public boolean buttonUp(Controller controller, final int buttonCode) {
        logger.debug("button up [inputListener/code]: " + controller.getName() + " / " + buttonCode);

        if (buttonCode == mappings.getButtonX()) {
            em.post(Events.TOGGLE_MINIMAP);
        } else if (buttonCode == mappings.getButtonY()) {
            em.post(Events.TOGGLE_VISIBILITY_CMD, "element.orbits", false);
        } else if (buttonCode == mappings.getButtonA()) {
            em.post(Events.TOGGLE_VISIBILITY_CMD, "element.labels", false);
        } else if (buttonCode == mappings.getButtonB()) {
            em.post(Events.TOGGLE_VISIBILITY_CMD, "element.asteroids", false);
        } else if (buttonCode == mappings.getButtonDpadUp()) {
            em.post(Events.STAR_POINT_SIZE_INCREASE_CMD);
        } else if (buttonCode == mappings.getButtonDpadDown()) {
            em.post(Events.STAR_POINT_SIZE_DECREASE_CMD);
        } else if (buttonCode == mappings.getButtonDpadLeft()) {
            em.post(Events.TIME_STATE_CMD, false, false);
        } else if (buttonCode == mappings.getButtonDpadRight()) {
            em.post(Events.TIME_STATE_CMD, true, false);
        } else if (buttonCode == mappings.getButtonStart()) {
            em.post(Events.SHOW_CONTROLLER_GUI_ACTION, cam);
        } else if (buttonCode == mappings.getButtonRstick()) {
            if (cam.getMode().isFocus()) {
                // Set free
                em.post(Events.CAMERA_MODE_CMD, CameraManager.CameraMode.FREE_MODE);
            } else {
                // Set focus
                em.post(Events.CAMERA_MODE_CMD, CameraManager.CameraMode.FOCUS_MODE);
            }
        }
        cam.setInputByController(true);

        removePressedKey(buttonCode);

        return true;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        if (Math.abs(value) > 0.1)
            logger.debug("axis moved [inputListener/code/value]: " + controller.getName() + " / " + axisCode + " / " + value);

        boolean treated = false;

        // Apply power function to axis reading
        double val = Math.signum(value) * Math.pow(Math.abs(value), mappings.getAxisValuePower());

        if (axisCode == mappings.getAxisLstickH()) {
            if (cam.getMode().isFocus()) {
                cam.setRoll(val * 1e-2 * mappings.getAxisLstickHSensitivity());
            } else {
                // Use this for lateral movement
                cam.setHorizontal(val * mappings.getAxisLstickHSensitivity());
            }
            treated = true;
        } else if (axisCode == mappings.getAxisLstickV()) {
            if (Math.abs(val) < 0.005)
                val = 0;
            cam.setVelocity(-val * mappings.getAxisLstickVSensitivity());
            treated = true;
        } else if (axisCode == mappings.getAxisRstickH()) {
            double valr = (GlobalConf.controls.INVERT_LOOK_X_AXIS ? -1.0 : 1.0) * val * mappings.getAxisRstickVSensitivity();
            if (cam.getMode().isFocus()) {
                cam.setHorizontal(valr * 0.1);
            } else {
                cam.setYaw(valr * 3e-2);
            }
            treated = true;
        } else if (axisCode == mappings.getAxisRstickV()) {
            double valr = (GlobalConf.controls.INVERT_LOOK_Y_AXIS ? 1.0 : -1.0) * val * mappings.getAxisRstickHSensitivity();
            if (cam.getMode().isFocus()) {
                cam.setVertical(valr * 0.1);
            } else {
                cam.setPitch(valr * 3e-2);
            }
            treated = true;
        } else if (axisCode == mappings.getAxisRT()) {
            double valr = val * 1e-1 * mappings.getAxisRTSensitivity();
            cam.setRoll(valr);
            treated = true;
        } else if (axisCode == mappings.getAxisLT()) {
            double valr = val * 1e-1 * mappings.getAxisLTSensitivity();
            cam.setRoll(-valr);
            treated = true;
        }

        if (treated)
            cam.setInputByController(true);

        return treated;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return false;
    }

    @Override
    public void notify(final Events event, final Object... data) {
        switch (event) {
        case RELOAD_CONTROLLER_MAPPINGS:
            updateControllerMappings((String) data[0]);
            break;
        default:
            break;
        }

    }

    @Override
    public void update() {
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }
}
