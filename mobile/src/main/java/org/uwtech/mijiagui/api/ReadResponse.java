package org.uwtech.mijiagui.api;

public class ReadResponse extends Message {

    private int from;

    public ReadResponse(int from, byte[] bytes) {
        this.from = from;
        this.bytes = bytes;
    }

    public int getFrom() {
        return from;
    }
}
