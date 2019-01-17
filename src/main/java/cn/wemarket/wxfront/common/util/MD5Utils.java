package cn.wemarket.wxfront.common.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Utils {
    public static String encode(String plainText){
        return DigestUtils.md5Hex(plainText);
    }
}
