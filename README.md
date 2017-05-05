# HackSourceCode

指定相关的类和方法，在Android工程编译的过程中，会自动在方法前后，插入Trace节点，统计方法执行时间
## 目录说明
1. app目录，测试Android工程

2. buildSrc[hack] 目录，为插件工程，主要逻辑是注入apk build流程

3. trace 目录，是一个java的library目录，主要逻辑是注入的具体代码

## 使用说明
1. 使用UploadArchives生成本地插件，或者将插件工程命名为buildSrc

2. 主工程根目录build文件中添加对插件包的赖`classpath'com.bigocto.hack:hack-plugin:1.0.0'`

3. 在application 目录下的build文件中，添加`apply plugin: 'com.bigocto.hack'`,`compile project(':trace')`

4. 添加 hack_source_classes.json 文件，json目录结构参考
```json
[
  {
    "PACKAGE": "com.bigocto.hacksourcecode",
    "NAME": "MainActivity.class",
    "METHODS": [
      "<init>",
      "onCreate",
      "onPause"
    ]
  },
  {
    "PACKAGE": "com.bigocto.hacksourcecode",
    "NAME": "JavaInjectTest.class",
    "METHODS": [
      "test1",
      "test2"
    ]
  }
]
```
5. 在app的目录下的build文件中，添加参数
```groovy
HackSourceCode{
    configureJsonFile = "/Users/zhangyu/OpenProjects/HackSourceCode/app/hack_source_classes.json"
}
```

| 参数       | 内容           | 是否必须  |
| ------------- |:-------------:|:-----------:|
| configureJsonFile| 指定配置文件目录 | 必填 |
| useTransformApi | 是否使用transformApi方式 |非必须|
| gradleLowVersion |gradle 版本低于1.5|hook task 模式必填|
| gradleHighVersion |gradle 版本高于3.0|hook task 模式必填|
| debugInjectSpecifyFile |测试直接注入至某个文件|非必须|
