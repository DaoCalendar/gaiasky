/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

/**
 * Fisheye distortion filter
 *
 * @author tsagrista
 */

package gaiasky.util.gdx.contrib.postprocess.filters;

import com.badlogic.gdx.math.Vector2;
import gaiasky.util.gdx.contrib.utils.ShaderLoader;

public final class FisheyeDistortion extends Filter<FisheyeDistortion> {
    private Vector2 viewport;
    private float fov;
    private int mode = 0;

    public enum Param implements Parameter {
        // @formatter:off
        Texture0("u_texture0", 0),
        Fov("u_fov", 0),
        Mode("u_mode", 0),
        Viewport("u_viewport", 2);
        // @formatter:on

        private final String mnemonic;
        private int elementSize;

        Param(String m, int elementSize) {
            this.mnemonic = m;
            this.elementSize = elementSize;
        }

        @Override
        public String mnemonic() {
            return this.mnemonic;
        }

        @Override
        public int arrayElementSize() {
            return this.elementSize;
        }
    }


    public FisheyeDistortion(float width, float height) {
        super(ShaderLoader.fromFile("screenspace", "fisheye"));
        viewport = new Vector2(width, height);
        rebind();
    }

    public FisheyeDistortion(int width, int height) {
        this((float) width, (float) height);
    }

    public void setViewportSize(float width, float height) {
        this.viewport.set(width, height);
        setParam(Param.Viewport, this.viewport);
    }

    /**
     * Sets the field of view, in degrees. Only needed when mode=1 (accurate)
     * @param fovDegrees
     */
    public void setFov(float fovDegrees){
        this.fov = (float) Math.toRadians(fovDegrees);
        setParam(Param.Fov, this.fov);
    }

    public void setMode(int mode){
        this.mode = mode;
        setParam(Param.Mode, this.mode);
    }

    @Override
    protected void onBeforeRender() {
        inputTexture.bind(u_texture0);
    }

    @Override
    public void rebind() {
        setParams(Param.Texture0, u_texture0);
        setParams(Param.Viewport, viewport);
        setParams(Param.Fov, fov);

        endParams();
    }
}
