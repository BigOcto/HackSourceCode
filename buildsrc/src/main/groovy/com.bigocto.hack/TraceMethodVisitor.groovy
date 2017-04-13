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
    MethodVisitor visitor
    public TraceMethodVisitor(String methodName, MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
        this.mMethodName = methodName;
        this.visitor = mv
    }

    @Override
    public void visitCode() {
        //add start
//        this.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//        this.visitLdcInsn("========start=========");
//        this.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

//        this.visitLdcInsn("zhangyu");
//        this.visitLdcInsn("hello world");
//        this.visitMethodInsn(INVOKEVIRTUAL, "android/util/Log", "v", "(Ljava/lang/String;Ljava/lang/String;)I", false);

        this.visitMethodInsn(INVOKESTATIC, "com/bigocto/hacksourcecode/JavaInjectTest", "test1", "()V", false);


        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
            this.visitMethodInsn(INVOKESTATIC, "com/bigocto/hacksourcecode/JavaInjectTest", "test2", "()V", false);
        }

        super.visitInsn(opcode);
    }
}
