/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.render;

import gaiasky.scenegraph.camera.ICamera;
import gaiasky.util.gdx.mesh.IntMesh;
import gaiasky.util.gdx.shader.ExtShaderProgram;

public interface IQuadRenderable extends IRenderable {

    /**
     * Renders the renderable as a quad using the star shader.
     *
     * @param shader
     * @param alpha
     * @param mesh
     * @param camera
     */
    void render(ExtShaderProgram shader, float alpha, IntMesh mesh, ICamera camera);
}
