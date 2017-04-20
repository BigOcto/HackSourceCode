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
        //add trace start tag
        visitor.visitLdcInsn(mMethodName)
        visitor.visitMethodInsn(INVOKESTATIC,"android/os/Trace","beginSection","(Ljava/lang/String;)V",false)
        //add time collector start
        visitor.visitLdcInsn(mMethodName)
        visitor.visitMethodInsn(INVOKESTATIC, "com/bigocto/trace/TraceTagUtils", "bigOctoTraceBegin", "(Ljava/lang/String;)V", false);
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
            //add time collector end
            visitor.visitLdcInsn(mMethodName)
            visitor.visitMethodInsn(INVOKESTATIC, "com/bigocto/trace/TraceTagUtils", "bigOctoTraceEnd", "(Ljava/lang/String;)V", false);
            //Add trace end tag
            visitor.visitMethodInsn(INVOKESTATIC,"android/os/Trace","endSection","()V",false)
        }

        super.visitInsn(opcode);
    }
}
