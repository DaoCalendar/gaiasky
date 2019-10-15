/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import gaiasky.assets.OrbitDataLoader.OrbitDataLoaderParameter;
import gaiasky.data.orbit.OrbitSamplerDataProvider;
import gaiasky.data.util.PointCloudData;
import gaiasky.event.EventManager;
import gaiasky.event.Events;
import gaiasky.scenegraph.Orbit;
import gaiasky.util.Logger;
import gaiasky.util.Logger.Log;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class OrbitRefresher {
    private static final Log logger = Logger.getLogger(OrbitRefresher.class);

    /**
     * Maximum size of load queue
     */
    private static final int LOAD_QUEUE_MAX_SIZE = 15;
    /**
     * Maximum number of pages to send to load every batch
     **/
    protected static final int MAX_LOAD_CHUNK = 5;

    /**
     * The instance
     */
    private static OrbitRefresher instance;

    /**
     * The loading queue
     */
    private Queue<OrbitDataLoaderParameter> toLoadQueue;

    /**
     * The daemon
     */
    private DaemonRefresher daemon;

    /**
     * Loading is paused
     */
    private boolean loadingPaused = false;

    public OrbitRefresher() {
        super();
        toLoadQueue = new ArrayBlockingQueue<>(LOAD_QUEUE_MAX_SIZE);
        OrbitRefresher.instance = this;

        // Start daemon
        daemon = new DaemonRefresher();
        daemon.setDaemon(true);
        daemon.setName("daemon-orbit-refresher");
        daemon.setPriority(Thread.MIN_PRIORITY);
        daemon.start();
    }

    public void queue(OrbitDataLoaderParameter param) {
        if (!loadingPaused && toLoadQueue.size() < LOAD_QUEUE_MAX_SIZE - 1) {
            toLoadQueue.remove(param);
            toLoadQueue.add(param);
            param.orbit.refreshing = true;
            flushLoadQueue();
        }

    }

    /**
     * Tells the loader to start loading the octants in the queue.
     */
    public void flushLoadQueue() {
        if (!daemon.awake && !toLoadQueue.isEmpty() && !loadingPaused) {
            daemon.interrupt();
        }
    }

    /**
     * The daemon refresher thread.
     *
     * @author Toni Sagrista
     */
    protected static class DaemonRefresher extends Thread {
        private boolean awake;
        private boolean running;
        private AtomicBoolean abort;
        private OrbitSamplerDataProvider provider;

        private Array<OrbitDataLoaderParameter> toLoad;

        public DaemonRefresher() {
            this.awake = false;
            this.running = true;
            this.abort = new AtomicBoolean(false);
            this.toLoad = new Array<>();
            this.provider = new OrbitSamplerDataProvider();
        }

        /**
         * Stops the daemon iterations when
         */
        public void stopDaemon() {
            running = false;
        }

        /**
         * Aborts only the current iteration
         */
        public void abort() {
            abort.set(true);
        }

        @Override
        public void run() {
            while (running) {
                /** ----------- PROCESS REQUESTS ----------- **/
                while (!instance.toLoadQueue.isEmpty()) {
                    toLoad.clear();
                    int i = 0;
                    while (instance.toLoadQueue.peek() != null && i <= MAX_LOAD_CHUNK) {
                        OrbitDataLoaderParameter param = instance.toLoadQueue.poll();
                        toLoad.add(param);
                        i++;
                    }

                    // Generate orbits if any
                    if (toLoad.size > 0) {
                        try {
                            for (OrbitDataLoaderParameter param : toLoad) {
                                Orbit orbit = param.orbit;
                                if (orbit != null) {
                                    // Generate data
                                    provider.load(null, param);
                                    final PointCloudData pcd = provider.getData();
                                    // Post new data to object
                                    Gdx.app.postRunnable(() -> {
                                        // Update orbit object
                                        orbit.setPointCloudData(pcd);
                                        orbit.initOrbitMetadata();

                                        orbit.refreshing = false;
                                    });

                                } else {
                                    // Error, need orbit
                                }
                            }
                        } catch (Exception e) {
                            // This will happen when the queue has been cleared during processing
                            logger.debug("Refreshing orbits operation failed");
                        }
                    }
                }

                /** ----------- SLEEP UNTIL INTERRUPTED ----------- **/
                try {
                    awake = false;
                    abort.set(false);
                    Thread.sleep(Long.MAX_VALUE - 8);
                } catch (InterruptedException e) {
                    // New data!
                    awake = true;
                }
            }
        }
    }

}