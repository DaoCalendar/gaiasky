/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.util.filter.attrib;

import gaiasky.scenegraph.ParticleGroup.ParticleBean;

public class AttributeGalLongitude extends AttributeAbstract implements IAttribute<ParticleBean> {
    @Override
    public double get(ParticleBean bean) {
        return bean.l();
    }
    public String getUnit(){
        return "deg";
    }
    public String toString(){
        return "Galactic longitude (l)";
    }
}
