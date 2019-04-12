package mbluenet.zac.com.profilesever;

import java.util.concurrent.LinkedBlockingQueue;

import mbluenet.zac.com.profilesever.utils.Log;
import mbluenet.zac.com.profilesever.utils.UtilHelper;


/**
 * Base loop thread model with a block queue
 */
public abstract class BaseTask implements Runnable {

    protected static final Log log = Log.getInstance();

    protected boolean stop = true;
    private LinkedBlockingQueue<BaseTask> queue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        stop = false;
        try {
            // e.g. if connection should not be closed, each connection can set
            // (isLoop = true) to keep the connection thread alive.
            boolean isLoop = true;
            while (isLoop && !stop) {
                isLoop = queue.take().runTask();
            }
        } catch (Exception e) {
            log.e(UtilHelper.getExceptionLog("!!! Task has been terminated by the exception: ", e));
        } finally {
            stop();
        }
    }

    /**
     * true: the thread will keep doing runnable by the post() method until
     * stop.
     * <p>
     * false: stop doing runnable.
     * <p>
     * default value should return false;
     */
    public abstract boolean runTask() throws Exception;

    /**
     * Provided for showing progress dialog or something that indicates user
     * doing tasks.
     */
    protected void preTask() {
    }

    public boolean isRunning() {
        return !stop;
    }

    public void stop() {
        if (!stop) {
            stop = true;
            post(new StopLoop());
            log.d(this.getClass().getSimpleName()
                    + " stop the task loop: finish as a thread");
        }
    }

    public void start() {
        start(this);
    }

    protected void start(BaseTask task) {
        post(task);
        // Starting a thread or adding this runnable into a thread-pool.
        Thread thread = new Thread(this);
        thread.setName(this.getClass().getSimpleName());
        thread.start();
    }

    protected void post(BaseTask runnable) {
        runnable.preTask();
        queue.offer(runnable);
    }

    public class StopLoop extends BaseTask {

        @Override
        public boolean runTask() {
            // do nothing, just for gracefully interrupt the thread with a message
            // queue.
            return false;
        }

    }
}
