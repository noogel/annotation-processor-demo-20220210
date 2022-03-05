# 插入式注解处理器 DEMO

annotation processor demo using java11

## env

java11

## debug

① idea

Run > Edit Configurations…

Add New Configuration (Type: Remote)

Keep all defaults & Just change the port to 8000

② CLI

```
mvnDebug clean install
```

③ idea run debug


## 问题

## 注解处理器不生效

看下是否增加 javax.annotation.processing.Processor 文件和注解处理器路径是否一致。

看下注解处理器中支持的注解路径和名称是否存在。
@SupportedAnnotationTypes("noogel.xyz.provider.StrengthenBuilder")

## 编译报错

https://stackoverflow.com/questions/46874126/java-lang-assertionerror-thrown-by-compiler-when-adding-generated-method-with-pa

耗费了三个时间块，网上的问题反馈很少。说明没有定位到根源问题，需要跳出问题表象。发现 BizUtils 和 StringProviderProcessor 不在一个包就会有问题，暂时放在一个包让其编译成功，反正也是一个demo 项目。
