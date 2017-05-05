package com.bigocto.hack.bean

/**
 * Created by zhangyu
 * on 2017/5/4.
 */
class HackClassInfo {
    String packageName
    String className
    List<String> methodList

    String getPackageName() {
        return packageName
    }

    void setPackageName(String packageName) {
        this.packageName = packageName
    }

    String getClassName() {
        return className
    }

    void setClassName(String className) {
        this.className = className
    }

    List<String> getMethodList() {
        return methodList
    }

    void setMethodList(List<String> methodList) {
        this.methodList = methodList
    }
}
