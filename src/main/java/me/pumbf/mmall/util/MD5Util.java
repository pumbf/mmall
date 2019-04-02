package me.pumbf.mmall.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class MD5Util {

    /**
     * 将origin字符串进行md5序列化
     * @param origin    待序列化的字符串
     * @param charsetName   待序列化的字符串的编码集
     * @return  序列化后16进制字符串
     */
    public static String encode(String origin, String charsetName) {
        String resultString = null;

        resultString = new String(origin);
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            if (charsetName == null || charsetName.equals(""))
                resultString = byteArrayToHexString(md5.digest(origin.getBytes()));
            else
                resultString = byteArrayToHexString(md5.digest(origin.getBytes(charsetName)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultString.toUpperCase();
    }

    /**
     * 将origin字符串进行md5序列化
     * @param origin    待序列化的字符串
     * @return  序列化后16进制字符串
     */
    public static String encode(String origin) {
        return encode(origin, "UTF-8");
    }

    /**
     * 将byte数组转换成16进制字符显示
     * @param bytes 待转换的byte数组
     * @return  转换好的16进制字符串
     */
    private static String byteArrayToHexString(byte[] bytes) {
        StringBuffer resultSB;
        resultSB = new StringBuffer();
        for(byte b : bytes) {
            resultSB.append(byteToString(b));
        }
        return resultSB.toString();
    }

    /**
     * 将单个byte转换成2个16进制字符显示
     * @param b byte
     * @return  2个16进制字符组成的字符串
     */
    private static String byteToString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    private static  final String hexDigits[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
}
