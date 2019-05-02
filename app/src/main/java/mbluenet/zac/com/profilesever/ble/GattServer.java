package mbluenet.zac.com.profilesever.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import mbluenet.zac.com.profilesever.utils.Log;

public class GattServer {

//extends BaseTask {


    private static Log log = Log.getInstance();
    private BluetoothManager mbManager;
    private BluetoothAdapter mAdapter;
    private Context context;

    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    /* Collection of notification subscribers */
    private Set<BluetoothDevice> mRegisteredDevices = new HashSet<>();


    public GattServer(Context context) {
        this.context = context;
        this.mbManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    /**
     * if Broadcast is start or not
     */
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            log.d("LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            log.d("LE Advertise Failed: " + errorCode);
        }
    };


    /**
     * Sever, Read / write data here
     */
    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {


            if (newState == BluetoothProfile.STATE_CONNECTED) {
                log.d("BluetoothDevice CONNECTED: address: " + device.getAddress() + " name： " + device.getName());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                log.d("BluetoothDevice DISCONNECTED: name: " + device.getName());
                //Remove device from any active subscriptions
                mRegisteredDevices.remove(device);
            } else {

                log.d("unknown op: name: " + device.getName());
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            log.d("onCharacteristicReadRequest: ");

            /**
             * Read data from sever
             *
             *
             * other nodes request to read data
             *
             *
             * sever.sendResponse()
             *
             */
            long now = System.currentTimeMillis();
            if (ServerProfile.RES_INFO.equals(characteristic.getUuid())) {
                log.d("Read resource info");
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        ServerProfile.getExactTime(now));
            } else {
                // Invalid characteristic
                log.d("Invalid Characteristic Read: " + characteristic.getUuid());
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite,
                                                 boolean responseNeeded, int offset, byte[] value) {

            /**
             * write data to sever
             *
             */
            log.d("onCharacteristicWriteRequest uuid: " + characteristic.getUuid());

            if (ServerProfile.RES_INFO.equals(characteristic.getUuid())) {

                byte[] data1 = value;
                if (data1 != null) {
                    log.d("[Get message from client]: " + new String(data1));
                }

                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        "Got message!! Sever...".getBytes());
            } else {
                log.d("Invalid Characteristic write from client: " + characteristic.getUuid());
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);

            }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {

            /**
             *
             *
             * subscribe data changes from sever
             *
             *
             * other nodes request to write data to sever
             *
             */
            if (ServerProfile.RES_NOTICE.equals(descriptor.getUuid())) {

                // add sub nodes to broadcast
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {


                    log.d("Subscribe device to notifications: " + device.getAddress() + " name: " + device.getName() + " need response: " + responseNeeded);
                    mRegisteredDevices.add(device);
                } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
                    log.d("Unsubscribe device from notifications: " + device.getAddress() + " name: " + device.getName());
                    mRegisteredDevices.remove(device);
                }

                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            null);
                }
            } else {
                log.d("Unknown descriptor write request");
                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_FAILURE,
                            0,
                            null);
                }
            }
        }


    };


    /**
     * Begin advertising over Bluetooth that this device is connectable
     * and supports the Current Time Service.
     */
    public void startAdvertising(byte[] advertise) {
        mBluetoothLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser == null) {
            log.d("Failed to create advertiser");
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(new ParcelUuid(ServerProfile.MDL_SERVICE))
                /**
                 * add broadcast data
                 *
                 *
                 * tested: only call once, and no any updates at all !!!
                 */
                .addServiceData(new ParcelUuid(ServerProfile.MDL_SERVICE), advertise)
                .build();

        mBluetoothLeAdvertiser
                .startAdvertising(settings, data, mAdvertiseCallback);
    }


    /**
     * Stop Bluetooth advertisements.
     */
    public void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    /**
     * Start Sever.....
     */
    public void startServer() {
        mBluetoothGattServer = mbManager.openGattServer(context, mGattServerCallback);
        if (mBluetoothGattServer == null) {
            log.d("Unable to create GATT server");
            return;
        }

        mBluetoothGattServer.addService(ServerProfile.createService());
    }

    /**
     * Shut down the GATT server.
     */
    public void stopServer() {
        if (mBluetoothGattServer == null) return;

        mBluetoothGattServer.close();
    }

    public void notifyRegisteredDevices(int profile) {
        notifyRegisteredDevices(String.valueOf(profile).getBytes());

    }

    public void notifyRegisteredDevices(String profile) {
        notifyRegisteredDevices(profile.getBytes());
    }


    /**
     * Broadcast Action here
     */
    public void notifyRegisteredDevices(byte[] data) {
        if (mRegisteredDevices.isEmpty()) {
//            log.d("No subscribers registered");
            return;
        }

        log.d("Sending update to " + mRegisteredDevices.size() + " subscribers" + " data length: " + data.length);
        for (BluetoothDevice device : mRegisteredDevices) {
            BluetoothGattCharacteristic resource = mBluetoothGattServer
                    .getService(ServerProfile.MDL_SERVICE)
                    .getCharacteristic(ServerProfile.RES_INFO);

            // update data
            // byte data here
            resource.setValue(data);


            /**
             * send to many nodes
             *
             */
            mBluetoothGattServer.notifyCharacteristicChanged(device, resource, false);
        }
    }
}
