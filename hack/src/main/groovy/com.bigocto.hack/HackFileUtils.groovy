package com.bigocto.hack;
/**
 * Created by zhangyu
 * on 2017/3/21.
 */
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

    public static boolean isEmpty(String text) {
        return text == null || text == '' || text.trim() == ''
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
        String p = HackConstans.HACK_CLASS.get(file.name)
        println("isExistFile : " + p)
        return p != null
    }

    public static boolean isExistDirectory(File file){
        String p = HackConstans.HACk_DIRECTORY.get(file.parentFile.path)
        println("isExistDirectory : " + p)
        return p != null
    }
}
