package com.bigocto.hack.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.bigocto.hack.HackInjector
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * Created by zhangyu 
 * on 2017/4/10.
 *
 * Using gradle transform api to hack class
 */

class TransformImpl extends Transform {

    private final Project project
    public TransformImpl(Project project) {
        this.project = project
    }
    @Override
    String getName() {
        return "TransformImpl"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)

        /**
         * 遍历输入文件
         */
        inputs.each { TransformInput input ->
            /**
             * 遍历目录
             */
            input.directoryInputs.each { DirectoryInput directoryInput ->
                project.logger.error "Dire file path${directoryInput.file.absolutePath}}"

                /**
                 * 获得产物的目录
                 */
                String buildTypes = directoryInput.file.name
                String productFlavors = directoryInput.file.parentFile.name
                //这里进行我们的处理
                def dir = new File(directoryInput.file.absolutePath)
                dir.eachFileRecurse(FileType.FILES) {file ->
                    HackInjector.injectFile(file)
                }

                /**
                 * After hack, destination output path
                 */
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY);
                FileUtils.copyDirectory(directoryInput.file, dest);
            }

            /**
             * 遍历jar
             */
            input.jarInputs.each { JarInput jarInput ->
                project.logger.error "Jar path ${jarInput.file.absolutePath}"

                String destName = jarInput.name;
                /**
                 * 重名名输出文件,因为可能同名,会覆盖
                 */
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath);
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4);
                }
                /**
                 * 获得输出文件
                 */
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR);

                //处理jar进行字节码注入处理TODO

                FileUtils.copyFile(jarInput.file, dest);
            }
        }
    }

    /**
     * traversing dir
     * @param file
     */
    private void traverseDir(File file){
        if (file.isDirectory()){
            file.each {File f ->
                traverseDir(f)
            }
        }else {
            project.logger.debug("Transform file ${file.absolutePath}")
            HackInjector.injectFile(file)
        }
    }


}