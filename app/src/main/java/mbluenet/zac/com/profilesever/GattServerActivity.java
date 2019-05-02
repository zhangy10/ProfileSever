/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mbluenet.zac.com.profilesever;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.TextView;

import mbluenet.zac.com.profilesever.ble.GattServer;
import mbluenet.zac.com.profilesever.utils.Log;


public class GattServerActivity extends Activity {


    public final static int LOG_PROFILE = 1;
    public static final int PROFILE_CHANGE = 2;


    private static Log log = Log.getInstance();
    /* Local UI */
    private TextView mLocalTimeView;
    private TextView logView;


    /* Bluetooth API */
//    private BluetoothManager mBluetoothManager;
    private GattServer server;


    public static final String TEST_STR = "first res info"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "123456789"
            + "zac is here so it's ok!"
            ;




    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case LOG_PROFILE:
                    // report to others
                    logView.setText((String) msg.obj);
                    break;
                case PROFILE_CHANGE:
                    int profile = (int) msg.obj;
                    server.notifyRegisteredDevices(profile);
//                    server.notifyRegisteredDevices(TEST_STR + TEST_STR + TEST_STR);
                    updateLocalUi(profile);
                    break;
                default:
                    break;
            }


        }
    };
    /**
     * System time change update
     */
//    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            byte adjustReason;
//            switch (intent.getAction()) {
//                case Intent.ACTION_TIME_CHANGED:
//                    adjustReason = TimeProfile.ADJUST_MANUAL;
//                    break;
//                case Intent.ACTION_TIMEZONE_CHANGED:
//                    adjustReason = TimeProfile.ADJUST_TIMEZONE;
//                    break;
//                default:
//                case Intent.ACTION_TIME_TICK:
//                    adjustReason = TimeProfile.ADJUST_NONE;
//                    break;
//            }
//            long now = System.currentTimeMillis();
//            notifyRegisteredDevices(now, adjustReason);
//            updateLocalUi(now);
//        }
//    };

    private ProfileWatcher profile = new ProfileWatcher(handler, this);
    private ProfileChange changer = new ProfileChange(handler);


    /**
     * Open / close broadcast
     * <p>
     * if turn off the BT, then stop server + broadcast
     */
    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    start();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stop();
                    break;
                default:
                    // Do nothing
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        mLocalTimeView = (TextView) findViewById(R.id.text_time);
        logView = (TextView) findViewById(R.id.log_view);


        /**
         * keep screen alive
         *
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        /**
         * for if BT is close, then close all things
         *
         *
         */
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);


        server = new GattServer(this);

        start();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
        unregisterReceiver(mBluetoothReceiver);
    }

    private void start() {
        profile.start();
        changer.start();

//        server.startAdvertising("Zac AD...".getBytes());
        String ad = TEST_STR.substring(0, 11);
        log.d("Ad length： " + ad.getBytes().length + " ad: " + ad);
        server.startAdvertising(ad.getBytes());
        server.startServer();
        updateLocalUi(0);
    }

    private void stop() {
        server.stopServer();
        server.stopAdvertising();
        profile.stop();
        changer.stop();
    }


    /**
     * Update graphical UI on devices that support it with the current time.
     */
    private void updateLocalUi(int profile) {
        mLocalTimeView.setText(String.valueOf(profile));
    }
}


