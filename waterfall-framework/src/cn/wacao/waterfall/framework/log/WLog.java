package cn.wacao.waterfall.framework.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Environment;
import android.os.Process;
import android.util.Log;

public class WLog {

    /**
     * Log options.
     */
    public static class LogOptions {
        public static final int LEVEL_VERBOSE = 1;
        public static final int LEVEL_DEBUG = 2;
        public static final int LEVEL_INFO = 3;
        public static final int LEVEL_WARN = 4;
        public static final int LEVEL_ERROR = 5;

        /**
         * Uniform tag to be used as log tag; null-ok, if this is null, will use
         * the tag argument in log methods.
         */
        public String uniformTag;

        /**
         * When it is null, all stack traces will be output. Usually this can be
         * set the application package name.
         */
        public String stackTraceFilterKeyword;

        /**
         * The level at which the log method really works(output to DDMS and
         * file).
         * <p/>
         * NOTE this setting excludes the file writing of VERBOSE
         * except when set {@link #honorVerbose} to true explicitly.
         * If logLevel is LEVEL_VERBOSE:
         * a) when honorVerbose is true, will output all logs to DDMS and file.
         * b) when honorVerbose is false(default), will output all levels no less
         * than LEVEL_DEBUG to DDMS and file, but for verbose, will only output
         * to DDMS.
         * <p/>
         * <p/>
         * MUST be one of the LEVEL_* constants.
         */
        public int logLevel = LEVEL_INFO;

        public boolean honorVerbose = false;

        /**
         * Maximum backup log files' size in MB. Can be 0, which means no back
         * up logs(old logs to be discarded).
         */
        public int backUpLogLimitInMB = LogToES.DEFAULT_BAK_FILE_NUM_LIMIT
                * LogToES.MAX_FILE_SIZE;

        /**
         * Default file buffer size. Must be positive.
         */
        public int buffSizeInBytes = LogToES.DEFAULT_BUFF_SIZE;

        /**
         * Log file name, should not including the directory part. Must be a
         * valid file name(for Android file system).
         */
        public String logFileName = "logs.txt";
    }

    private static volatile LogOptions sOptions = new LogOptions();

    private static final ExecutorService sThread = Executors
            .newSingleThreadExecutor();

    /**
     * @param directory Where to put the logs folder. Should be a writable directory.
     * @return True for succeeded, false otherwise.
     */
    public static boolean initialize(String directory) {
        return LogToES.setLogPath(directory);
    }

    /**
     * @param directory Where to put the logs folder.
     * @param options   null-ok. Options for log methods.
     * @return True for succeeded, false otherwise.
     */
    public static boolean initialize(String directory, LogOptions options) {
        setOptions(options);
        return LogToES.setLogPath(directory);
    }

    /**
     * Make sure initialize is called before calling this.
     */
    public static void setUniformTag(String tag) {
        if (tag != null && tag.length() != 0) {
            sOptions.uniformTag = tag;
        }
    }

    public static String getLogPath() {
        return LogToES.getLogPath();
    }

    public static LogOptions getOptions() {
        return sOptions;
    }

    private static boolean setOptions(LogOptions options) {
        final LogOptions tmpOp = (options == null ? new LogOptions() : options);
        sOptions = tmpOp;
        LogToES.setBackupLogLimitInMB(tmpOp.backUpLogLimitInMB);
        LogToES.setBuffSize(tmpOp.buffSizeInBytes);
        return tmpOp.buffSizeInBytes > 0
                && !isNullOrEmpty(tmpOp.logFileName);
    }

    /**
     * Output verbose log. Exception will be caught if input arguments have
     * format error.
     * <p/>
     * NOTE {@link #initialize(String)} or
     * {@link #initialize(String, cn.wacao.waterfall.framework.log.WLog.LogOptions)} must be called before calling
     * this.
     *
     * @param obj
     * @param format The format string such as "This is the %d sample : %s".
     * @param args   The args for format.
     *               <p/>
     *               Reference : boolean : %b. byte, short, int, long, Integer, Long
     *               : %d. NOTE %x for hex. String : %s. Object : %s, for this
     *               occasion, toString of the object will be called, and the
     *               object can be null - no exception for this occasion.
     */
    public static void verbose(Object obj, String format, Object... args) {
        final boolean shouldOutputVerboseToDDMS = shouldOutputVerboseToDDMS();
        final boolean shouldOutputVerboseToFile = shouldOutputVerboseToFile();
        if (shouldOutputVerboseToDDMS || shouldOutputVerboseToFile) {
            try {
                int line = getCallerLineNumber();
                String filename = getCallerFilename();
                outputVerbose(obj, line, filename, format,
                        shouldOutputVerboseToDDMS, shouldOutputVerboseToFile, args);
            } catch (java.util.IllegalFormatException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Output debug log. This version aims to improve performance by removing
     * the string concatenated costs on release version. Exception will be
     * caught if input arguments have format error.
     * <p/>
     * NOTE {@link #initialize(String)} or
     * {@link #initialize(String, cn.wacao.waterfall.framework.log.WLog.LogOptions)} must be called before calling
     * this.
     *
     * @param obj
     * @param format The format string such as "This is the %d sample : %s".
     * @param args   The args for format.
     *               <p/>
     *               Reference : boolean : %b. byte, short, int, long, Integer, Long
     *               : %d. NOTE %x for hex. String : %s. Object : %s, for this
     *               occasion, toString of the object will be called, and the
     *               object can be null - no exception for this occasion.
     */
    public static void debug(Object obj, String format, Object... args) {
        if (shouldWriteDebug()) {
            int line = getCallerLineNumber();
            String filename = getCallerFilename();
            outputDebug(obj, format, line, filename, args);
        }
    }

    /**
     * Output information log. Exception will be caught if input arguments have
     * format error.
     * <p/>
     * NOTE {@link #initialize(String)} or
     * {@link #initialize(String, cn.wacao.waterfall.framework.log.WLog.LogOptions)} must be called before calling
     * this.
     *
     * @param obj
     * @param format The format string such as "This is the %d sample : %s".
     * @param args   The args for format.
     *               <p/>
     *               Reference : boolean : %b. byte, short, int, long, Integer, Long
     *               : %d. NOTE %x for hex. String : %s. Object : %s, for this
     *               occasion, toString of the object will be called, and the
     *               object can be null - no exception for this occasion.
     */
    public static void info(Object obj, String format, Object... args) {
        if (shouldWriteInfo()) {
            try {
                int line = getCallerLineNumber();
                String filename = getCallerFilename();
                outputInfo(obj, format, line, filename, args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Output warning log. Exception will be caught if input arguments have
     * format error.
     * <p/>
     * NOTE {@link #initialize(String)} or
     * {@link #initialize(String, cn.wacao.waterfall.framework.log.WLog.LogOptions)} must be called before calling
     * this.
     *
     * @param obj
     * @param format The format string such as "This is the %d sample : %s".
     * @param args   The args for format.
     *               <p/>
     *               Reference : boolean : %b. byte, short, int, long, Integer, Long
     *               : %d. NOTE %x for hex. String : %s. Object : %s, for this
     *               occasion, toString of the object will be called, and the
     *               object can be null - no exception for this occasion.
     */
    public static void warn(Object obj, String format, Object... args) {
        if (shouldWriteWarn()) {
            try {
                int line = getCallerLineNumber();
                String filename = getCallerFilename();
                outputWarning(obj, format, line, filename, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Output error log. Exception will be caught if input arguments have format
     * error.
     * <p/>
     * NOTE {@link #initialize(String)} or
     * {@link #initialize(String, cn.wacao.waterfall.framework.log.WLog.LogOptions)} must be called before calling
     * this.
     *
     * @param obj
     * @param format The format string such as "This is the %d sample : %s".
     * @param args   The args for format.
     *               <p/>
     *               Reference : boolean : %b. byte, short, int, long, Integer, Long
     *               : %d. NOTE %x for hex. String : %s. Object : %s, for this
     *               occasion, toString of the object will be called, and the
     *               object can be null - no exception for this occasion.
     */
    public static void error(Object obj, String format, Object... args) {
        if (shouldWriteError()) {
            try {
                int line = getCallerLineNumber();
                String filename = getCallerFilename();
                outputError(obj, format, line, filename, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Output an error log with contents of a Throwable.
     * <p/>
     * NOTE {@link #initialize(String)} or
     * {@link #initialize(String, cn.wacao.waterfall.framework.log.WLog.LogOptions)} must be called before calling
     * this.
     *
     * @param t An Throwable instance.
     */
    public static void error(Object obj, Throwable t) {
        if (shouldWriteError()) {
            int line = getCallerLineNumber();
            String filename = getCallerFilename();
            String methodname = getCallerMethodName();
            outputError(obj, t, line, filename, methodname);
        }
    }

    /**
     * Flush the written logs. The log methods write logs to a buffer.
     * <p/>
     * NOTE this will be called if close is called.
     */
    public static void flush() {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                LogToES.flush();
            }
        };

        executeCommand(command);
    }

    /**
     * Close the logging task. Flush will be called here. Failed to call this
     * may cause some logs lost.
     */
    public static void close() {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                if (externalStorageExist()) {
                    LogToES.close();
                }
            }
        };

        executeCommand(command);
    }

    public static boolean isOpen() {
        return !sThread.isShutdown() && !sThread.isTerminated() && LogToES.isOpen();
    }

    private static void executeCommand(final Runnable command) {
        sThread.execute(command);
    }

    private static String objClassName(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        } else {
            return obj.getClass().getSimpleName();
        }
    }

    private static void writeToLog(final String logText) {
        final long timeMillis = System.currentTimeMillis();
        final Runnable command = new Runnable() {
            @Override
            public void run() {
                if (externalStorageExist()) {
                    try {
                        LogToES.writeLogToFile(LogToES.getLogPath(),
                                sOptions.logFileName, logText, false, timeMillis);
                    } catch (Throwable e) {
                        Log.e("WLogs", "writeToLog fail, " + e);
                    }
                }
            }
        };
        executeCommand(command);
    }

    private static void logToFile(String logText, Throwable t) {
        StringWriter sw = new StringWriter();
        sw.write(logText);
        sw.write("\n");
        t.printStackTrace(new PrintWriter(sw));
        writeToLog(sw.toString());
    }

    private static String msgForException(Object obj, String methodname,
                                          String filename, int line) {
        StringBuilder sb = new StringBuilder();
        if (obj instanceof String)
            sb.append((String) obj);
        else
            sb.append(obj.getClass().getSimpleName());
        sb.append(" Exception occurs at ");
        sb.append("(P:");
        sb.append(Process.myPid());
        sb.append(")");
        sb.append("(T:");
        sb.append(Thread.currentThread().getId());
        sb.append(") at ");
        sb.append(methodname);
        sb.append(" (");
        sb.append(filename);
        sb.append(":" + line);
        sb.append(")");
        String ret = sb.toString();
        return ret;
    }

    private static String msgForTextLog(Object obj, String filename, int line,
                                        String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        sb.append("(P:");
        sb.append(Process.myPid());
        sb.append(")");
        sb.append("(T:");
        sb.append(Thread.currentThread().getId());
        sb.append(")");
        sb.append("(C:");
        sb.append(objClassName(obj));
        sb.append(")");
        sb.append("at (");
        sb.append(filename);
        sb.append(":");
        sb.append(line);
        sb.append(")");
        String ret = sb.toString();
        return ret;
    }

    private static int getCallerLineNumber() {
        return Thread.currentThread().getStackTrace()[4].getLineNumber();
    }

    private static String getCallerFilename() {
        return Thread.currentThread().getStackTrace()[4].getFileName();
    }

    private static String getCallerMethodName() {
        return Thread.currentThread().getStackTrace()[4].getMethodName();
    }

    private static String getThreadStacksKeyword() {
        return sOptions.stackTraceFilterKeyword;
    }

    public static void printThreadStacks() {
        printThreadStacks(tagOfStack(), getThreadStacksKeyword(), false, false);
    }

    public static void printThreadStacks(String tag) {
        printThreadStacks(tag, getThreadStacksKeyword(),
                isNullOrEmpty(getThreadStacksKeyword()), false);
    }

    public static void printThreadStacks(Throwable e, String tag) {
        printStackTraces(e.getStackTrace(), tag);
    }

    public static void printThreadStacks(String tag, String keyword) {
        printThreadStacks(tag, keyword, false, false);
    }

    // tag is for output identifier.
    // keyword is for filtering irrelevant logs.
    public static void printThreadStacks(String tag, String keyword,
                                         boolean fullLog, boolean release) {
        printStackTraces(Thread.currentThread().getStackTrace(), tag, keyword,
                fullLog, release);
    }

    public static void printStackTraces(StackTraceElement[] traces, String tag) {
        printStackTraces(traces, tag, getThreadStacksKeyword(),
                isNullOrEmpty(sOptions.stackTraceFilterKeyword), false);
    }

    private static void printStackTraces(StackTraceElement[] traces,
                                         String tag, String keyword, boolean fullLog, boolean release) {
        printLog(tag, "------------------------------------", release);
        for (StackTraceElement e : traces) {
            String info = e.toString();
            if (fullLog
                    || (!isNullOrEmpty(keyword) && info.indexOf(keyword) != -1)) {
                printLog(tag, info, release);
            }
        }
        printLog(tag, "------------------------------------", release);
    }

    private static void printLog(String tag, String log, boolean release) {
        if (release) {
            info(tag, log);
        } else {
            debug(tag, log);
        }
    }

    public static String stackTraceOf(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    public static String stackTrace() {
        try {
            throw new RuntimeException();
        } catch (Exception e) {
            return stackTraceOf(e);
        }
    }

    private static String tag(Object tag) {
        final LogOptions options = sOptions;
        return (options.uniformTag == null ? (tag instanceof String ? (String) tag
                : tag.getClass().getSimpleName())
                : options.uniformTag);
    }

    private static String tagOfStack() {
        return (sOptions.uniformTag == null ? "CallStack" : sOptions.uniformTag);
    }

    private static boolean shouldOutputVerboseToDDMS() {
        return sOptions.logLevel <= LogOptions.LEVEL_VERBOSE;
    }

    private static boolean shouldOutputVerboseToFile() {
        return sOptions.logLevel <= LogOptions.LEVEL_VERBOSE && sOptions.honorVerbose;
    }

    private static boolean shouldWriteDebug() {
        return sOptions.logLevel <= LogOptions.LEVEL_DEBUG;
    }

    private static boolean shouldWriteInfo() {
        return sOptions.logLevel <= LogOptions.LEVEL_INFO;
    }

    private static boolean shouldWriteWarn() {
        return sOptions.logLevel <= LogOptions.LEVEL_WARN;
    }

    private static boolean shouldWriteError() {
        return sOptions.logLevel <= LogOptions.LEVEL_ERROR;
    }

    private static boolean externalStorageExist() {
        return Environment.getExternalStorageState().equalsIgnoreCase(
                Environment.MEDIA_MOUNTED);
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private static void outputVerbose(final Object obj, final int line,
                                      final String filename, final String format, boolean outToDDMS, boolean outToFile, final Object... args) {
        try {
            String msg = String.format(format, args);
            String logText = msgForTextLog(obj, filename, line, msg);
            if (outToDDMS) {
                Log.v(tag(obj), logText);
            }
            if (outToFile) {
                writeToLog(logText);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void outputDebug(final Object obj, final String format,
                                    final int line, final String filename, final Object... args) {
        try {
            String msg = String.format(format, args);
            String logText = msgForTextLog(obj, filename, line, msg);
            Log.d(tag(obj), logText);
            writeToLog(logText);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void outputInfo(final Object obj, final String format,
                                   final int line, final String filename, final Object... args) {
        try {
            String msg = String.format(format, args);
            String logText = msgForTextLog(obj, filename, line, msg);
            Log.i(tag(obj), logText);
            writeToLog(logText);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void outputWarning(final Object obj, final String format,
                                      final int line, final String filename, final Object... args) {
        try {
            String msg = String.format(format, args);
            String logText = msgForTextLog(obj, filename, line, msg);
            Log.w(tag(obj), logText);
            writeToLog(logText);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void outputError(final Object obj, final String format,
                                    final int line, final String filename, final Object... args) {
        try {
            String msg = String.format(format, args);
            String logText = msgForTextLog(obj, filename, line, msg);
            // If the last arg is a throwable, print the stack.
            if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
                Throwable t = (Throwable) args[args.length - 1];
                Log.e(tag(obj), logText, t);
                logToFile(logText, t);
            } else {
                Log.e(tag(obj), logText);
                writeToLog(logText);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void outputError(final Object obj, final Throwable t,
                                    final int line, final String filename, final String methodname) {
        try {
            String logText = msgForException(obj, methodname, filename, line);
            Log.e(tag(obj), logText, t);
            logToFile(logText, t);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
}
