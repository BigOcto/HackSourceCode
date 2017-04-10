package com.bigocto.hack;

import org.apache.tools.ant.util.StringUtils;
import org.codehaus.groovy.util.StringUtil;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;

import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Created by zhangyu
 * on 2017/3/28.
 */

public class TraceMethodVisitor extends MethodVisitor {

    private String mMethodName = "";
    public TraceMethodVisitor(String methodName, MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
        this.mMethodName = methodName;
    }

    @Override
    public void visitCode() {
        //add start
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("========start=========");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

//        if (!mMethodName.equals("")){
//            mv.visitLdcInsn(mMethodName);
//        }
//        mv.visitMethodInsn(INVOKESTATIC, "android/os/Trace", "beginSection", "(Ljava/lang/String;)V", false);

        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("========end=========");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

//            mv.visitMethodInsn(INVOKESTATIC, "android/os/Trace", "endSection", "()V", false);
        }

        super.visitInsn(opcode);
    }
}
