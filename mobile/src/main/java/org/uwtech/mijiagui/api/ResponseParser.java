package org.uwtech.mijiagui.api;

import java.util.Arrays;

public class ResponseParser {

    public ReadResponse parse(byte[] bytes) throws Exception {

        if (bytes[0] != (byte) 0x55 || bytes[1] != (byte) 0xAA) {
            throw new Exception("Message header is incorrect");
        }

        if (hasType(bytes, (byte) 0x23, (byte) 0x01)) {
            return parseRead(bytes);
        }
        throw new Exception("Unknown type");
    }

    private ReadResponse parseRead(byte[] bytes) {
        int length = (bytes[2] & 0xFF) - 2;
        int from = bytes[5] & 0xFF;
        byte[] data = Arrays.copyOfRange(bytes, 6, 6 + length);
        return new ReadResponse(from, data);
    }

    private boolean hasType(byte[] bytes, byte first, byte second) {
        return bytes[3] == first && bytes[4] == second;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}

