/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.data;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import gaiasky.scenegraph.ISceneGraph;
import gaiasky.scenegraph.SceneGraphNode;
import gaiasky.scenegraph.StarGroup;
import gaiasky.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaiasky.util.I18n;
import gaiasky.util.Logger;
import gaiasky.util.Logger.Log;
import gaiasky.util.TextUtils;
import gaiasky.util.coord.IBodyCoordinates;
import gaiasky.util.time.ITimeFrameProvider;

import java.io.FileNotFoundException;

public class SceneGraphJsonLoader {
    private static Log logger = Logger.getLogger(SceneGraphJsonLoader.class);

    public static ISceneGraph loadSceneGraph(FileHandle[] jsonFiles, ITimeFrameProvider time, boolean multithreading, int maxThreads) throws FileNotFoundException, ReflectionException, NoSuchMethodException {
        ISceneGraph sg = null;
        try {
            logger.info(I18n.txt("notif.loading", "JSON data descriptor files:"));
            for (FileHandle fh : jsonFiles) {
                logger.info("\t" + fh.path() + " - exists: " + fh.exists());
                if (!fh.exists()) {
                    logger.error(I18n.txt("error.loading.notexistent", fh.path()));
                }
            }

            Array<SceneGraphNode> nodes = new Array<>(false, 20600);

            for (FileHandle jsonFile : jsonFiles) {
                JsonReader jsonReader = new JsonReader();
                JsonValue model = jsonReader.parse(jsonFile.read());

                // Must have a 'data' element
                if (model.has("data")) {
                    String name = model.get("name") != null ? model.get("name").asString() : null;
                    String desc = model.get("description") != null ? model.get("description").asString() : null;

                    JsonValue child = model.get("data").child;
                    while (child != null) {
                        String clazzName = child.getString("loader").replace("gaia.cu9.ari.gaiaorbit", "gaiasky");
                        @SuppressWarnings("unchecked") Class<Object> clazz = (Class<Object>) ClassReflection.forName(clazzName);

                        JsonValue filesJson = child.get("files");
                        if (filesJson != null) {
                            String[] files = filesJson.asStringArray();

                            Constructor c = ClassReflection.getConstructor(clazz);
                            ISceneGraphLoader loader = (ISceneGraphLoader) c.newInstance();

                            if (name != null)
                                loader.setName(name);
                            if (desc != null)
                                loader.setDescription(desc);

                            // Init loader
                            loader.initialize(files);

                            JsonValue curr = filesJson;
                            while (curr.next != null) {
                                curr = curr.next;
                                String nameAttr = curr.name;
                                Object val = null;
                                Class valueClass = null;
                                if (curr.isDouble()) {
                                    val = curr.asDouble();
                                    valueClass = Double.class;
                                } else if (curr.isString()) {
                                    val = curr.asString();
                                    valueClass = String.class;
                                } else if (curr.isNumber()) {
                                    val = curr.asLong();
                                    valueClass = Long.class;
                                }
                                if (val != null) {
                                    String methodName = "set" + TextUtils.propertyToMethodName(nameAttr);
                                    Method m = searchMethod(methodName, valueClass, clazz);
                                    if (m != null)
                                        m.invoke(loader, val);
                                    else
                                        logger.error("ERROR: No method " + methodName + "(" + valueClass.getName() + ") in class " + clazz + " or its superclass/interfaces.");
                                }
                            }

                            // Load data
                            Array<? extends SceneGraphNode> data = loader.loadData();
                            for (SceneGraphNode elem : data) {
                                nodes.add(elem);
                            }
                        }

                        child = child.next;
                    }
                } else {
                    // Use regular JsonLoader
                    JsonLoader loader = new JsonLoader();
                    loader.initialize(new String[]{jsonFile.file().getAbsolutePath()});
                    // Load data
                    Array<? extends SceneGraphNode> data = loader.loadData();
                    for (SceneGraphNode elem : data) {
                        nodes.add(elem);
                    }
                }
            }

            // Initialize nodes and look for octrees
            boolean hasOctree = false;
            boolean hasStarGroup = false;
            for (SceneGraphNode node : nodes) {
                node.initialize();
                if (node instanceof AbstractOctreeWrapper) {
                    hasOctree = true;
                    AbstractOctreeWrapper aow = (AbstractOctreeWrapper) node;
                    if (aow.children != null)
                        for (SceneGraphNode n : aow.children) {
                            if (n instanceof StarGroup) {
                                hasStarGroup = true;
                                break;
                            }
                        }
                }

                if (node instanceof StarGroup)
                    hasStarGroup = true;
            }

            sg = SceneGraphImplementationProvider.provider.getImplementation(multithreading, hasOctree, hasStarGroup, maxThreads);

            sg.initialize(nodes, time, hasOctree, hasStarGroup);

        } catch (Exception e) {
            throw e;
        }
        return sg;
    }

    /**
     * Searches for the given method with the given class. If none is found, it looks for fitting methods
     * with the classe's interfaces and superclasses recursively.
     *
     * @param methodName
     * @param clazz
     * @return
     */
    private static Method searchMethod(String methodName, Class<?> clazz, Class<?> source) {
        Method m = null;
        try {
            m = ClassReflection.getMethod(source, methodName, clazz);
        } catch (ReflectionException e) {
            try {
                if (methodName.contains("setCoordinates")) {
                    // Special case
                    m = ClassReflection.getMethod(source, methodName, IBodyCoordinates.class);
                }
            } catch (ReflectionException e1) {
                logger.error(e1);
            }
        }
        return m;
    }

}
