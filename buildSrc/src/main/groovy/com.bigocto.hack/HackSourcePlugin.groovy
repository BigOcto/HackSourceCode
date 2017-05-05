package com.bigocto.hack

import com.android.build.gradle.AppExtension
import com.bigocto.hack.bean.HackClassInfo
import com.bigocto.hack.bean.HackConstans
import com.bigocto.hack.transform.TransformImpl
import groovy.io.FileType
import groovy.json.JsonBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by zhangyu
 * on 2017/3/19.
 */
class HackSourcePlugin implements Plugin<Project> {
    static final String PLUGIN_NAME = "HackSourceCode"

    Project project
    PluginExtension extension
    def debugInjectSpecifyFile = null
    def configureJsonFile = null

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = project.extensions.create(PLUGIN_NAME, PluginExtension)

        project.afterEvaluate {
            def useTransformApi = extension.useTransformApi
            debugInjectSpecifyFile = extension.debugInjectSpecifyFile
            configureJsonFile = extension.configureJsonFile
            if (!HackFileUtils.isEmpty(debugInjectSpecifyFile)) {
                println "debugInjectSpecifyFile path : ${debugInjectSpecifyFile}"
            }
            if (!HackFileUtils.isEmpty(configureJsonFile)) {
                println "configureJsonFile path : ${configureJsonFile}"
                HackFileUtils.getJson(configureJsonFile)
                List<HackClassInfo> infoList = HackFileUtils.getJson(configureJsonFile)
                for (HackClassInfo info : infoList){
                    String fullName = info.getPackageName()+"."+info.getClassName()
                    HackConstans.HACK_CLASS.put(info.getClassName(), info.getClassName())
                    HackConstans.HACK_CLASS_AND_METHOD.put(fullName, info.getMethodList())
                    if (HackConstans.PACKAGE_NAME.get(info.getPackageName()) == null){
                        HackConstans.PACKAGE_NAME.put(info.getPackageName(),info.getPackageName())
                    }
                }
            } else {
                println "configureJsonFile path null error !!!!!!!"
            }

            //Using transform api
            if (useTransformApi != null && useTransformApi.equals("true")) {
                def android = project.extensions.findByType(AppExtension)
                android.registerTransform(new TransformImpl(project))
            }

            //Using hook gradle task
            project.android.applicationVariants.each { variant ->
                if (useTransformApi == null || !useTransformApi.equals("true")) {
                    def gradleIsLowVersion = extension.gradleLowVersion
                    def gradleIsHighVersion = extension.gradleHighVersion

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
                                    injectFile(file)

                                    if (file.path.endsWith(".jar")) {
                                        jarDependencies.add(file.path)
                                    }
                                }
                            } else {
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
                }

                //inject specify file after assemble task
                def assembleTask = project.tasks.findByName("assemble${variant.name.capitalize()}")
                if (assembleTask) {
                    assembleTask.doLast {
                        println "Test assemble task do last inject file path : ${debugInjectSpecifyFile}"
                        if (debugInjectSpecifyFile != null) {
                            File file = new File(debugInjectSpecifyFile)
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
        if (HackFileUtils.isEmpty(debugInjectSpecifyFile)) {
            HackInjector.injectFile(file)
        }
    }

}