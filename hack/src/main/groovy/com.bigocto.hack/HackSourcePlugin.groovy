package com.bigocto.hack

import groovy.io.FileType
import groovy.json.JsonBuilder
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils
import proguard.gradle.ProGuardTask

/**
 * Created by zhangyu
 * on 2017/3/19.
 */
class HackSourcePlugin implements Plugin<Project> {
    static final String PLUGIN_NAME = "helloPlugin"

    Project project
    PluginExtension extension
    def argsFilePath = null

    //make jars
    JavaCompile compileJavaSrc
    Jar jarLib
    ProGuardTask proguardLib
    Copy copyLib

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = project.extensions.create(PLUGIN_NAME, PluginExtension)

        project.afterEvaluate {
            project.android.applicationVariants.each { variant ->
                def gradleIsLowVersion = extension.gradleLowVersion
                def gradleIsHighVersion = extension.gradleHighVersion
                argsFilePath = extension.injectSpecifyFile
                println "argsFilePath path : ${argsFilePath}"

                def isLowerVersion = false
                def isHighVersion = false

                if (gradleIsLowVersion) {
                    isLowerVersion = true
                }
                if (gradleIsHighVersion) {
                    isHighVersion = true
                }

                project.rootProject.buildscript.configurations.classpath.resolvedConfiguration.firstLevelModuleDependencies.each {
                    if (it.moduleGroup == "com.android.tools.build" && it.moduleName == "gradle") {

                        println "it.moduleVersion${it.moduleVersion}"

                        if (!it.moduleVersion.startsWith("1.5")
                                && !it.moduleVersion.startsWith("2")
                                && !it.moduleVersion.startsWith("3")) {
                            isLowerVersion = true
                            return false
                        }
                        if (it.moduleVersion.startsWith("3")) {
                            isHighVersion = true
                            return false
                        }
                    }
                }

                def classesProcessTask
                def preDexTask
                def multiDexListTask
                boolean multiDexEnabled = variant.apkVariantData.variantConfiguration.isMultiDexEnabled()

                if (isLowerVersion) {
                    if (multiDexEnabled) {
                        classesProcessTask = project.tasks.findByName("packageAll${variant.name.capitalize()}ClassesForMultiDex")
                        multiDexListTask = project.tasks.findByName("create${variant.name.capitalize()}MainDexClassList")
                    } else {
                        classesProcessTask = project.tasks.findByName("dex${variant.name.capitalize()}")
                        preDexTask = project.tasks.findByName("preDex${variant.name.capitalize()}")
                    }
                } else if (isHighVersion) {
                    String manifest_path = project.android.sourceSets.main.manifest.srcFile.path
                    if (getMinSdkVersion(variant.mergedFlavor, manifest_path) < 21 && multiDexEnabled) {
                        classesProcessTask = project.tasks.findByName("transformClassesWithJarMergingFor${variant.name.capitalize()}")
                        multiDexListTask = project.tasks.findByName("transformClassesWithMultidexlistFor${variant.name.capitalize()}")
                    } else {

                        if (variant.name.capitalize().equals("debug")) {
                            classesProcessTask = project.tasks.findByName("transformClassesWithDexBuilderFor${variant.name.capitalize()}")
                        } else {
                            classesProcessTask = project.tasks.findByName("transformClassesWithPreDexFor${variant.name.capitalize()}")
                        }
                    }
                } else {
                    String manifest_path = project.android.sourceSets.main.manifest.srcFile.path
                    if (getMinSdkVersion(variant.mergedFlavor, manifest_path) < 21 && multiDexEnabled) {
                        classesProcessTask = project.tasks.findByName("transformClassesWithJarMergingFor${variant.name.capitalize()}")
                        multiDexListTask = project.tasks.findByName("transformClassesWithMultidexlistFor${variant.name.capitalize()}")
                    } else {
                        classesProcessTask = project.tasks.findByName("transformClassesWithDexFor${variant.name.capitalize()}")
                    }
                }

                if (classesProcessTask == null) {
                    println "isLowerVersion:${isLowerVersion},isHighVersion:${isHighVersion},Skip ${project.name}'s hack process"
                    return
                }
                classesProcessTask.outputs.upToDateWhen { false }

                def modules = [:]
                project.rootProject.allprojects.each { pro ->
                    //modules.add("exploded-aar" + File.separator + pro.group + File.separator + pro.name + File.separator)
                    modules[pro.name] = "exploded-aar" + File.separator + pro.group + File.separator + pro.name + File.separator
                }

                if (preDexTask) {
                    preDexTask.outputs.upToDateWhen { false }
                    def hackClassesBeforePreDex = "hackClassesBeforePreDex${variant.name.capitalize()}"
                    project.task(hackClassesBeforePreDex) << {
                        def jarDependencies = []

                        preDexTask.inputs.files.files.each { f ->
                            if (f.path.endsWith(".jar")) {
                                println("preDexTask input files : " + f.path)
                                injectFile(file)
                                jarDependencies.add(f.path)
                            }
                        }
                        def json = new JsonBuilder(jarDependencies).toPrettyString()
                        project.logger.info(json)
                        HackFileUtils.saveJson(json, HackFileUtils.joinPath(HackFileUtils.getBuildCacheDir(project.buildDir.absolutePath), "jar_dependencies.json"), true);
                    }

                    def hackClassesBeforePreDexTask = project.tasks[hackClassesBeforePreDex]
                    hackClassesBeforePreDexTask.dependsOn preDexTask.taskDependencies.getDependencies(preDexTask)
                    preDexTask.dependsOn hackClassesBeforePreDexTask
                }

                //hack classes inject method
                def hackClassesBeforeDex = "hackClassesBeforeDex${variant.name.capitalize()}"
                project.task(hackClassesBeforeDex) << {
                    def jarDependencies = []
                    classesProcessTask.inputs.files.files.each { f ->
                        if (f.isDirectory()) {
                            f.eachFileRecurse(FileType.FILES) { file ->
                                println("file name :" + file.name + "|file path : " + file.path)
                                injectFile(file)

                                if (file.path.endsWith(".jar")) {
                                    jarDependencies.add(file.path)
                                }
                            }
                        } else {
                            println("file name :" + f.name + "|file path : " + f.path)
                            injectFile(f)
                            if (f.path.endsWith(".jar")) {
                                jarDependencies.add(f.path)
                            }
                        }
                    }
                }

                //hack class before dex
                def taskBeforeDex = "taskBeforeDex${variant.name.capitalize()}"
                project.task(taskBeforeDex) << {
                    println("start task before transformClassesWithDex")
                    project.tasks.findByName("compileDebugJavaWithJavac").enabled = false
                }

                if (classesProcessTask) {
                    def hackClassesBeforeDexTask = project.tasks[hackClassesBeforeDex]
                    hackClassesBeforeDexTask.dependsOn classesProcessTask.taskDependencies.getDependencies(classesProcessTask)
                    classesProcessTask.dependsOn hackClassesBeforeDexTask
                }

                def assembleTask = project.tasks.findByName("assemble${variant.name.capitalize()}")

                if (assembleTask) {

                    assembleTask.doLast {
                        println "Test assemble task do last inject file path : ${argsFilePath}"
                        if (argsFilePath != null) {
                            File file = new File(argsFilePath)
                            HackInjector.injectFile(file)
                        }
                    }
                }
            }
        }
    }


    private static int getMinSdkVersion(def mergedFlavor, String manifestPath) {
        if (mergedFlavor.minSdkVersion != null) {
            return mergedFlavor.minSdkVersion.apiLevel
        } else {
            return getMinSdkVersionWithPath(manifestPath)
        }
    }

    public static int getMinSdkVersionWithPath(String manifestPath) {
        def minSdkVersion = 0
        def manifestFile = new File(manifestPath)
        if (manifestFile.exists() && manifestFile.isFile()) {
            def manifest = new XmlSlurper(false, false).parse(manifestFile)
            minSdkVersion = manifest."uses-sdk"."@android:minSdkVersion".text()
        }
        return Integer.valueOf(minSdkVersion)
    }

    private void injectFile(File file) {
        if (HackFileUtils.isEmpty(argsFilePath)) {
            HackInjector.injectFile(file)
        }
    }

    //test for make jars
    private void createMakeJarTasks() {
        // Create a task to compile all java sources.
        compileJavaSrc = project.tasks.create("compileJava", JavaCompile);
        compileJavaSrc.setDescription("编译java源代码")
        compileJavaSrc.source = extension.javaSrcDir
        compileJavaSrc.include("com/nought/hellolib/**")
        compileJavaSrc.classpath = project.files([extension.androidJarDir + "/android.jar", extension.javaBase + "/" + extension.javaRt])
        compileJavaSrc.destinationDir = project.file(extension.classesOutDir)
        compileJavaSrc.sourceCompatibility = JavaVersion.VERSION_1_7
        compileJavaSrc.targetCompatibility = JavaVersion.VERSION_1_7
        compileJavaSrc.options.encoding = "UTF-8"
        compileJavaSrc.options.debug = false
        compileJavaSrc.options.verbose = false

        // Create a task to jar the classes.
        jarLib = project.tasks.create("jarLib", Jar);
        jarLib.setDescription("将class文件打包成jar")
        jarLib.dependsOn compileJavaSrc
        jarLib.archiveName = "helloLib.jar"
        jarLib.from(extension.classesOutDir)
        jarLib.destinationDir = project.file(extension.outputFileDir)
        jarLib.exclude("com/nought/hellolib/BuildConfig.class")
        jarLib.exclude("com/nought/hellolib/BuildConfig\$*.class")
        jarLib.exclude("**/R.class")
        jarLib.exclude("**/R\$*.class")
        jarLib.include("com/nought/hellolib/*.class")

        // Create a task to proguard the jar.
        proguardLib = project.tasks.create("proguardLib", ProGuardTask);
        proguardLib.setDescription("混淆jar包")
        proguardLib.dependsOn jarLib
        proguardLib.injars(extension.outputFileDir + "/" + "helloLib.jar")
        proguardLib.outjars(extension.outputFileDir + "/" + extension.outputFileName)
        proguardLib.libraryjars(extension.androidJarDir + "/android.jar")
        proguardLib.libraryjars(extension.javaBase + "/" + extension.javaRt)
        proguardLib.configuration(extension.proguardConfigFile)
        proguardLib.printmapping(extension.outputFileDir + "/" + "helloLib.mapping")

        // Create a task to copy the jar.
        copyLib = project.tasks.create("copyLib", Copy);
        copyLib.setDescription("不混淆，仅拷贝jar包")
        copyLib.dependsOn jarLib
        copyLib.from(extension.outputFileDir)
        copyLib.into(extension.outputFileDir)
        copyLib.include("helloLib.jar")
        copyLib.rename("helloLib.jar", extension.outputFileName)

        def packageProguardJar = project.tasks.create("packageProguardJar");
        packageProguardJar.setDescription("打包混淆、关闭log开关的hello lib")
        // packageProguardJar任务作为一个钩子，依赖真正执行工作的proguardLib
        packageProguardJar.dependsOn proguardLib
        // 最后把log开关置回原来开发时的状态
        packageProguardJar.doLast {
            enableLoggerDebug(true)
        }

        def packageNoProguardJar = project.tasks.create("packageNoProguardJar");
        packageNoProguardJar.setDescription("打包不混淆、开启log开关的hello lib")
        // packageNoProguardJar任务作为一个钩子，依赖真正执行工作的copyLib
        packageNoProguardJar.dependsOn copyLib
    }

    // 如果是执行packageProguardJar任务，那么要提前关闭log开关
    def enableLoggerDebug(boolean flag) {
        def loggerFilePath = "src/main/java/com/nought/hellolib/UncleNought.java"
        def updatedDebug = new File(loggerFilePath).getText('UTF-8')
                .replaceAll("ENABLE_DEBUG\\s?=\\s?" + (!flag).toString(), "ENABLE_DEBUG = " + flag.toString())
        new File(loggerFilePath).write(updatedDebug, 'UTF-8')
        println(flag ? 'ENABLE_DEBUG : [true]' : 'ENABLE_DEBUG : [false]')
    }
}