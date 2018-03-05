package com.liking.android.bleopendoordemo.data;

import com.liking.android.bleopendoordemo.utils.NumberUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2018/3/5
 * Created by sanfen
 *
 * @version 1.0.0
 */

public class DataPack {

    public static final byte start = (byte) 0xAA;
    public static final byte end = 0x55;
    public static List<Byte> mByteBuffer = new ArrayList<>();

    public static byte[] pack(String content) {
        byte[] bytes = content.getBytes();
        byte cmd = CmdConstant.CMD_ENTER_DOOR;
        byte len = (byte) bytes.length;
        byte verify = (byte) (cmd ^ len);
        for (byte b : bytes) {
            verify ^= b;
        }
        byte[] ret = new byte[1 + 1 + 1 + bytes.length + 1 + 1];
        ret[0] = start;
        ret[1] = cmd;
        ret[2] = len;
        for (int i = 0; i < bytes.length; i++) {
            ret[3 + i] = bytes[i];
        }
        ret[ret.length - 2] = verify;
        ret[ret.length - 1] = end;
        return ret;
    }

    public static String unpack(byte[] bytes) {
        for (byte b : bytes){
            mByteBuffer.add(b);
        }
        if (mByteBuffer.size() < 5) {
            return "";
        }
        if (mByteBuffer.get(0) == start) {
            int len = mByteBuffer.get(2);
            if (mByteBuffer.size() < (len + 5)) {
                return "";
            }
            List<Byte> subList = mByteBuffer.subList(3, 3 + len);
            byte[] b = new byte[subList.size()];
            for (int i = 0; i < b.length; i ++) {
                b[i] = subList.get(i);
            }
            String ret = new String(b);
            mByteBuffer = mByteBuffer.subList(len + 5, mByteBuffer.size());
            return ret;
        }
        return "";
    }
}
