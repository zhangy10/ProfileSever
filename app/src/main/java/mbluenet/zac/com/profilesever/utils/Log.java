package mbluenet.zac.com.profilesever.utils;

public class Log {

    private boolean isOpen = true;
    private static Log logInstance = null;

    public static final String LOG_TAG = "mbnet";

    public synchronized static Log getInstance() {
        if (logInstance == null) {
            logInstance = new Log();
        }
        return logInstance;
    }

    public Log() {
    }

    public Log(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void d(String msg) {
        if (isOpen) android.util.Log.d(LOG_TAG, msg);
    }

    public void i(String msg) {
        if (isOpen) android.util.Log.i(LOG_TAG, msg);
    }

    public void e(String msg) {
        if (isOpen) android.util.Log.e(LOG_TAG, msg);
    }

    public void d(String tag, String msg) {
        if (isOpen) android.util.Log.d(tag, msg);
    }

    public void i(String tag, String msg) {
        if (isOpen) android.util.Log.i(tag, msg);
    }

    public void e(String tag, String msg) {
        if (isOpen) android.util.Log.e(tag, msg);
    }
}
