package com.bigocto.hack

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * Created by zhangyu
 * on 2017/3/21.
 */
public class HackInjector {

    public static boolean checkInjection(File file, Collection<String> modules) {
        return (file.absolutePath.contains("intermediates" + File.separator + "exploded-aar" + File.separator)
                && isProjectModuleJar(file.absolutePath, modules))
    }

    private static boolean isProjectModuleJar(String path, Collection<String> modules) {
        for (String module : modules) {
            if (path.contains(module)) {
                return true
            }
        }
        return false
    }

    public static void injectFile(File file) {
        if (file.path.endsWith(".class")) {
            if (HackFileUtils.isExistFile(file)) {
                List<String> list = HackFileUtils.getClassMethods(file.name)
                if (list != null && list.size() > 0){
                    HashMap<String ,String> hashMap = HackFileUtils.list2Map(list)
                    inject(file, hashMap)
                }else {
                    inject(file, null)
                }

            }
        }
    }

    public static void inject(File file, HashMap<String, String> method){
        FileInputStream fis = null;
        FileOutputStream fos = null;
        def cacheFile = null
        try {
            cacheFile= new File(file.parent, file.name + ".cache");

            fis = new FileInputStream(file)
            fos = new FileOutputStream(cacheFile)
            println "injectFile: ${file.path}"
            byte[] bytes = hackClass(file, null, false, fis, method);
            fos.write(bytes)

        } catch (Exception e) {
            e.printStackTrace()
            println("Class injectFile error" + e.printStackTrace())
            println("Class injectFile error path :" + $ { file.path })
        } finally {
            if (fis != null) {
                fis.close()
            }
            if (fos != null) {
                fos.close()
            }
        }

        if (file.exists()) {
            file.delete()
        }
        if (cacheFile != null){
            cacheFile.renameTo(file)
        }
    }

    private static byte[] hackClass(File file, String entry, boolean isJar, InputStream inputStream) {
       return hackClass(file,entry,isJar,inputStream,null)
    }

    private static byte[] hackClass(File file, String entry, boolean isJar, InputStream inputStream, HashMap<String, String> methodName) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ClassVisitor cv
        if (methodName == null || methodName.size() == 0){
            cv = new HackClassVisitor(file, entry, isJar, Opcodes.ASM4, cw)
        }else {
            cv = new HackClassVisitor(file, entry, isJar, Opcodes.ASM4, cw, methodName)
        }
        cr.accept(cv, 0)
        return cw.toByteArray()
    }

}
