/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.data.octreegen.generator;

import gaiasky.data.octreegen.StarBrightnessComparator;
import gaiasky.scenegraph.ParticleGroup.ParticleBean;
import gaiasky.scenegraph.StarGroup;
import gaiasky.scenegraph.StarGroup.StarBean;
import gaiasky.util.tree.OctreeNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BrightestStarsSimple implements IAggregationAlgorithm {
    /** Maximum number of objects in the densest node of a level **/
    private int MAX_PART;

    private Comparator<ParticleBean> comp;

    /**
     * Constructor using fields
     * 
     * @param maxPart
     *            Number of objects in the densest node of this level
     */
    public BrightestStarsSimple(int maxPart) {
        comp = new StarBrightnessComparator();
        this.MAX_PART = maxPart;
    }

    @Override
    public boolean sample(List<ParticleBean> inputStars, OctreeNode octant, float percentage) {
        StarGroup sg = new StarGroup();
        List<ParticleBean> data = new ArrayList<>();

        int nInput = inputStars.size();
        inputStars.sort(comp);

        int added = 0;
        while (added < MAX_PART && added < nInput) {
            StarBean sb = (StarBean) inputStars.get(added);
            if (sb.octant == null) {
                data.add(sb);
                sb.octant = octant;
                added++;
            }
        }
        if (added > 0) {
            sg.setData(data, false);
            octant.add(sg);
            sg.octant = octant;
            sg.octantId = octant.pageId;
        }
        return added == inputStars.size();

    }

    public int getMaxPart() {
        return MAX_PART;
    }

    public int getDiscarded() {
        return 0;
    }

    @Override
    public int getMaxDepth() {
        return 30;
    }

}
