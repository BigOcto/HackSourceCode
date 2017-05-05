package com.bigocto.hack.visitor;

import org.apache.tools.ant.util.StringUtils;
import org.codehaus.groovy.util.StringUtil;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File
import java.lang.reflect.Field;

import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Created by zhangyu
 * on 2017/3/28.
 */

public class HackMethodVisitor extends MethodVisitor {

    private String mMethodName = "";
    MethodVisitor visitor
    public HackMethodVisitor(String methodName, MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
        this.mMethodName = methodName;
        this.visitor = mv
    }

    @Override
    public void visitCode() {
        println(mMethodName + ': visit code' )

        //add trace start tag
//        this.visitLdcInsn(mMethodName)
//        this.visitMethodInsn(INVOKESTATIC,"android/os/Trace","beginSection","(Ljava/lang/String;)V",false)
        //add time collector start
        visitLdcInsn(mMethodName)
        visitMethodInsn(INVOKESTATIC, "com/bigocto/trace/TraceTagUtils", "bigOctoTraceBegin", "(Ljava/lang/String;)V", false);
        super.visitCode()
    }


    @Override
    public void visitInsn(int opcode) {

        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
            println(mMethodName + ': visit Insn' )

            //add time collector end
            visitLdcInsn(mMethodName)
            visitMethodInsn(INVOKESTATIC, "com/bigocto/trace/TraceTagUtils", "bigOctoTraceEnd", "(Ljava/lang/String;)V", false);
            //Add trace end tag
//            this.visitMethodInsn(INVOKESTATIC,"android/os/Trace","endSection","()V",false)
        }

        super.visitInsn(opcode);
    }


    @Override
    void visitMethodInsn(int i, String s, String s1, String s2, boolean b) {
        super.visitMethodInsn(i, s, s1, s2, b)
    }

    @Override
    void visitLdcInsn(Object o) {
        super.visitLdcInsn(o)
    }
}
