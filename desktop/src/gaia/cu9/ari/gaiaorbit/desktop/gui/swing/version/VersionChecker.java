package gaia.cu9.ari.gaiaorbit.desktop.gui.swing.version;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.JsonReader;

import gaia.cu9.ari.gaiaorbit.desktop.gui.swing.callback.Runnable;

public class VersionChecker implements Runnable {
    private static final int VERSIONCHECK_TIMEOUT_MS = 5000;
    private String stringUrl;
    private Object result = null;
    private boolean error = false;

    public VersionChecker(String stringUrl) {
        this.stringUrl = stringUrl;
    }

    @Override
    public Object run() {

        HttpRequest request = new HttpRequest(HttpMethods.GET);
        request.setUrl(stringUrl);
        request.setTimeOut(VERSIONCHECK_TIMEOUT_MS);

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            public void handleHttpResponse(HttpResponse httpResponse) {
                JsonReader reader = new JsonReader();
                result = reader.parse(httpResponse.getResultAsStream());
            }

            public void failed(Throwable t) {
                error = true;
            }

            @Override
            public void cancelled() {
                error = true;

            }
        });

        return result;
    }

    public boolean isError() {
        return error;
    }

}
