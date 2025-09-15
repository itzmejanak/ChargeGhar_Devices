package com.demo.tools;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class HmacCoder {
    /**
     * MAC算法可选以下多种算法
     *
     * <pre>
     * HmacMD5
     * HmacSHA1
     * HmacSHA256
     * HmacSHA384
     * HmacSHA512
     * </pre>
     */
    public static final String TYPE_HMAC_MD5 = "HmacMD5";
    public static final String TYPE_HMAC_SHA1 = "HmacSHA1";
    public static final String TYPE_HMAC_SHA256= "HmacSHA256";
    public static final String TYPE_HMAC_SHA384 = "HmacSHA384";
    public static final String TYPE_HMAC_SHA512 = "HmacSHA512";

    /**
     * HMAC加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, String key, String type) throws Exception {

        SecretKey secretKey = new SecretKeySpec(key.getBytes(), type);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);

        return mac.doFinal(data);

    }

    public static String encrypt (String data, String key, String type) throws Exception {
        byte[] inputData = data.getBytes();

        String result = HmacCoder.byteArrayToHexString(HmacCoder.encrypt(inputData, key, type));

        return result;
    }


    /*byte数组转换为HexString*/
    public static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }


    public static void main(String[] args)throws Exception{
        String data = HmacCoder.encrypt("clientIdL402HS003LdeviceNameL402HS003LproductKey2JpKO3rv551",
                "CVCupjl1k0dYl3gaebaQTRubcPH2xggO", TYPE_HMAC_MD5);

        System.out.println(data);
    }
}
