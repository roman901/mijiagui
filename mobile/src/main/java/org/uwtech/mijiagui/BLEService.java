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
import java.util.UUID;

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
                bluetoothGatt = device.connectGatt(getApplicationContext(), false, btleGattCallback, BluetoothDevice.TRANSPORT_LE);
                bluetoothGatt.requestMtu(512);
            }
        }
    };

    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            switch (newState) {
                case 0:
                    Log.d("MijiaGUI", "BLEService disconnected from device");
                    System.exit(0);
                    break;
                case 2:
                    Log.d("MijiaGUI", "BLEService connected to device");
                    bluetoothGatt.discoverServices();
                    break;
                default:
                    Log.d("MijiaGUI", "BLEService entered unknown state");
                    System.exit(0);
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("MijiaGUI", "BLEService got services from device");
                BluetoothGattService service = gatt.getService(UUID.fromString(MijiaAPI.SERVICE_UUID));

                if (service !=null) {
                    Log.i("MijiaGUI", "BLEService found Mijia Scooter");
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(MijiaAPI.READ_CHARACTERISTIC_UUID));
                    gatt.setCharacteristicNotification(characteristic, true);

                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(MijiaAPI.NOTIFY_DESCRIPTOR_UUID));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    api = new MijiaAPI(BLEService.this, gatt);
                } else {
                    Log.e("MijiaGUI", "BLEService not found Mijia Scooter");
                    bluetoothGatt.disconnect();
                }
            } else {
                Log.w("MijiaGUI", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            api.gotCharacteristicChanged(characteristic.getValue());
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            // Notifications now works
            api.gotDescriptorWrite();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            System.exit(0);
        }
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
