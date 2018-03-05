package com.liking.android.bleopendoordemo.utils;

public class NumberUtil {

    public static byte[] get4ByteArray(int len) {
        byte[] head = new byte[4];
        head[0] = (byte) ((len >> 24) & 0xFF);
        head[1] = (byte) ((len >> 16) & 0xFF);
        head[2] = (byte) ((len >> 8) & 0xFF);
        head[3] = (byte) (len & 0xFF);
        return head;
    }

    public static byte[] get2ByteArray(short len) {
        byte[] head = new byte[2];
        head[0] = (byte) ((len >> 8) & 0xFF);
        head[1] = (byte) (len & 0xFF);
        return head;
    }

    public static short getShortArray(byte[] b){
        if(b == null) return 0;
        return (short) (b[1] & 0xff | (b[0] & 0xff)<< 8);
    }

    public static int getIntArray(byte[] b) {
        if(b == null) return 0;
        return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;
    }


    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexFun(byte[] bytes) {
        // 一个byte为8位，可用两个十六进制位标识
        char[] buf = new char[bytes.length * 2];
        int a = 0;
        int index = 0;
        for(byte b : bytes) { // 使用除与取余进行转换
            if(b < 0) {
                a = 256 + b;
            } else {
                a = b;
            }

            buf[index++] = HEX_CHAR[a / 16];
            buf[index++] = HEX_CHAR[a % 16];
        }
        return new String(buf);
    }


    /**
     * 将16进制字符串转换为byte[]
     *
     * @param str
     * @return
     */
    public static byte[] toBytes(String str) {
        if(str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for(int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

}

