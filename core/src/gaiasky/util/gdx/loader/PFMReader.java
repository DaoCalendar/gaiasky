/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.util.gdx.loader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.FloatTextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;
import gaiasky.util.Logger;
import gaiasky.util.math.MathUtilsd;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PFMReader {
    private static Logger.Log logger = Logger.getLogger(PFMReader.class);

    static public TextureData readPFMTextureData(FileHandle file, boolean invert) {

        try {
            PortableFloatMap pfm = new PortableFloatMap(file.file());
            float[] floatData = pfm.pixels;
            int width = pfm.width;
            int height = pfm.height;
            if (invert)
                floatData = invertWarp(floatData, width, height);

            FloatTextureData td = new FloatTextureData(width, height, GL30.GL_RGB16F, GL30.GL_RGB, GL30.GL_FLOAT, false);
            td.prepare();
            FloatBuffer buff = td.getBuffer();
            buff.put(floatData);

            return td;
        } catch (Exception e) {
            throw new GdxRuntimeException("Couldn't read PFM file '" + file + "'", e);
        } finally {
        }
    }

    static public Pixmap readPFMPixmap(FileHandle file, boolean invert) {
        try {
            PortableFloatMap pfm = new PortableFloatMap(file.file());
            float[] floatData = pfm.pixels;
            int width = pfm.width;
            int height = pfm.height;
            //int width = 100;
            //int height = 100;
            //float[] floatData = generateIdentity(width, height);
            if (invert)
                floatData = invertWarp(floatData, width, height);
            int totalSize = pfm.pixels.length;
            //int totalSize = width * height * 3;

            // Convert to Pixmap
            Format format = Format.RGB888;
            Pixmap pixmap = new Pixmap(width, height, format);

            ByteBuffer pixelBuf = pixmap.getPixels();
            pixelBuf.position(0);
            pixelBuf.limit(pixelBuf.capacity());

            for (int i = 0; i < totalSize; i++) {
                float f = floatData[i];
                byte b;
                if (Float.isNaN(f)) {
                    b = (byte) 0;
                } else {
                    f = (invert ? f : nor(f)) * 255f;
                    b = (byte) f;
                }

                pixelBuf.put(b);
            }

            pixelBuf.position(0);
            pixelBuf.limit(pixelBuf.capacity());

            return pixmap;
        } catch (Exception e) {
            throw new GdxRuntimeException("Couldn't read PFM file '" + file + "'", e);
        } finally {
        }
    }

    private static float nor(float value) {
        return MathUtilsd.clamp(value + 0.5f, 0f, 1f);
        //return value;
    }

    private static float[] generateIdentity(int w, int h) {
        float[] out = new float[w * h * 3];

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                float u = (float) i / (float) (w - 1);
                float v = (float) j / (float) (h - 1);

                // Store this pixel's position at end location
                out[(w * j + i) * 3 + 0] = u;
                out[(w * j + i) * 3 + 1] = v;
                out[(w * j + i) * 3 + 2] = Float.NaN;
            }
        }
        return out;
    }

    private static float[] generateSqrt(int w, int h) {
        float[] out = new float[w * h * 3];

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                float u = (float) i / (float) (w - 1);
                float v = (float) j / (float) (h - 1);

                // Store this pixel's position at end location
                out[(w * j + i) * 3 + 0] = (float) Math.sqrt(u);
                out[(w * j + i) * 3 + 1] = (float) Math.sqrt(v);
                out[(w * j + i) * 3 + 2] = Float.NaN;
            }
        }
        return out;
    }

    /**
     * Inverts the warp function. The mapping must be invertible, i.e. no folds must be
     * present.
     *
     * @param d The data
     * @param w The source width
     * @param h The source height
     * @return
     */
    private static float[] invertWarp(final float[] d, int w, int h) throws RuntimeException {
        // Create transformed mesh
        List<Quad> mesh = new ArrayList<>();
        float du = 1f / (w - 1f);
        float dv = 1f / (h - 1f);
        for (int j = 0; j < h - 1; j++) {
            for (int i = 0; i < w - 1; i++) {
                float u = i / (w - 1f);
                float v = j / (h - 1f);
                float[] origuv = new float[] { u, v };

                int bl = (w * j + i) * 3;
                int br = (w * j + i + 1) * 3;
                int tl = (w * (j + 1) + i) * 3;
                int tr = (w * (j + 1) + i + 1) * 3;
                float[] positions = new float[] { nor(d[bl]), nor(d[bl + 1]), nor(d[br]), nor(d[br + 1]), nor(d[tr]), nor(d[tr + 1]), nor(d[tl]), nor(d[tl + 1]) };

                Quad quad = new Quad(positions, origuv);
                mesh.add(quad);
            }
        }

        boolean warned = false;
        // Go over every pixel in final image and map it to original
        float[] out = new float[d.length];
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int p = (w * j + i) * 3;
                float u = i / (w - 1f);
                float v = j / (h - 1f);

                List<Quad> matches = mesh.stream().filter(quad -> quad.contains(u, v)).collect(Collectors.toList());
                if (matches.size() == 0) {
                    // Black
                    out[p + 0] = 0;
                    out[p + 1] = 0;
                    out[p + 2] = Float.NaN;
                } else {
                    if (matches.size() > 1 && !warned) {
                        // It has folds, warn
                        logger.warn("WARN: Warp function has folds!");
                        warned = true;
                    }
                    // Take first and do inverse bilinear interpolation
                    Quad quad = matches.get(0);
                    float[] uv = quad.invBilinear(u, v);

                    float finalU = quad.origUV[0] + du * uv[0];
                    float finalV = quad.origUV[1] + dv * uv[1];

                    out[p + 0] = finalU;
                    out[p + 1] = finalV;
                    out[p + 2] = Float.NaN;
                }
            }
        }

        return out;
    }

    /**
     * Quad with the mapped and original positions
     */
    private static class Quad {
        float[] positions, origUV;

        public Quad(float[] positions, float[] origUV) {
            this.positions = positions;
            this.origUV = origUV;
        }

        public boolean contains(float x, float y) {
            final int numFloats = positions.length;
            int intersects = 0;

            for (int i = 0; i < numFloats; i += 2) {
                float x1 = positions[i];
                float y1 = positions[i + 1];
                float x2 = positions[(i + 2) % numFloats];
                float y2 = positions[(i + 3) % numFloats];
                if (((y1 <= y && y < y2) || (y2 <= y && y < y1)) && x < ((x2 - x1) / (y2 - y1) * (y - y1) + x1))
                    intersects++;
            }
            return (intersects & 1) == 1;
        }

        float cross2d(float[] a, float[] b) {
            return a[0] * b[1] - a[1] * b[0];
        }

        // given a point p and a quad defined by four points {a,b,c,d}, return the bilinear
        // coordinates of p in the quad. Returns (-1,-1) if the point is outside of the quad.
        public float[] invBilinear(float x, float y) {
            float[] p = new float[] { x, y };
            float[] a = new float[] { positions[0], positions[1] };
            float[] b = new float[] { positions[2], positions[3] };
            float[] c = new float[] { positions[4], positions[5] };
            float[] d = new float[] { positions[6], positions[7] };

            float[] e = new float[] { b[0] - a[0], b[1] - a[1] };
            float[] f = new float[] { d[0] - a[0], d[1] - a[1] };
            float[] g = new float[] { a[0] - b[0] + c[0] - d[0], a[1] - b[1] + c[1] - d[1] };
            float[] h = new float[] { p[0] - a[0], p[1] - a[1] };

            float k2 = cross2d(g, f);
            float k1 = cross2d(e, f) + cross2d(h, g);
            float k0 = cross2d(h, e);

            float w = k1 * k1 - 4f * k0 * k2;
            if (w < 0f)
                return new float[] { -1f, -1f };
            w = (float) Math.sqrt(w);

            // will fail for k0=0, which is only on the ba edge
            float v = 2f * k0 / (-k1 - w);
            if (v < 0.0 || v > 1.0)
                v = 2f * k0 / (-k1 + w);

            float u = (h[0] - f[0] * v) / (e[0] + g[0] * v);
            if (u < 0.0 || u > 1.0 || v < 0.0 || v > 1.0)
                return new float[] { -1f, -1f };
            return new float[] { u, v };
        }

    }
}