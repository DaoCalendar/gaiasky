package gaia.cu9.ari.gaiaorbit.interfce;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.IFocus;
import gaia.cu9.ari.gaiaorbit.scenegraph.StubModel;
import gaia.cu9.ari.gaiaorbit.scenegraph.camera.NaturalCamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.camera.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.comp.ViewAngleComparator;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.vr.VRContext.VRControllerButtons;
import gaia.cu9.ari.gaiaorbit.vr.VRContext.VRDevice;
import gaia.cu9.ari.gaiaorbit.vr.VRDeviceListener;

public class OpenVRListener implements VRDeviceListener {
    /** The natural camera **/
    private NaturalCamera cam;
    /** Focus comparator **/
    private Comparator<IFocus> comp;
    /** Map from VR device to model object **/
    private HashMap<VRDevice, StubModel> vrDeviceToModel;
    /** Aux vectors **/
    private Vector3d p0, p1;

    public OpenVRListener(NaturalCamera cam) {
        this.cam = cam;
        this.comp = new ViewAngleComparator<IFocus>();
        this.p0 = new Vector3d();
        this.p1 = new Vector3d();
    }

    private void lazyInit() {
        if (vrDeviceToModel == null)
            vrDeviceToModel = GaiaSky.instance.getVRDeviceToModel();
    }

    public void connected(VRDevice device) {
        Logger.info(device + " connected");
        EventManager.instance.post(Events.VR_DEVICE_CONNECTED, device);
    }

    public void disconnected(VRDevice device) {
        Logger.info(device + " disconnected");
        EventManager.instance.post(Events.VR_DEVICE_DISCONNECTED, device);
    }

    public void buttonPressed(VRDevice device, int button) {
        if (GlobalConf.controls.DEBUG_MODE) {
            Logger.info("vr button down [device/code]: " + device.toString() + " / " + button);
        }

        if (button == VRControllerButtons.Grip) {
            // Forward
            lazyInit();
            StubModel sm = vrDeviceToModel.get(device);
            if (sm != null) {
                // Direct direction
                cam.setVelocityVR(sm.getBeamP0(), sm.getBeamP1(), 1);
            }
        } else if (button == VRControllerButtons.SteamVR_Touchpad) {
            // Backward
            lazyInit();
            StubModel sm = vrDeviceToModel.get(device);
            if (sm != null) {
                // Invert direction
                cam.setVelocityVR(sm.getBeamP0(), sm.getBeamP1(), -1);
            }
        }
    }

    public void buttonReleased(VRDevice device, int button) {
        if (GlobalConf.controls.DEBUG_MODE) {
            Logger.info("vr button released [device/code]: " + device.toString() + " / " + button);
        }

        if (button == VRControllerButtons.SteamVR_Trigger) {
            lazyInit();
            // Selection
            StubModel sm = vrDeviceToModel.get(device);
            if (sm != null) {
                p0.set(sm.getBeamP0());
                p1.set(sm.getBeamP1());
                IFocus hit = getBestHit(p0, p1);
                if (hit != null) {
                    EventManager.instance.post(Events.FOCUS_CHANGE_CMD, hit);
                    EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
                }
            } else {
                Logger.info("Model corresponding to device not found");
            }
        } else if (button == VRControllerButtons.Grip || button == VRControllerButtons.SteamVR_Touchpad) {
            // Stop
            cam.clearVelocityVR();
        } else if (button == VRControllerButtons.A) {
            // Change mode from free to focus and viceversa
            CameraMode cm = cam.getMode().equals(CameraMode.Focus) ? CameraMode.Free_Camera : CameraMode.Focus;
            EventManager.instance.post(Events.CAMERA_MODE_CMD, cm);
        } else if (button == VRControllerButtons.B) {
            // Toggle VR GUI
            EventManager.instance.post(Events.DISPLAY_VR_GUI_CMD, "VR GUI");
        }
    }

    private Array<IFocus> getHits(Vector3d p0, Vector3d p1) {
        Array<IFocus> l = GaiaSky.instance.getFocusableEntities();

        Array<IFocus> hits = new Array<IFocus>();

        Iterator<IFocus> it = l.iterator();
        // Add all hits
        while (it.hasNext()) {
            IFocus s = it.next();
            s.addHit(p0, p1, cam, hits);
        }

        return hits;
    }

    private IFocus getBestHit(Vector3d p0, Vector3d p1) {
        Array<IFocus> hits = getHits(p0, p1);
        if (hits.size != 0) {
            // Sort using distance
            hits.sort(comp);
            // Get closest
            return hits.get(hits.size - 1);
        }
        return null;
    }

    @Override
    public void event(int code) {
        if (GlobalConf.controls.DEBUG_MODE) {
            Logger.info("Unhandled event: " + code);
        }
    }

    @Override
    public void buttonTouched(VRDevice device, int button) {
        if (GlobalConf.controls.DEBUG_MODE) {
            Logger.info("vr button touched [device/code]: " + device.toString() + " / " + button);
        }
    }

    @Override
    public void buttonUntouched(VRDevice device, int button) {
        if (GlobalConf.controls.DEBUG_MODE) {
            Logger.info("vr button untouched [device/code]: " + device.toString() + " / " + button);
        }
    }
}
