package cn.wacao.waterfall.framework.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import cn.wacao.waterfall.framework.AppConfig;
import cn.wacao.waterfall.framework.AppConstant;
import cn.wacao.waterfall.framework.http.volley.*;
import cn.wacao.waterfall.framework.http.volley.toolbox.*;
import cn.wacao.waterfall.framework.log.WLog;
import cn.wacao.waterfall.framework.utils.FileUtils;
import cn.wacao.waterfall.framework.utils.Function;
import cn.wacao.waterfall.framework.utils.MD5Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wacao on 14-9-25.
 */
public class HttpMgr {

    private static HttpMgr mMgr;
    public static final int HTTP_CACHE_MAX_MEMORY_SIZE = 5 * 1024 * 1024;
    public static final int IMAGE_HTTP_CACHE_MAX_MEMORY_SIZE = 50 * 1024 * 1024;

    private RequestQueue mQueue;
    private Cache mCache;

    private RequestQueue mDownloadQueue;
    private File mDownloadRoot;

    private RequestQueue mImageQueue;
    private ImageLoader mImageLoader;
    private Cache mImageCache;

    private HttpMgr() {
    }

    public static synchronized HttpMgr getInstance() {
        if (mMgr == null) {
            mMgr = new HttpMgr();
        }
        return mMgr;
    }

    public synchronized void init(Context context) {
        initRequest(context);
        initImageLoader(context);
    }

    private void initDownloadRequest(Context context) {
        Cache cache = new NoCache();
        HttpStack stack = new HurlStack();

        Network network = new DownloadNetwork(stack);

        mDownloadQueue = new RequestQueue(cache, network);
        mDownloadQueue.start();

        mDownloadRoot = FileUtils.getDiskCacheDir(context, AppConstant.APP_NAME + File.separator + AppConstant.HTTP_DOWNLOAD_CACHE);
        if (!mDownloadRoot.exists())
            mDownloadRoot.mkdir();
    }

    private void initRequest(Context context) {
        mCache = new DiskBasedCache(FileUtils.getDiskCacheDir(context, AppConstant.APP_NAME + File.separator + AppConstant.HTTP_CACHE), HTTP_CACHE_MAX_MEMORY_SIZE);
        HttpStack stack = new HurlStack();

        Network network = new BasicNetwork(stack);

        mQueue = new RequestQueue(mCache, network);
        mQueue.start();
    }

    private void initImageLoader(Context context) {
        mImageCache = new DiskBasedCache(FileUtils.getDiskCacheDir(context, AppConstant.APP_NAME + File.separator + AppConstant.HTTP_IMAGE_CACHE), IMAGE_HTTP_CACHE_MAX_MEMORY_SIZE);
        HttpStack stack = new HurlStack();
        Network network = new BasicNetwork(stack);

        mImageQueue = new RequestQueue(mImageCache, network);
        mImageLoader = new ImageLoader(this.mImageQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(20);

            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }

            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });
        mImageQueue.start();
    }

    /**
     * 执行get请求
     *
     * @param url
     * @param listener
     * @param errorListener
     */
    public void perfromStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        perfromStringRequest(url, null, listener, errorListener);
    }

    /**
     * 执行get请求
     *
     * @param url
     * @param listener
     * @param errorListener
     */
    public void perfromStringRequest(String url, Map<String, String> param, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        if (Function.empty(url) || listener == null || errorListener == null)
            return;
        url = fillParam(url, param);
        StringRequest request = new StringRequest(url, listener, errorListener);
        mQueue.add(request);
    }

    public static final String PARAM_APP_VERSION = "app_version";
    public static final String PARAM_PLATFORM = "platform";

    public static final String PARAM_KEY = "_key";
    public static final String PARAM_MARKET_ID = "market_id";
    public static final String PARAM_DEVICE_CODE = "device_code";

    private String fillParam(String url, Map<String, String> param) {
        if (Function.empty(param))
            param = new HashMap<String, String>();
        fillCommonParam(param);

        StringBuilder sb = new StringBuilder(url);
        sb.append("?");

        for (String key : param.keySet()) {
            sb.append(key).append("=").append(param.get(key)).append("&");
        }
        return sb.substring(0, sb.length() - 1);
    }

    private void fillCommonParam(Map<String, String> params) {
    }

    /**
     * @param url              资源文件路径
     * @param fileName         本地磁盘的文件名（仅仅是文件名，不是文件绝对路径）,可以为空，空的话，文件名就是url的MD5值
     * @param succListener     成功下载的回调
     * @param listener
     * @param progressListener
     */
    public void perfromDownloadRequest(String url, String fileName, Response.Listener<String> succListener, Response.ErrorListener listener, Response.ProgressListener progressListener) {
        if (Function.empty(url))
            return;

        if (mDownloadQueue == null) {
            initDownloadRequest(AppConfig.getInstance().getAppContext());
        }

        String file = fileName;
        if (Function.empty(fileName)) {
            file = MD5Utils.getMD5String(url);
        }
        String downloadPath = mDownloadRoot.getAbsolutePath() + File.separator + file;
        DownloadRequest request = new DownloadRequest(url, downloadPath, succListener, listener, progressListener);
        mDownloadQueue.add(request);
    }

    /**
     * 暂停下载
     *
     * @param url
     */
    public void cancelDownloadRequest(final String url) {
        mDownloadQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                if (request instanceof DownloadRequest) {
                    boolean apply = request.getUrl().equals(url);
                    WLog.verbose(this, "cancelDownloadRequest apply = " + apply);
                    return apply;
                }
                return false;
            }
        }, true);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
