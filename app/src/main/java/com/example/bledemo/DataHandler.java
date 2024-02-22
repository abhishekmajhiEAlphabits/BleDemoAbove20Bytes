package com.example.bledemo;

import android.widget.Toast;

public class DataHandler {

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        if (len % 2 == 0) {
//            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }
            return data;
        } else {
            for (int i = 0; i < len; i += 2) {
//                data[i / 2] = (byte) (Character.digit(s.charAt(i), 16));
            }
        }
        return null;
    }
}
