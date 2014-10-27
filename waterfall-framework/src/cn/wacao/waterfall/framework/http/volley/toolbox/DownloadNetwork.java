package cn.wacao.waterfall.framework.http.volley.toolbox;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import cn.wacao.waterfall.framework.http.volley.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huluxia-wc on 14-9-29.
 */
public class DownloadNetwork extends BasicNetwork {
    private static final int DEFALUT_BUFFER_SIZE = 1024;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    public DownloadNetwork(HttpStack httpStack) {
        super(httpStack);
    }

    public DownloadNetwork(HttpStack httpStack, ByteArrayPool pool) {
        super(httpStack, pool);
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {
        long requestStart = SystemClock.elapsedRealtime();
        while (true) {
            HttpResponse httpResponse = null;
            byte[] responseContents = null;
            Map<String, String> responseHeaders = new HashMap<String, String>();
            try {
                // Gather headers.
                Map<String, String> headers = new HashMap<String, String>();
                addCacheHeaders(headers, request.getCacheEntry());
                httpResponse = mHttpStack.performRequest(request, headers);
                StatusLine statusLine = httpResponse.getStatusLine();
                int statusCode = statusLine.getStatusCode();

                responseHeaders = convertHeaders(httpResponse.getAllHeaders());
                // Handle cache validation.
                if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
                    return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED,
                            request.getCacheEntry() == null ? null : request.getCacheEntry().data,
                            responseHeaders, true);
                }

                // Some responses such as 204s do not have content.  We must check.
                if (httpResponse.getEntity() != null) {
                    responseContents = entityToBytes(httpResponse.getEntity(), request, responseHeaders);
                } else {
                    // Add 0 byte response as a way of honestly representing a
                    // no-content request.
                    responseContents = new byte[0];
                }

                // if the request is slow, log it.
                long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
                logSlowRequests(requestLifetime, request, responseContents, statusLine);

                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }
                return new NetworkResponse(statusCode, responseContents, responseHeaders, false);
            } catch (SocketTimeoutException e) {
                attemptRetryOnException("socket", request, new TimeoutError());
            } catch (ConnectTimeoutException e) {
                attemptRetryOnException("connection", request, new TimeoutError());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            } catch (IOException e) {
                int statusCode = 0;
                NetworkResponse networkResponse = null;
                if (httpResponse != null) {
                    statusCode = httpResponse.getStatusLine().getStatusCode();
                } else {
                    throw new NoConnectionError(e);
                }
                VolleyLog.e("Unexpected response code %d for %s", statusCode, request.getUrl());
                if (responseContents != null) {
                    networkResponse = new NetworkResponse(statusCode, responseContents,
                            responseHeaders, false);
                    if (statusCode == HttpStatus.SC_UNAUTHORIZED ||
                            statusCode == HttpStatus.SC_FORBIDDEN) {
                        attemptRetryOnException("auth",
                                request, new AuthFailureError(networkResponse));
                    } else {
                        // TODO: Only throw ServerError for 5xx status codes.
                        throw new ServerError(networkResponse);
                    }
                } else {
                    throw new NetworkError(networkResponse);
                }
            }
        }
    }

    protected byte[] entityToBytes(HttpEntity entity, Request<?> request, Map<String, String> responseHeaders) throws IOException, ServerError {
        if (request instanceof DownloadRequest) {
            String filePath = getFilePath(request);
            File file = new File(filePath);
            boolean fileExist = file.exists();
            VolleyLog.d("download with url=" + request.getUrl() + ", to path = " + filePath + ", file exist = " + fileExist);

            if (!fileExist) {
                file.createNewFile();
                return downloadNewFile(entity, request, file);
            } else {
                return downloadResume(entity, request, file, responseHeaders);
            }
        }
        return super.entityToBytes(entity, request);
    }

    /**
     * 断点续传
     * <p/>
     * example ：Content-Range=bytes 2198528-5220681/5220682
     *
     * @param entity
     * @param request
     * @param file
     * @param responseHeaders
     * @return
     * @throws java.io.IOException
     * @throws cn.wacao.waterfall.framework.http.volley.ServerError
     */
    private byte[] downloadResume(HttpEntity entity, final Request<?> request, File file, Map<String, String> responseHeaders) throws IOException, ServerError {
        if (!responseHeaders.containsKey(DownloadRequest.RESPONSE_HEADER_RANG)) {
            return downloadNewFile(entity, request, file);
        }

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
        byte[] buffer = null;
        try {
            InputStream in = entity.getContent();
            if (in == null) {
                throw new ServerError();
            }

            buffer = mPool.getBuf(DEFALUT_BUFFER_SIZE);

            long seekLocation = 0;
            String rangeValue = responseHeaders.get(DownloadRequest.RESPONSE_HEADER_RANG);
            int bytePrefix = "bytes ".length();
            if (bytePrefix > rangeValue.length()) {
                VolleyLog.e("downloadResume rangeValue INVALID");
                throw new ServerError();
            }
            rangeValue = rangeValue.substring(bytePrefix, rangeValue.length());
            VolleyLog.d("downloadResume rangeValue = " + rangeValue);

            if (rangeValue.contains("-")) {
                String bytesString = rangeValue.split("-")[0];

                try {
                    seekLocation = Long.parseLong(bytesString);
                } catch (NumberFormatException e) {
                    VolleyLog.e("downloadResume exception = " + e);
                }
                VolleyLog.v("downloadResume seekLocation = " + seekLocation);
                randomAccessFile.seek(seekLocation);
            }


            int count;
            long progress = seekLocation;
            randomAccessFile.seek(progress);

            final long total = entity.getContentLength() + progress;
            VolleyLog.d("downloadResume file = " + file.getAbsolutePath() + " content length = " + total);

            while (true) {
                if (request.isCanceled()) {
                    Response.CancelListener cancelListener = ((DownloadRequest) request).getCancelListener();
                    if (cancelListener != null)
                        cancelListener.onCancel();
                    break;
                }
                if ((count = in.read(buffer)) == -1) {
                    VolleyLog.wtf("downloadResume read buffer result is -1");
                    break;
                }

                randomAccessFile.write(buffer, 0, count);

                progress += count;
                final long postProgress = progress;
                ((DownloadRequest) request).setProgress(progress);
                if (postProgress(count, total, (DownloadRequest) request)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((DownloadRequest) request).getProgressListener().onProgress(total, postProgress);
                        }
                    });
                }
            }
            VolleyLog.d("downloadResume completed, file = " + file.getAbsolutePath() + ", url = " + request.getUrl());
            return ((DownloadRequest) request).getDownloadPath().getBytes();
        } finally {
            try {
                entity.consumeContent();
            } catch (IOException e) {
                VolleyLog.e("downloadResume entity to bytes consumingContent error");
            }
            mPool.returnBuf(buffer);
            randomAccessFile.close();
        }
    }

    /**
     * 下载新文件
     *
     * @param entity
     * @param request
     * @param file
     * @return
     * @throws java.io.IOException
     * @throws cn.wacao.waterfall.framework.http.volley.ServerError
     */
    private byte[] downloadNewFile(HttpEntity entity, final Request<?> request, File file) throws IOException, ServerError {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
        byte[] buffer = null;
        try {
            InputStream in = entity.getContent();
            if (in == null) {
                throw new ServerError();
            }
            final long total = entity.getContentLength();
            VolleyLog.d("downloadNewFile file = " + file.getAbsolutePath() + " content length = " + total);

            buffer = mPool.getBuf(DEFALUT_BUFFER_SIZE);
            int count;
            long progress = ((DownloadRequest) request).getProgress();

            while (true) {
                if (request.isCanceled()) {
                    Response.CancelListener cancelListener = ((DownloadRequest) request).getCancelListener();
                    if (cancelListener != null)
                        cancelListener.onCancel();
                    break;
                }
                if ((count = in.read(buffer)) == -1) {
                    VolleyLog.wtf("downloadNewFile read buffer result is -1");
                    break;
                }
                outputStream.write(buffer, 0, count);

                progress += count;
                final long postProgress = progress;
                ((DownloadRequest) request).setProgress(progress);
                if (postProgress(count, total, (DownloadRequest) request)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((DownloadRequest) request).getProgressListener().onProgress(total, postProgress);
                        }
                    });
                }
            }
            VolleyLog.d("downloadNewFile completed, file = " + file.getAbsolutePath() + ", url = " + request.getUrl());
            return ((DownloadRequest) request).getDownloadPath().getBytes();
        } finally {
            try {
                entity.consumeContent();
            } catch (IOException e) {
                VolleyLog.e("downloadNewFile entity to bytes consumingContent error");
            }
            mPool.returnBuf(buffer);
            outputStream.close();
        }
    }

    private String getFilePath(Request<?> request) {
        String filePath = ((DownloadRequest) request).getDownloadPath();
        return filePath;
    }

    private boolean postProgress(int count, long total, DownloadRequest request) {
        if (total < 0) {
            VolleyLog.wtf("download url = " + request.getUrl() + ", total is INVALID");
            return false;
        }
        if (request.getProgressListener() == null)
            return false;
        return true;
    }
}
