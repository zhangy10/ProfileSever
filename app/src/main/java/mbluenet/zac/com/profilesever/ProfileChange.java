package mbluenet.zac.com.profilesever;


import android.os.Handler;
import android.os.Message;

import mbluenet.zac.com.profilesever.utils.Log;
import mbluenet.zac.com.profilesever.utils.UtilHelper;

public class ProfileChange extends BaseTask{

    private static final Log log = Log.getInstance();
    private static final int INTERVAL = 1000 * 1;
    private int profile = 0;
    private Handler handler;

    public ProfileChange(Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean runTask() {

        while(!stop) {
            profile++;
            if (handler != null) {
                Message msg = new Message();
                msg.obj = profile;
                msg.what = GattServerActivity.PROFILE_CHANGE;
                handler.sendMessage(msg);
            }

            try {
                Thread.sleep(INTERVAL);
            } catch (Exception e) {
                log.e(UtilHelper.getExceptionLog("Thread sleep error...", e));
            }
        }

        return false;
    }
}
