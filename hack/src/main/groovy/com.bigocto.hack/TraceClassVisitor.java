package com.bigocto.hack;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.io.File;

import static org.objectweb.asm.Opcodes.ACC_FINAL;

/**
 * Created by zhangyu
 * on 2017/3/28.
 */

public class TraceClassVisitor extends ClassVisitor {

    private File file = null;
    public TraceClassVisitor(File file, String entry, boolean isJar, int api, ClassWriter cv) {
        super(api, cv);
        this.file = file;
    }

    @Override
    public void visit(int i, int i1, String s, String s1, String s2, String[] strings) {
        if (cv != null) {
            super.visit(i, i1 & (~ACC_FINAL), s, s1, s2, strings);
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = null;

//        if(name.equals("onCreate")){
            System.out.println(file.getName() + "Method name : " + name);
            mv = cv.visitMethod(access, name, desc, signature, exceptions);
            return new TraceMethodVisitor(name, mv);
//        }


//        if (cv != null){
//            mv = cv.visitMethod(access, name, desc, signature, exceptions);
//        }
//
//        return mv;
    }

}
