package gaiasky.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import gaiasky.data.ISceneGraphLoader;
import gaiasky.event.EventManager;
import gaiasky.event.Events;
import gaiasky.util.CatalogInfo;
import gaiasky.util.Logger;
import uk.ac.starlink.util.DataSource;

/**
 * Represents a generic catalog of entities.
 * This entity loads catalog data given a provider and a file, initializes them
 * and adds them as children.
 */
public class GenericCatalog extends FadeNode {

    protected String description;

    /**
     * Fully qualified name of data provider class
     */
    protected String provider;

    /**
     * Path of data file
     */
    protected String datafile;

    /**
     * STIL data source, if no data file exists
     */
    protected DataSource ds;

    protected Array<? extends SceneGraphNode> clusters;

    public GenericCatalog() {
        super();
        this.description = "-";
    }

    @Override
    public void initialize() {
        initialize(true, true);
    }

    public void initialize(boolean dataLoad, boolean createCatalogInfo) {
        super.initialize();
        /** Load data **/
        try {
            String dsName = this.getName();

            // Load data and add
            if (dataLoad) {
                Class<?> clazz = Class.forName(provider);
                ISceneGraphLoader provider = (ISceneGraphLoader) clazz.getConstructor().newInstance();
                if(datafile != null)
                    provider.initialize(new String[]{datafile});
                else if(ds!= null)
                    provider.initialize(ds);

                provider.setName(dsName);
                clusters = provider.loadData();
                clusters.forEach(object -> {
                    SceneGraphNode ape = object;
                    ape.setParent(dsName);
                    ape.setColor(this.cc);
                    ape.setLabelcolor(this.labelcolor != null ? this.labelcolor.clone() : this.cc.clone());
                    ape.initialize();
                });
            }

            // Create catalog info
            if (createCatalogInfo) {
                CatalogInfo ci = new CatalogInfo(dsName, description, null, CatalogInfo.CatalogInfoType.INTERNAL, 1f, this);
                EventManager.instance.post(Events.CATALOG_ADD, ci, false);
            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass()).error(e);
        }
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);
        if (children != null)
            children.forEach(child -> child.doneLoading(manager));
    }

    @Override
    public void setUp() {
        super.setUp();
        if (clusters != null) {
            clusters.forEach(object -> {
                sg.insert(object, true);
            });
            clusters.clear();
            clusters = null;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDatafile() {
        return datafile;
    }

    public void setDatafile(String datafile) {
        this.datafile = datafile;
    }
    public void setDataSource(DataSource ds) {
        this.ds = ds;
    }
}
