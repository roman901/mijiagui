package org.uwtech.mijiagui.api;

public class Message {
    byte[] bytes;

    public Message() {}

    public Message(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
