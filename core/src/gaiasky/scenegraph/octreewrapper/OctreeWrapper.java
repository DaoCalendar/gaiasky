/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.scenegraph.octreewrapper;

import gaiasky.scenegraph.SceneGraphNode;
import gaiasky.scenegraph.camera.ICamera;
import gaiasky.util.math.Vector3d;
import gaiasky.util.time.ITimeFrameProvider;
import gaiasky.util.tree.OctreeNode;

import java.util.ArrayList;

/**
 * Static Octree wrapper that can be inserted into the scene graph. This
 * implementation is single-threaded.
 * 
 * @author Toni Sagrista
 *
 */
public class OctreeWrapper extends AbstractOctreeWrapper {

    public OctreeWrapper() {
        super();
    }

    public OctreeWrapper(String parentName, OctreeNode root) {
        super(parentName, root);
        roulette = new ArrayList<SceneGraphNode>(root.nObjects);
    }

    @Override
    protected void updateOctreeObjects(ITimeFrameProvider time, Vector3d parentTransform, ICamera camera) {
        int size = roulette.size();
        for (int i = 0; i < size; i++) {
            SceneGraphNode sgn = roulette.get(i);
            sgn.update(time, parentTransform, camera, this.opacity * sgn.octant.opacity);
        }
    }

}
