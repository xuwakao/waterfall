package cn.wacao.waterfall.framework.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import android.os.SystemClock;
import android.util.Log;

public class LogToES {
    private static final String BAK_EXT = ".bak";

    private static final FastDateFormat LOG_FORMAT = FastDateFormat
            .getInstance("yyyy:MM:dd kk:mm:ss");

    private static FastDateFormat simpleDateFormat = FastDateFormat
            .getInstance("-MM-dd-kk-mm-ss");

    /**
     * 10 days.
     */
    private static final long DAY_DELAY = 10L * 24 * 60 * 60 * 1000;

    /**
     * In MB.
     */
    public static final int MAX_FILE_SIZE = 2;

    public static final int DEFAULT_BAK_FILE_NUM_LIMIT = 2;

    /**
     * Back file num limit, when this is exceeded, will delete older logs.
     */
    private static int mBackFileNumLimit = DEFAULT_BAK_FILE_NUM_LIMIT;

    public static final int DEFAULT_BUFF_SIZE = 32 * 1024;

    /**
     * Buffer size , threshold for flush/close.
     */
    private static int BUFF_SIZE = DEFAULT_BUFF_SIZE;

    private static Object mLock = new Object();

    /**
     * These two are protected by mLock.
     */
    private static BufferedWriter mWriter;
    private static String mPath;

    /**
     * To flush by interval.
     */
    private static long mLastMillis = 0;
    private static final long FLUSH_INTERVAL = 5000;

    private volatile static String mLogPath;

    public static void setBackupLogLimitInMB(int logCapacityInMB) {
        mBackFileNumLimit = (logCapacityInMB + MAX_FILE_SIZE - 1)
                / MAX_FILE_SIZE;
    }

    public static boolean setLogPath(String logDir) {
        if (logDir == null || logDir.length() == 0) {
            return false;
        }
        mLogPath = logDir;

        new File(logDir).mkdirs();

        return new File(logDir).isDirectory();
    }

    public static String getLogPath() {
        return mLogPath;
    }

    public static void setBuffSize(int bytes) {
        BUFF_SIZE = bytes;
    }

    public static void writeLogToFile(String dir, String fileName, String msg,
                                      boolean immediateClose, long timeMillis) throws IOException {
        writeLog(dir, fileName, msg, immediateClose, timeMillis);
    }

    public static void writeLog(String path, String fileName, String msg,
                                boolean immediateClose, long timeMillis) throws IOException {
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        File logFile = new File(
                path.endsWith(File.separator) ? (path + fileName) : (path
                        + File.separator + fileName));
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            long fileSize = (logFile.length() >>> 20);// convert to M bytes
            if (fileSize > MAX_FILE_SIZE) {
                deleteOldLogs();

                String fileExt = simpleDateFormat.format(timeMillis);

                StringBuilder sb = new StringBuilder(path);
                sb.append(File.separator).append(fileName).append(fileExt)
                        .append(BAK_EXT);

                File fileNameTo = new File(sb.toString());
                logFile.renameTo(fileNameTo);

                limitVolume();
            }
        }

        String strLog = LOG_FORMAT.format(timeMillis);

        StringBuffer sb = new StringBuffer(strLog);
        sb.append(' ');
        sb.append(msg);
        sb.append('\n');
        strLog = sb.toString();

        synchronized (mLock) {
            boolean needCreate = false;
            if (mPath == null) {
                mPath = logFile.getAbsolutePath();
                needCreate = true;
            } else if (!equal(mPath, logFile.getAbsolutePath())) {
                BufferedWriter writer = mWriter;
                if (writer != null) {
                    writer.close();
                }
                mWriter = null;
                mPath = null;
                needCreate = true;
            }

            BufferedWriter bufWriter = mWriter;

            if (needCreate || bufWriter == null) {
                mPath = logFile.getAbsolutePath();
                FileWriter fileWriter = new FileWriter(logFile, true);
                bufWriter = new BufferedWriter(fileWriter, BUFF_SIZE);
                mWriter = bufWriter;
            }

            // we can make FileWriter static, but when to close it
            bufWriter.write(strLog);

            // It doesn't matter there are multiple files gets mixed.
            final long curMillis = SystemClock.elapsedRealtime();
            if (curMillis - mLastMillis >= FLUSH_INTERVAL) {
                bufWriter.flush();
                mLastMillis = curMillis;
            }

            if (immediateClose) {
                bufWriter.close();
                mPath = null;
                mWriter = null;
            }
        }
    }

    private static boolean equal(String s1, String s2) {
        if (s1 != null && s2 != null) {
            return s1.equals(s2);
        } else {
            return s1 == null && s2 == null;
        }
    }

    private static void deleteOldLogs() {
        String dir = getLogPath();
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            return;
        }

        long now = System.currentTimeMillis();
        File files[] = dirFile.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.getName().endsWith(BAK_EXT)) {
                long lastModifiedTime = file.lastModified();
                if (now - lastModifiedTime > DAY_DELAY) {
                    file.delete();
                }
            }
        }
    }

    private static void limitVolume() {
        String dir = getLogPath();
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            return;
        }

        final File files[] = dirFile.listFiles();
        if (files == null || files.length <= Math.max(0, mBackFileNumLimit)) {
            return;
        }

        int numOfDeletable = 0;
        for (int i = 0, N = files.length; i < N; i++) {
            File file = files[i];
            if (file.getName().endsWith(BAK_EXT)) {
                ++numOfDeletable;
            }
        }

        if (numOfDeletable <= 0) {
            // really weird, the naming rule have been changed!
            // this function won't work anymore.
            return;
        }

        // the logs.txt and uncaught_exception.txt may be missing,
        // so just allocate same size as the old.
        File[] deletables = new File[numOfDeletable];
        int i = 0;
        for (File e : files) {
            if (i >= numOfDeletable) {
                // unexpected case.
                break;
            }
            if (e.getName().endsWith(BAK_EXT)) {
                deletables[i++] = e;
            }
        }

        deleteIfOutOfBound(deletables);
    }

    private static void deleteIfOutOfBound(File[] files) {
        if (files.length <= mBackFileNumLimit) {
            return;
        }

        // sort files by create time(time is on the file name) DESC.
        Comparator<? super File> comparator = new Comparator<File>() {

            @Override
            public int compare(File lhs, File rhs) {
                return rhs.getName().compareTo(lhs.getName());
            }

        };

        Arrays.sort(files, comparator);

        final int filesNum = files.length;

        // delete files from index to size.
        for (int i = mBackFileNumLimit; i < filesNum; ++i) {
            File file = files[i];
            if (!file.delete()) {
                // NOTE here we cannot call YLog, we are to be depended by YLog.
                Log.e("LogToES", "LogToES failed to delete file " + file);
            }
        }
    }

    public static void flush() {
        synchronized (mLock) {
            BufferedWriter writer = mWriter;
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void close() {
        synchronized (mLock) {
            BufferedWriter writer = mWriter;
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mPath = null;
        }
    }

    public static boolean isOpen() {
        synchronized (mLock) {
            BufferedWriter writer = mWriter;
            return writer != null;
        }
    }

}
