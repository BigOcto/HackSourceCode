package com.bigocto.hack;
/**
 * Created by zhangyu
 * on 2017/3/21.
 */
public class HackConstans {
    public static final String HACK_BUILD_CACHE_DIR = "hack"

    public static final def HACK_CLASS = [
//            "TransferApplication.class" : "TransferApplication.class",
//            "BaseApplication.class" : "BaseApplication.class",
            "MainActivity.class" : "MainActivity.class",
//            "MainApplication.class" : "MainApplication.class",
    ]

    public static def HACK_CLASS_AND_METHOD = [
            "MainActivity.class":["<init>","onCreate", "onPause"],
            "JavaInjectTest.class":["test1", "test2"],
    ]

    public static final def HACk_DIRECTORY = [
            (BuildDir + "com/bigocto/hacksourcecode") : (BuildDir + "com/bigocto/hacksourcecode")
    ]

    public static final String BuildDir ="/Users/zhangyu/OpenProjects/HackSourceCode/app/build/intermediates/classes/debug/";
}
