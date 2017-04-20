package com.bigocto.trace;


import android.util.Log;

import java.util.HashMap;

/**
 * Created by zhangyu
 * on 2017/4/13.
 * Trace tag helper class
 */

public class TraceTagUtils {

    private static HashMap<String, Long> methodTimeMap = new HashMap<>();

    public static void bigOctoTraceBegin(String method) {
        long time = System.currentTimeMillis();
        methodTimeMap.put(method, time);
        Log.e("zhangyu", method + " st time: " + time);
    }

    public static void bigOctoTraceEnd(String method) {
        long startTime = methodTimeMap.get(method);
        long endTime = System.currentTimeMillis();
        Log.e("zhangyu", method + " end time: " + endTime);
        Log.e("zhangyu", method + " total time: " + (endTime - startTime));
    }
}
