package org.uwtech.mijiagui.api;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public class MijiaAPI {
    public static final String WRITE_CHARACTERISTIC_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String READ_CHARACTERISTIC_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    private BluetoothGatt connection;
    private BluetoothGattCharacteristic characteristic;
    public MijiaAPI(BluetoothGatt connection, BluetoothGattCharacteristic characteristic) {
        this.connection = connection;
        this.characteristic = characteristic;
    }

    public byte[] dumpMemory() {
        return null;
    }

    public boolean sendMessage(byte[] bytes) {

        //connection.setCharacteristicNotification(new BluetoothGattCharacteristic(READ_CHARACTERISTIC_UUID));
        return false;
    }
}
