package com.tzuhao.zzhcoolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * HttpUtil.java是LZCoolWeather的网络访问的工具类。
 *
 * @author Learzhu
 * @version 2.2.5 2017/4/2 17:29
 * @update UserName 2017/4/2 17:29
 * @updateDes
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
