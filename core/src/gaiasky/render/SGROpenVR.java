package gaiasky.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import gaiasky.GaiaSky;
import gaiasky.event.EventManager;
import gaiasky.event.Events;
import gaiasky.event.IObserver;
import gaiasky.interafce.*;
import gaiasky.render.IPostProcessor.PostProcessBean;
import gaiasky.scenegraph.StubModel;
import gaiasky.scenegraph.camera.ICamera;
import gaiasky.scenegraph.camera.NaturalCamera;
import gaiasky.util.Constants;
import gaiasky.util.GlobalConf;
import gaiasky.util.GlobalResources;
import gaiasky.util.Logger;
import gaiasky.util.Logger.Log;
import gaiasky.util.math.Vector3d;
import gaiasky.vr.openvr.VRContext;
import gaiasky.vr.openvr.VRContext.Space;
import gaiasky.vr.openvr.VRContext.VRDevice;
import gaiasky.vr.openvr.VRContext.VRDeviceType;
import org.lwjgl.openvr.*;

import java.nio.FloatBuffer;
import java.util.Map;

/**
 * Renders to OpenVR. Renders basically two scenes, one for each eye, using the
 * OpenVR context.
 *
 * @author tsagrista
 */
public class SGROpenVR extends SGRAbstract implements ISGR, IObserver {
    private static Log logger = Logger.getLogger(SGROpenVR.class);

    private VRContext vrContext;

    /**
     * Frame buffers for each eye
     **/
    FrameBuffer fbLeft, fbRight;
    /**
     * Textures
     **/
    Texture texLeft, texRight;

    HmdMatrix44 projectionMat = HmdMatrix44.create();
    HmdMatrix34 eyeMat = HmdMatrix34.create();

    public final Matrix4 eyeSpace = new Matrix4();
    public final Matrix4 invEyeSpace = new Matrix4();

    public Array<StubModel> controllerObjects;
    private Map<VRDevice, StubModel> vrDeviceToModel;
    private Environment controllersEnv;

    private SpriteBatch sb;

    // GUI
    private VRGui<VRInfoGui> infoGui;
    private VRGui<VRControllerInfoGui> controllerHintGui;
    private VRGui<VRSelectionGui> selectionGui;
    private Stage emptyStage;

    private Vector3 auxf1;
    private Vector3d auxd1;

    public SGROpenVR(VRContext vrContext) {
        super();
        // VR Context
        this.vrContext = vrContext;
        // Sprite batch for screen rendering
        this.sb = GlobalResources.spriteBatch;

        if (vrContext != null) {
            // Left eye, fb and texture
            fbLeft = new FrameBuffer(Format.RGBA8888, vrContext.getWidth(), vrContext.getHeight(), true);
            texLeft = org.lwjgl.openvr.Texture.create();
            texLeft.set(fbLeft.getColorBufferTexture().getTextureObjectHandle(), VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Gamma);

            // Right eye, fb and texture
            fbRight = new FrameBuffer(Format.RGBA8888, vrContext.getWidth(), vrContext.getHeight(), true);
            texRight = org.lwjgl.openvr.Texture.create();
            texRight.set(fbRight.getColorBufferTexture().getTextureObjectHandle(), VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Gamma);

            // Aux vectors
            auxf1 = new Vector3();
            auxd1 = new Vector3d();

            // Controllers
            Array<VRDevice> controllers = vrContext.getDevicesByType(VRDeviceType.Controller);

            // Env
            controllersEnv = new Environment();
            controllersEnv.set(new ColorAttribute(ColorAttribute.AmbientLight, .2f, .2f, .2f, 1f));
            DirectionalLight dlight = new DirectionalLight();
            dlight.color.set(1f, 1f, 1f, 1f);
            dlight.direction.set(0, -1, 0);
            controllersEnv.add(dlight);

            // Controller objects
            vrDeviceToModel = GaiaSky.instance.getVRDeviceToModel();
            controllerObjects = new Array<>(controllers.size);
            for (VRDevice controller : controllers) {
                if (!controller.isInitialized())
                    controller.initialize();
                addVRController(controller);
            }

            // GUI
            infoGui = new VRGui(VRInfoGui.class, (int) (GlobalConf.screen.BACKBUFFER_WIDTH / 10f));
            infoGui.initialize(null);

            controllerHintGui = new VRGui(VRControllerInfoGui.class, (int) (GlobalConf.screen.BACKBUFFER_WIDTH / 10f));
            controllerHintGui.initialize(null);

            selectionGui = new VRGui(VRSelectionGui.class, (int) (GlobalConf.screen.BACKBUFFER_WIDTH / 10f));
            selectionGui.initialize(null);

            Viewport vp = new ScreenViewport();
            emptyStage = new Stage(vp, sb);
            vp.update(GlobalConf.screen.BACKBUFFER_WIDTH, GlobalConf.screen.BACKBUFFER_HEIGHT, true);

            FloatBuffer fovt = BufferUtils.newFloatBuffer(1);
            FloatBuffer fovb = BufferUtils.newFloatBuffer(1);
            FloatBuffer fovr = BufferUtils.newFloatBuffer(1);
            FloatBuffer fovl = BufferUtils.newFloatBuffer(1);
            VRSystem.VRSystem_GetProjectionRaw(VR.EVREye_Eye_Left, fovl, fovr, fovt, fovb);

            try {
                double fovT = Math.toDegrees(Math.atan(fovt.get()));
                double fovB = Math.toDegrees(Math.atan(fovb.get()));
                double fovR = Math.toDegrees(Math.atan(fovr.get()));
                double fovL = Math.toDegrees(Math.atan(fovl.get()));
                float fov = (float) (fovB - fovT);
                if (fov > 50) {
                    logger.info("Setting fov to HMD value: " + fov);
                    EventManager.instance.post(Events.FOV_CHANGED_CMD, fov);
                } else {
                    // Default
                    logger.info("Setting fov to default value: " + 89f);
                    EventManager.instance.post(Events.FOV_CHANGED_CMD, 89f);
                }
            } catch (Exception e) {
                // Default
                EventManager.instance.post(Events.FOV_CHANGED_CMD, 89f);
            }
            EventManager.instance.subscribe(this, Events.FRAME_SIZE_UDPATE, Events.SCREENSHOT_SIZE_UDPATE, Events.VR_DEVICE_CONNECTED, Events.VR_DEVICE_DISCONNECTED);
        }
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, double t, int rw, int rh, int tw, int th, FrameBuffer fb, PostProcessBean ppb) {
        if (vrContext != null) {
            rc.ppb = null;
            try {
                vrContext.pollEvents();
            } catch (Exception e) {
                // Should never happen
            }

            // Add controllers
            for (StubModel controller : controllerObjects) {
                Vector3 devicepos = controller.getDevice().getPosition(Space.Tracker);
                // Length from headset to controller
                auxd1.set(devicepos).sub(vrContext.getDeviceByType(VRDeviceType.HeadMountedDisplay).getPosition(Space.Tracker));
                double controllerDist = auxd1.len();
                if (controller.instance != null) {
                    controller.addToRenderLists(SceneGraphRenderer.RenderGroup.MODEL_PIX);
                }
            }

            /** LEFT EYE **/

            // Camera to left
            updateCamera((NaturalCamera) camera.getCurrent(), camera.getCamera(), VR.EVREye_Eye_Left, false, rc);

            sgr.renderGlowPass(camera, sgr.getGlowFb(), VR.EVREye_Eye_Left);

            boolean postproc = postprocessCapture(ppb, fbLeft, tw, th);

            // Render scene
            sgr.renderScene(camera, t, rc);
            // Camera
            camera.render(GlobalConf.screen.SCREEN_WIDTH, GlobalConf.screen.SCREEN_HEIGHT);

            // GUI
            emptyStage.draw();
            if (controllerHintGui.mustDraw()) {
                renderGui(controllerHintGui.left());
            } else {
                if (infoGui.mustDraw())
                    renderGui(infoGui.left());

                if (selectionGui.mustDraw())
                    renderGui(selectionGui.left());
            }

            sendOrientationUpdate(camera.getCamera(), rw, rh);
            postprocessRender(ppb, fbLeft, postproc, camera, rw, rh);

            /** RIGHT EYE **/

            // Camera to right
            updateCamera((NaturalCamera) camera.getCurrent(), camera.getCamera(), VR.EVREye_Eye_Right, false, rc);

            sgr.renderGlowPass(camera, sgr.getGlowFb(), VR.EVREye_Eye_Right);

            postproc = postprocessCapture(ppb, fbRight, tw, th);

            // Render scene
            sgr.renderScene(camera, t, rc);
            // Camera
            camera.render(GlobalConf.screen.SCREEN_WIDTH, GlobalConf.screen.SCREEN_HEIGHT);

            // GUI
            emptyStage.draw();
            if (controllerHintGui.mustDraw()) {
                renderGui(controllerHintGui.right());
            } else {
                if (infoGui.mustDraw())
                    renderGui(infoGui.right());

                if (selectionGui.mustDraw())
                    renderGui(selectionGui.right());
            }

            sendOrientationUpdate(camera.getCamera(), rw, rh);
            postprocessRender(ppb, fbRight, postproc, camera, rw, rh);

            /** SUBMIT TO VR COMPOSITOR **/
            VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Left, texLeft, null, VR.EVRSubmitFlags_Submit_Default);
            VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Right, texRight, null, VR.EVRSubmitFlags_Submit_Default);

            /** Render to screen **/
            com.badlogic.gdx.graphics.Texture screenTex = fbRight.getColorBufferTexture();
            sb.begin();
            sb.draw(screenTex, 0, 0, GlobalConf.screen.BACKBUFFER_WIDTH, GlobalConf.screen.BACKBUFFER_HEIGHT, 0, 0, rw, rh, false, true);
            sb.end();
        }

    }

    private void updateCamera(NaturalCamera cam, PerspectiveCamera camera, int eye, boolean updateFrustum, RenderingContext rc) {
        // get the projection matrix from the HDM 
        VRSystem.VRSystem_GetProjectionMatrix(eye, camera.near, camera.far, projectionMat);
        VRContext.hmdMat4toMatrix4(projectionMat, camera.projection);

        // get the eye space matrix from the HDM
        VRSystem.VRSystem_GetEyeToHeadTransform(eye, eyeMat);
        VRContext.hmdMat34ToMatrix4(eyeMat, eyeSpace);
        invEyeSpace.set(eyeSpace).inv();

        // get the pose matrix from the HDM
        VRDevice hmd = vrContext.getDeviceByType(VRDeviceType.HeadMountedDisplay);
        Vector3 up = hmd.getUp(Space.Tracker);
        Vector3 dir = hmd.getDirection(Space.Tracker);
        Vector3 pos = hmd.getPosition(Space.Tracker);

        // Update main camera
        if (cam != null) {
            cam.vroffset.set(pos).scl(Constants.M_TO_U);
            cam.direction.set(dir);
            cam.up.set(up);
            rc.vroffset = cam.vroffset;
        }

        // Update Eye camera
        camera.view.idt();
        camera.view.setToLookAt(pos, auxf1.set(pos).add(dir), up);

        camera.combined.set(camera.projection);
        Matrix4.mul(camera.combined.val, invEyeSpace.val);
        Matrix4.mul(camera.combined.val, camera.view.val);

        if (updateFrustum) {
            camera.invProjectionView.set(camera.combined);
            Matrix4.inv(camera.invProjectionView.val);
            camera.frustum.update(camera.invProjectionView);
        }
    }

    private void renderGui(IGui gui) {
        gui.update(Gdx.graphics.getDeltaTime());
        gui.render(GlobalConf.screen.BACKBUFFER_WIDTH, GlobalConf.screen.BACKBUFFER_HEIGHT);
    }

    public void resize(final int w, final int h) {

    }

    public void dispose() {
        if (fbLeft != null)
            fbLeft.dispose();
        if (fbRight != null)
            fbRight.dispose();
        if (vrContext != null) {
            vrContext.dispose();
        }
    }

    private void addVRController(VRDevice device) {
        if (!vrDeviceToModel.containsKey(device)) {
            StubModel sm = new StubModel(device, controllersEnv);
            controllerObjects.add(sm);
            vrDeviceToModel.put(device, sm);
        }
    }

    private void removeVRController(VRDevice device) {
        if (vrDeviceToModel.containsKey(device)) {
            StubModel sm = vrDeviceToModel.get(device);
            controllerObjects.removeValue(sm, true);
            vrDeviceToModel.remove(device);
        }
    }

    @Override
    public void notify(final Events event, final Object... data) {
        switch (event) {
            case VR_DEVICE_CONNECTED:
                VRDevice device = (VRDevice) data[0];
                if (device.getType() == VRDeviceType.Controller) {
                    GaiaSky.postRunnable(() -> {
                        addVRController(device);
                    });
                }
                break;
            case VR_DEVICE_DISCONNECTED:
                device = (VRDevice) data[0];
                if (device.getType() == VRDeviceType.Controller) {
                    GaiaSky.postRunnable(() -> {
                        removeVRController(device);
                    });
                }
                break;
            default:
                break;
        }

    }

}
