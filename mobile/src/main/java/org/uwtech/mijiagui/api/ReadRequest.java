package org.uwtech.mijiagui.api;

import java.util.List;

public class ReadRequest extends Message {

    public ReadRequest(int command, List<Integer> params) {
        bytes = new byte[params.size() + 8];

        bytes[0] = (byte) 0x55;
        bytes[1] = (byte) 0xAA;
        bytes[2] = (byte) (params.size() + 2);
        bytes[3] = (byte) 0x20;
        bytes[4] = (byte) 0x01;
        bytes[5] = (byte) command;

        for (int i = 0; i < params.size(); i++) {
            bytes[6 + i] = params.get(i).byteValue();
        }

        int sum = 0;
        for (int i = 2; i <= 5+params.size(); i++) {
            sum += bytes[i] & 0xFF;
        }

        int v = (sum ^ 0xFFFF) & 0xFFFF;

        bytes[6 + params.size()] = (byte) (v & 0xFF);
        bytes[7 + params.size()] = (byte) (v >> 8);
    }

    public ReadRequest(byte[] bytes) {
        super(bytes);
    }
}
