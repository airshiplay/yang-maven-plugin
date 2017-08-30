# play-yang

基于jython pyang jnc将yang文件生成java对象，自动安装jython、pyang、jnc

```
java -jar /Users/lig/jython2.7.1/jython.jar  setup.py install

java -jar /Users/lig/jython2.7.1/jython.jar  bin/pyang  -f jnc --plugindir /Users/lig/Documents/workspace/play-yang/yang-codegen/src/main/resources  --jnc-output /Users/lig/Documents/workspace/play-yang/yang-codegen/target/generated-sources/java/com.air     --jnc-classpath-schema-loading /Users/lig/Documents/workspace/play-yang/yang-example/src/main/yang/demo.yang

```

play-yang
```
pyang -f jnc --plugindir /Users/lig/Documents/workspace/play-yang/yang-codegen/src/main/resources  --jnc-output /Users/lig/Documents/workspace/play-yang/yang-codegen/target/generated-sources/java/com.air     --jnc-classpath-schema-loading /Users/lig/Documents/workspace/play-yang/yang-example/src/main/yang/demo.yang

```
