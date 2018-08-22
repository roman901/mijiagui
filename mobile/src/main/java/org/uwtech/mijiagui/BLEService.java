package org.uwtech.mijiagui;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.uwtech.mijiagui.api.MijiaAPI;

import java.util.List;

import static org.uwtech.mijiagui.api.ResponseParser.hexStringToByteArray;


public class BLEService extends Service {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    BluetoothGatt bluetoothGatt;
    String mac;

    MijiaAPI api;

    public BLEService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MijiaGUI", "BLEService started");

        mac = intent.getStringExtra("mac");

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
        btScanner.startScan(leScanCallback);

        return super.onStartCommand(intent, flags, startId);
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device.getAddress().equals(mac)) {
                bluetoothGatt = device.connectGatt(getApplicationContext(), false, btleGattCallback);
            }
        }
    };

    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            switch (newState) {
                case 0:
                    Log.d("MijiaGUI", "BLEService disconnected");
                    break;
                case 2:
                    Log.d("MijiaGUI", "BLEService connected");
                    bluetoothGatt.discoverServices();
                    break;
                default:
                    Log.d("MijiaGUI", "BLEService unknown state");
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService gattService : gatt.getServices()) {
                    for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                        if (characteristic.getUuid().toString().equals(MijiaAPI.READ_CHARACTERISTIC_UUID)) {
                            Log.i("MijiaGUI", "onServicesDiscovered: found Mijia Scooter");
                            api = new MijiaAPI(gatt, characteristic);
                            gatt.setCharacteristicNotification(characteristic, true);
                            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }
                        }
                    }
                }

            } else {
                Log.w("MijiaGUI", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d("onCharacteristicRead", bytesToHex(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("onCharacteristicWrite", bytesToHex(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d("onCharacteristicChanged", bytesToHex(characteristic.getValue()));
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothGatt.disconnect();
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
