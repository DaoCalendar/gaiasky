/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.render.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import gaiasky.render.ComponentTypes;
import gaiasky.render.ILineRenderable;
import gaiasky.render.IRenderable;
import gaiasky.render.SceneGraphRenderer;
import gaiasky.render.SceneGraphRenderer.RenderGroup;
import gaiasky.scenegraph.Particle;
import gaiasky.scenegraph.camera.ICamera;
import gaiasky.util.GlobalConf;
import gaiasky.util.gdx.mesh.IntMesh;
import gaiasky.util.gdx.shader.ExtShaderProgram;
import org.lwjgl.opengl.GL30;

import java.util.Comparator;
import java.util.List;

public class LineRenderSystem extends ImmediateRenderSystem {
    protected ICamera camera;
    protected Vector3 aux2;

    private ExtShaderProgram shaderProgram;

    public LineRenderSystem(RenderGroup rg, float[] alphas, ExtShaderProgram[] shaders) {
        super(rg, alphas, shaders, -1);
        aux2 = new Vector3();
    }

    @Override
    protected void initShaderProgram() {
        Gdx.gl.glEnable(GL30.GL_LINE_SMOOTH);
        Gdx.gl.glEnable(GL30.GL_LINE_WIDTH);
        Gdx.gl.glHint(GL30.GL_NICEST, GL30.GL_LINE_SMOOTH_HINT);
    }

    @Override
    protected void initVertices() {
        meshes = new Array<>();
        initVertices(meshIdx++);
    }

    private void initVertices(int index) {
        if (index >= meshes.size) {
            meshes.setSize(index + 1);
        }
        if (meshes.get(index) == null) {
            if (index > 0)
                logger.info("Capacity too small, creating new meshdata: " + curr.capacity);
            curr = new MeshData();
            meshes.set(index, curr);

            curr.capacity = 10000;

            VertexAttribute[] attribs = buildVertexAttributes();
            curr.mesh = new IntMesh(false, curr.capacity, 0, attribs);

            curr.vertexSize = curr.mesh.getVertexAttributes().vertexSize / 4;
            curr.vertices = new float[curr.capacity * curr.vertexSize];
            curr.colorOffset = curr.mesh.getVertexAttribute(Usage.ColorPacked) != null ? curr.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
        } else {
            curr = meshes.get(index);
        }
    }

    protected VertexAttribute[] buildVertexAttributes() {
        Array<VertexAttribute> attribs = new Array<>();
        attribs.add(new VertexAttribute(Usage.Position, 3, ExtShaderProgram.POSITION_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.ColorPacked, 4, ExtShaderProgram.COLOR_ATTRIBUTE));

        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera, double t) {

        shaderProgram = getShaderProgram();
        shaderProgram.begin();
        shaderProgram.setUniformMatrix("u_projView", camera.getCamera().combined);
        shaderProgram.setUniformf("u_alpha", 1f);

        // Rel, grav, z-buffer
        addEffectsUniforms(shaderProgram, camera);

        this.camera = camera;
        renderables.forEach(r ->{
            ILineRenderable renderable = (ILineRenderable) r;
            boolean rend = true;
            // TODO ugly hack
            if (renderable instanceof Particle && !SceneGraphRenderer.instance.isOn(ComponentTypes.ComponentType.VelocityVectors))
                rend = false;
            if (rend) {
                renderable.render(this, camera, getAlpha(renderable));
            }

            Gdx.gl.glLineWidth(renderable.getLineWidth() * 1.5f * GlobalConf.scene.LINE_WIDTH_FACTOR);

            for (int md = 0; md < meshIdx; md++) {
                MeshData meshd = meshes.get(md);
                meshd.mesh.setVertices(meshd.vertices, 0, meshd.vertexIdx);
                meshd.mesh.render(shaderProgram, renderable.getGlPrimitive());

                meshd.clear();
            }
        });
        shaderProgram.end();

        // Reset indices
        meshIdx = 1;
        curr = meshes.get(0);
    }

    /**
     * Breaks current line of points
     */
    public void breakLine() {

    }

    public void addPoint(ILineRenderable lr, double x, double y, double z, float r, float g, float b, float a) {
        // Check if 3 more indices fit
        if (curr.numVertices + 1 >= curr.capacity) {
            // Create new mesh data
            initVertices(meshIdx++);
        }

        color(r, g, b, a);
        vertex((float) x, (float) y, (float) z);
    }

    public void addLine(ILineRenderable lr, double x0, double y0, double z0, double x1, double y1, double z1, Color col) {
        addLinePostproc(x0, y0, z0, x1, y1, z1, col.r, col.g, col.b, col.a);
    }

    public void addLine(ILineRenderable lr, double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a) {
        addLinePostproc(x0, y0, z0, x1, y1, z1, r, g, b, a);
    }

    public void addLinePostproc(double x0, double y0, double z0, double x1, double y1, double z1, double r, double g, double b, double a) {
        // Check if 3 more indices fit
        if (curr.numVertices + 2 >= curr.capacity) {
            // Create new mesh data
            initVertices(meshIdx++);
        }

        color(r, g, b, a);
        vertex((float) x0, (float) y0, (float) z0);
        color(r, g, b, a);
        vertex((float) x1, (float) y1, (float) z1);
    }

    protected class LineArraySorter implements Comparator<double[]> {
        private int idx;

        public LineArraySorter(int idx) {
            this.idx = idx;
        }

        @Override
        public int compare(double[] o1, double[] o2) {
            double f = o1[idx] - o2[idx];
            if (f == 0)
                return 0;
            else if (f < 0)
                return 1;
            else
                return -1;
        }

    }

}
