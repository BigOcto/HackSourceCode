package com.bigocto.hack

import com.bigocto.hack.bean.HackClassInfo;

/**
 * Created by zhangyu
 * on 2017/3/21.
 */
import groovy.json.JsonSlurper

public class HackFileUtils {
    public static boolean saveJson(String json, String fileName, boolean override) {
        def pending = new File(fileName)
        if (pending.exists() && pending.isFile()) {
            if (override) {
                println "Old file $pending.absolutePath removed."
                pending.delete()
            } else {
                println "File $pending.absolutePath exists."
                return false
            }
        }

        pending << json
        println "Save to $pending.absolutePath"
        return true
    }

    public static List<HackClassInfo> getJson(String filePath) {
        def inputFile = new File(filePath)
        def jsonList = new JsonSlurper().parseText(inputFile.text)
        List<HackClassInfo> hackClassInfoList = new ArrayList<HackClassInfo>()

        jsonList.each { ob ->
            HackClassInfo info = new HackClassInfo()
            info.setPackageName(ob.PACKAGE)
            info.setClassName(ob.NAME)
            info.setMethodList(ob.METHODS)
            hackClassInfoList.add(info)
        }
        for (HackClassInfo info : hackClassInfoList){
            println(info.packageName + "." + info.className)
            println(info.methodList.toString())
        }
        return hackClassInfoList
    }

    public static boolean isEmpty(String text) {
        return text == null || text == '' || text.trim() == ''
    }

    public static Map<String, String> list2Map(List<String> list) {
        Map<String, String> map = new HashMap<>()
        list.each { str ->
            map.put(str, str)
        }
        return map
    }

    public static String joinPath(String... sep) {
        if (sep.length == 0) {
            return "";
        }
        if (sep.length == 1) {
            return sep[0];
        }

        return new File(sep[0], joinPath(Arrays.copyOfRange(sep, 1, sep.length))).getPath();
    }

    public static String getBuildCacheDir(String buildDirPath) {
        def buildCacheDir = new File(buildDirPath, HackConstans.HACK_BUILD_CACHE_DIR)
        if (!buildCacheDir.exists() || !buildCacheDir.isDirectory()) {
            buildCacheDir.mkdirs()
        }
        return buildCacheDir.absolutePath
    }

    public static boolean isExistFile(File file) {
        String p = null;
        if (HackConstans.HACK_CLASS.size() > 0) {
            p = HackConstans.HACK_CLASS.get(file.name)
        }
        println("isExistFile : " + p)
        return p != null
    }

    public static boolean isExistDirectory(File file) {
        String p = HackConstans.HACk_DIRECTORY.get(file.parentFile.path)
        println("isExistDirectory : " + p)
        return p != null
    }

    public static List<String> getClassMethods(String className) {
        List<String> methods = HackConstans.HACK_CLASS_AND_METHOD.get(className)
        if (methods.size() != 0) {
            return methods
        } else {
            return null
        }
    }
}
