package cn.wacao.waterfall.framework.http.volley.toolbox;

import cn.wacao.waterfall.framework.http.volley.*;
import cn.wacao.waterfall.framework.log.WLog;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huluxia-wc on 14-9-29.
 * <p/>
 * Request which is used to download file from network.
 * <p/>
 * The response is the file path which is passed to construct
 * {@link cn.wacao.waterfall.framework.http.volley.toolbox.DownloadRequest}
 * <p/>
 * This request would post progress while downloading.
 */
public class DownloadRequest extends Request<String> {
    public Response.ProgressListener progressListener;
    public Response.CancelListener cancelListener;
    public Response.Listener<String> succListener;
    public String downloadPath;
    private long progress;
    public static final String HEADER_RANG = "Range";
    public static final String RESPONSE_HEADER_RANG = "Content-Range";
    private static final String HEADER_RANG_VALUE = "bytes=%d-";

    public DownloadRequest(String url, String downloadPath, Response.Listener<String> succListener, Response.ErrorListener listener, Response.ProgressListener progressListener) {
        this(url, downloadPath, succListener, listener, progressListener, null);
    }

    public DownloadRequest(String url, String downloadPath, Response.Listener<String> succListener, Response.ErrorListener listener, Response.ProgressListener progressListener, Response.CancelListener cancelListener) {
        super(Method.GET, url, listener);
        if (url == null || url.trim().length() <= 0)
            throw new IllegalArgumentException("download request url should not be NULL");
        setShouldCache(false);
        this.downloadPath = downloadPath;
        this.progressListener = progressListener;
        this.succListener = succListener;
        this.cancelListener = cancelListener;
        setTag(url);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        this.succListener.onResponse(response);
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public Response.ProgressListener getProgressListener() {
        return progressListener;
    }

    public void setProgress(long progress) {
        this.progress = progress;
        DownloadPreference.getInstance().putLong(this.downloadPath, progress);
    }

    public long getProgress() {
        return progress;
    }

    public Response.CancelListener getCancelListener() {
        return cancelListener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<String, String>();
        File file = new File(this.downloadPath);
        this.progress = DownloadPreference.getInstance().getLong(this.downloadPath, 0);
        WLog.info(this, "downloadPath = " + downloadPath + ", download start progress = " + progress);
        if (file.exists() && this.progress > 0) {
            WLog.info(this, "add reange header");
            headers.put(HEADER_RANG, String.format(HEADER_RANG_VALUE, progress));
        } else {
            WLog.info(this, "donwload file not exist or progress is INVALID, progress = " + progress);
        }
        return headers;
    }
}
