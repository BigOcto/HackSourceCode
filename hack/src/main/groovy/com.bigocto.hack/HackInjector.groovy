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
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    def cacheFile = new File(file.parent, file.name + ".cache");

                    fis = new FileInputStream(file)
                    fos = new FileOutputStream(cacheFile)
                    println "injectFile: ${file.path}"
                    byte[] bytes = hackClass(file, null, false, fis);
                    fos.write(bytes)

                    if (file.exists()) {
                        file.delete()
                    }
                    cacheFile.renameTo(file)
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
            }
        }
    }

    private static byte[] hackClass(File file, String entry, boolean isJar, InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ClassVisitor cv = new TraceClassVisitor(file, entry, isJar, Opcodes.ASM4, cw)
        cr.accept(cv, 0)
        return cw.toByteArray()
    }
}
