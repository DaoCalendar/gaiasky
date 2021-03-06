/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.data.group;

import gaiasky.scenegraph.ParticleGroup.ParticleBean;

import java.io.InputStream;
import java.util.List;

/**
 * Data provider for a particle group.
 *
 * @author tsagrista
 */
public interface IParticleGroupDataProvider {
    /**
     * Loads the data as it is.
     *
     * @param file The file to load
     * @return Array of particle beans
     */
    List<ParticleBean> loadData(String file);

    /**
     * Loads the data applying a factor using a memory mapped file for improved speed.
     *
     * @param file   The file to load
     * @param factor Factor to apply to the positions
     * @return Array of particle beans
     */
    List<ParticleBean> loadDataMapped(String file, double factor);

    /**
     * Loads the data applying a factor.
     *
     * @param file   The file to load
     * @param factor Factor to apply to the positions
     * @return Array of particle beans
     */
    List<ParticleBean> loadData(String file, double factor);

    /**
     * Loads the data applying a factor.
     *
     * @param is     Input stream to load the data from
     * @param factor Factor to apply to the positions
     * @return Array of particle beans
     */
    List<ParticleBean> loadData(InputStream is, double factor);

    /**
     * Sets a cap on the number of files to load. Set to 0 or negative for
     * unlimited
     *
     * @param cap The cap number
     */
    void setFileNumberCap(int cap);
}
