package cn.wacao.waterfall.framework.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by xuwakao on 13-8-28.
 */
public class FileUtils {

    /**
     * Get the root file of external storage.
     * <p/>
     * This file can not be shared.
     *
     * @param context
     * @return
     */
    public static File getExtStorageRoot(Context context) {
        return new File(Environment.getExternalStorageDirectory().getPath() + File.separator + context.getPackageName());
    }

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     * <p/>
     * -------------Warning--------------------
     * <p/>
     * external directory may be deleted when the app is uninstalled.
     * <p/>
     * internal directory may be cleaned when app's memory is low
     * <p/>
     * if want to store something which persist even when app has been uninstalled,dont use this method.
     * <p/>
     * Use the method {@link #getPermanentExtStorageDir(android.content.Context, String, String)}
     * <p/>
     * --------------------------------------
     *
     * @param context    The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        /**
         * This expression is important because External files are not always available: they will disappear if the
         * user mounts the external storage on a computer or removes it.
         * @see {@link android.content.Context#getExternalCacheDir}
         */;
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
                !isExternalStorageRemovable() ?
                getExternalCacheDir(context).getPath() :
                context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     * otherwise.
     */
    @TargetApi(9)
    public static boolean isExternalStorageRemovable() {
        if (Version.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
//    @TargetApi(8)
    public static File getExternalCacheDir(Context context) {
//        if (Version.hasFroyo()) {
//            return context.getExternalCacheDir();
//        }

        // Before Froyo we need to construct the external cache dir ourselves
        /**final String cacheDir = "/Android/data/" + context.getPackageName() + File.separator + "cache";
        return new File(Environment.getExternalStorageDirectory().getPath() + File.separator + cacheDir);**/
        return new File(Environment.getExternalStorageDirectory().getPath());
    }

    /**
     * Get the internal app cach directory(e.g. /data/data/com.xuwakao.mixture/cache)
     *
     * @param context The context to use
     * @return The internal cache dir
     */
    public static File getInternalCacheDir(Context context) {
        return context.getCacheDir();
    }

    /**
     * Get interanl file directory(e.g. /data/data/com.xuwakao.mixture/files)
     *
     * @param context The context to use
     * @return The file representing an internal directory for your app.
     */
    public static File getInternalFilesDir(Context context) {
        return context.getFilesDir();
    }

    /**
     * Get directory on external storage that would not be deleted even when app would hava been uninstalled.
     * And this dirtory is shared by all apps.
     * <p/>
     * If you want to save files that are not specific to your application and
     * that should not be deleted when your application is uninstalled, save them to one of the public directories on the external storage.
     * <p/>
     * In API Level 8 or greater, use getExternalStoragePublicDirectory(),
     * passing it the type of public directory you want, such as DIRECTORY_MUSIC, DIRECTORY_PICTURES, DIRECTORY_RINGTONES, or others.
     * This method will create the appropriate directory if necessary.
     * <p/>
     * If you're using API Level 7 or lower, use getExternalStorageDirectory() to open a File that represents the root of the external storage
     *
     * @param context The context to use
     * @param type    The type of storage directory to return.  Should be one of
     *                {@link android.os.Environment#DIRECTORY_MUSIC}, {@link android.os.Environment#DIRECTORY_PODCASTS},
     *                {@link android.os.Environment#DIRECTORY_RINGTONES}, {@link android.os.Environment#DIRECTORY_ALARMS},
     *                {@link android.os.Environment#DIRECTORY_NOTIFICATIONS}, {@link android.os.Environment#DIRECTORY_PICTURES},
     *                {@link android.os.Environment#DIRECTORY_MOVIES}, {@link android.os.Environment#DIRECTORY_DOWNLOADS}, or
     *                {@link android.os.Environment#DIRECTORY_DCIM}.  May not be null.
     * @param uniqueName The unique name of directory
     * @return
     */
    @TargetApi(8)
    public static File getPermanentExtStorageDir(Context context, String type, String uniqueName) {
        if (Version.hasFroyo()) {
            return Environment.getExternalStoragePublicDirectory(type);
        }

        return new File(getExtStorageRoot(context).getPath() + File.separator + uniqueName);
    }


    /**
     * Get directory on external storage that would be deleted even when app would be uninstalled.
     * And this directory is private the this app.
     * <p/>
     * If you're using API Level 8 or greater, use getExternalFilesDir() to open a File that represents
     * the external storage directory where you should save your files.
     * This method takes a type parameter that specifies the type of subdirectory you want,
     * such as DIRECTORY_MUSIC and DIRECTORY_RINGTONES (pass null to receive the root of your application's file directory).
     * This method will create the appropriate directory if necessary.
     * <p/>
     * If you're using API Level 7 or lower, use getExternalStorageDirectory(),
     * to open a File representing the root of the external storage. You should then write your data in the following directory:
     * <p/>
     * \/Android\/data\/<package_name>\/files
     *
     * @param context
     * @param type
     * @param uniqueName The unique name of directory
     * @return
     */
    @TargetApi(8)
    public static File getImpermanentExtStorageDir(Context context, String type, String uniqueName) {
        if (Version.hasFroyo()) {
            return context.getExternalFilesDir(type);
        }

        return new File(getExtStorageRoot(context).getPath() + File.separator + uniqueName);
    }

    /**
     * Checks if external storage is available for read and write
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static File buildPath(File base, String... segments) {
        File cur = base;
        for (String segment : segments) {
            if (cur == null) {
                cur = new File(segment);
            } else {
                cur = new File(cur, segment);
            }
        }
        return cur;
    }

    public static class ExtStorageNotFoundExecption extends Exception {
        public ExtStorageNotFoundExecption() {
            super();
        }

        public ExtStorageNotFoundExecption(String detail) {
            super(detail);
        }
    }
}
