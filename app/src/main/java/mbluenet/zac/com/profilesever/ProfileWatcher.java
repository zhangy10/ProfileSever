package mbluenet.zac.com.profilesever;


import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;

import java.util.List;


/**
 * get resource info by the given interval
 */
public class ProfileWatcher extends BaseTask {


    private int testCount = 101;

    private static final int TEN_SECOND = 1000 * 10;
    private static String LAST_APP;
    // default 5s per, check one round resource
    private int interval = 1000 * 5;
    private Handler handler;
    private Context context;
    private IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    public ProfileWatcher(Handler handler, Context context) {
        this(0, handler, context);
    }

    public ProfileWatcher(int interval, Handler handler, Context context) {
        this.interval = interval == 0 ? this.interval : interval;
        this.handler = handler;
        this.context = context;
    }

    public String getCount() {
        return String.valueOf(++testCount);
    }

    /**
     * Check is in-use
     * <p>
     * need to go to security to open permission
     *
     * @param context
     * @return
     */
    public static String getScreenAc(Context context) {
        if (context == null) {
            return "";
        }
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - TEN_SECOND, time);
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return LAST_APP;
        }

        long end = System.currentTimeMillis();
//        log.d("[Get recent app usage list] size : " + queryUsageStats.size() + " cost time: " + (end - time));
        UsageStats lastApp = null;
        for (UsageStats usageStats : queryUsageStats) {
            if (lastApp == null ||
                    lastApp.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                lastApp = usageStats;
            }
        }
        LAST_APP = lastApp.getPackageName();
        return LAST_APP;
    }

    private static String formatMemoeryText(long memory) {
        float memoryInMB = memory * 1f / 1024 / 1024;
        return String.format("%.1f MB", memoryInMB);
    }

    /**
     * Get memory info
     *
     * @return
     */
    public static String getMemoryInfo() {
        long time = System.currentTimeMillis();
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long vmAlloc = total - rt.freeMemory();
        long nativeHeap = Debug.getNativeHeapSize();
        long nativeAlloc = Debug.getNativeHeapAllocatedSize();
        long max = rt.maxMemory();
        long end = System.currentTimeMillis();

//        log.d("[getMemoryInfo: time use]" + (end - time));
        return String.format("total: %s, vmAlloc: %s, nativeHeap: %s, nativeAlloc: %s, max: %s", formatMemoeryText(total),
                formatMemoeryText(vmAlloc), formatMemoeryText(nativeHeap), formatMemoeryText(nativeAlloc), formatMemoeryText(max));
    }

    public static String getActivityMemInfo(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //查看对内存限制  manifest 申请更多堆内存 使用 largeHeap=“true”
        int memoryClass = activityManager.getMemoryClass();
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        //表示系统剩余内存
        long availMem = memoryInfo.availMem;
        //它是boolean值，表示系统是否处于低内存运行
        boolean lowMemory = memoryInfo.lowMemory;
        //内存阀值 低于这个值就是第内存运行
        long threshold = memoryInfo.threshold;

        //获取debug调试信息
        Debug.MemoryInfo memoryInfo1 = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo1);
        //返回的是当前进程navtive堆本身总的内存大小
        long nativeHeapSize = Debug.getNativeHeapSize();
        //返回的是当前进程navtive堆中已使用的内存大小
        long nativeHeapAllocatedSize = Debug.getNativeHeapAllocatedSize();
        //返回的是当前进程navtive堆中已经剩余的内存大小
        long nativeHeapFreeSize = Debug.getNativeHeapFreeSize();

        return null;
    }

    /**
     * Get battery info
     *
     * @return
     */
    public static String getBattery(Context context, IntentFilter ifilter) {
        if (context != null) {
            long time = System.currentTimeMillis();
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level / (float) scale;
            long end = System.currentTimeMillis();
//            log.d("[getBattery: time use ]" + (end - time));
            return "isCharging: " + isCharging + " battery: " + batteryPct + " level: " + level + " scale: " + scale;
        }
        return "";
    }

    @Override
    public boolean runTask() throws Exception {
        while (!stop) {
            // after interval time, get resource info
            Thread.sleep(interval);
            String out = "-[TOP activity: ] " + getScreenAc(context) + "\n" +
                    "-[Memory: ] " + getMemoryInfo() + "\n" +
                    "-[Battery: ] " + getBattery(context, ifilter) + "\n" +
                    "-[Count: ] " + testCount;
//            log.d(out);
            if (handler != null) {
                Message msg = new Message();
                msg.what = GattServerActivity.LOG_PROFILE;
                msg.obj = out;
                handler.sendMessage(msg);
            }
        }
        return true;
    }

    @Override
    public void stop() {
        this.ifilter = null;
        this.context = null;
        super.stop();
    }
}
