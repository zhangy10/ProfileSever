package mbluenet.zac.com.profilesever.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

import mbluenet.zac.com.profilesever.utils.Log;

public class ServerProfile {

    private static Log log = Log.getInstance();

    public static UUID MDL_SERVICE = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");
    public static UUID RES_INFO = UUID.fromString("00002c2d-0000-1000-8000-00805f9b34fb");
//    public static UUID NETWORK_INFO = UUID.fromString("00002b0f-0000-1000-8000-00805f9b34fb");
    public static UUID RES_NOTICE = UUID.fromString("00002c0f-0000-1000-8000-00805f9b34fb");


    private static int DATA1 = 1;

    public static BluetoothGattService createService() {
        BluetoothGattService service = new BluetoothGattService(MDL_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic resource = new BluetoothGattCharacteristic(RES_INFO,
                //Read only characteristic, supports notifications
                BluetoothGattCharacteristic.PROPERTY_READ
                        | BluetoothGattCharacteristic.PROPERTY_NOTIFY
                | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);

        BluetoothGattDescriptor configDescriptor = new BluetoothGattDescriptor(RES_NOTICE,
                //Read/write descriptor
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);

        resource.addDescriptor(configDescriptor);
        service.addCharacteristic(resource);

//        BluetoothGattCharacteristic network = new BluetoothGattCharacteristic(NETWORK_INFO,
//                BluetoothGattCharacteristic.PROPERTY_READ |BluetoothGattCharacteristic.PROPERTY_WRITE,
//                BluetoothGattCharacteristic.PERMISSION_READ |BluetoothGattDescriptor.PERMISSION_WRITE);
//
//        service.addCharacteristic(network);

        log.d("createService service....");
        return service;
    }


    /**
     * Construct the field values for a Current Time characteristic
     * from the given epoch timestamp and adjustment reason.
     */
    public static byte[] getExactTime(long timestamp) {
        DATA1++;
        return new String(String.valueOf(DATA1) + " zac[1]!!!").getBytes();
    }
}

