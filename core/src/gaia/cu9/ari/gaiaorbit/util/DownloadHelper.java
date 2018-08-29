package gaia.cu9.ari.gaiaorbit.util;

import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.files.FileHandle;

import gaia.cu9.ari.gaiaorbit.util.Logger.Log;

/**
 * Contains utilities to download and uncompress files
 * @author tsagrista
 *
 */
public class DownloadHelper {
    private static final Log logger = Logger.getLogger(DownloadHelper.class);

    /*
     * Spawns a new thread which downloads the file from the given location, running the
     * progress {@link ProgressRunnable} while downloading, and running the finish {@link java.lang.Runnable}
     * when finished.
     */
    public static void downloadFile(String url, FileHandle to, ProgressRunnable progress, Runnable finish, Runnable fail, Runnable cancel) {

        // Make a GET request to get data descriptor
        HttpRequest request = new HttpRequest(HttpMethods.GET);
        request.setTimeOut(2500);
        request.setUrl(url);

        // Send the request, listen for the response
        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                // Determine how much we have to download
                long length = Long.parseLong(httpResponse.getHeader("Content-Length"));

                // We're going to download the file, create the streams
                InputStream is = httpResponse.getResultAsStream();
                OutputStream os = to.write(false);

                byte[] bytes = new byte[1024];
                int count = -1;
                long read = 0;
                try {
                    logger.info("Downloading: " + GlobalConf.program.DATA_DESCRIPTOR_URL);
                    // Keep reading bytes and storing them until there are no more.
                    while ((count = is.read(bytes, 0, bytes.length)) != -1) {
                        os.write(bytes, 0, count);
                        read += count;

                        // Compute progress value
                        final double progressValue = ((double) read / (double) length) * 100;

                        // Run progress runnable
                        if (progress != null)
                            progress.run(progressValue);
                    }
                    is.close();
                    os.close();
                    logger.info(txt("gui.download.finished", to.path()));

                    // Run finish runnable
                    if (finish != null)
                        finish.run();
                } catch (Exception e) {
                    logger.error(e);
                    if (fail != null)
                        fail.run();
                }
            }

            @Override
            public void failed(Throwable t) {
                logger.error(t, txt("gui.download.fail"));
                if (fail != null)
                    fail.run();
            }

            @Override
            public void cancelled() {
                logger.error("Download cancelled: " + url);
                if (cancel != null)
                    cancel.run();
            }
        });
    }

    protected static String txt(String key) {
        return I18n.bundle.get(key);
    }

    protected static String txt(String key, Object... args) {
        return I18n.bundle.format(key, args);
    }

}
