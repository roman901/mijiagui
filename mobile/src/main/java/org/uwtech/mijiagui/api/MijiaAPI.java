package org.uwtech.mijiagui.api;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.util.Log;

import org.uwtech.mijiagui.BLEService;
import org.uwtech.mijiagui.FloatingWindow;
import org.uwtech.mijiagui.MainActivity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MijiaAPI {
    public static final String SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String WRITE_CHARACTERISTIC_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String READ_CHARACTERISTIC_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String NOTIFY_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    private BLEService bleService;
    private BluetoothGatt connection;
    private BluetoothGattCharacteristic wCharacteristic;

    private ArrayDeque<Message> queue = new ArrayDeque<>();

    private int loop = 0;

    public MijiaAPI(BLEService bleService, BluetoothGatt connection) {
        this.bleService = bleService;
        this.connection = connection;
        BluetoothGattService service = connection.getService(UUID.fromString(SERVICE_UUID));
        wCharacteristic = service.getCharacteristic(UUID.fromString(WRITE_CHARACTERISTIC_UUID));
        wCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void requestValue(Command command) {
        List<Integer> args = new ArrayList<>();
        args.add(command.size * 2);
        ReadRequest message = new ReadRequest(command.command, args);

        wCharacteristic.setValue(message.getBytes());
        Log.d("MijiaGUI", String.valueOf(connection.writeCharacteristic(wCharacteristic)));
    }

    public void gotDescriptorWrite() {
        requestValue(Command.SPEED); // First value in loop
    }

    public void gotCharacteristicChanged(byte[] data) {
        ResponseParser parser = new ResponseParser();
        try {
            ReadResponse response = parser.parse(data);

            Intent intent = new Intent(MainActivity.BLE_BROADCAST_MSG);
            intent.putExtra("from", response.getFrom());
            intent.putExtra("bytes", response.getBytes());
            bleService.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        switch (this.loop) {
            case 0:
                requestValue(Command.SPEED);
                break;

            case 1:
                requestValue(Command.CURRENT);
                break;

            case 2:
                requestValue(Command.BATTERY);
                break;

            case 3:
                requestValue(Command.REMAINING_MILEAGE);
                break;
            default:
                this.loop = 0;
                requestValue(Command.SPEED);
        }
        this.loop++;
    }

    public enum Command {
        SPEED(181, 1),
        BATTERY(180, 1),
        REMAINING_MILEAGE(37, 1),
        CURRENT(80, 1);

        public int command;
        public int size;
        Command(int command, int size) {
            this.command = command;
            this.size = size;
        }
    }
}
