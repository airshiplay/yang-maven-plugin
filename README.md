# play-yang
 Play Yang  based on JNC,and a plugin for pyang to generate Java classes from YANG models.
 
 Reference
 [JNC](https://github.com/tail-f-systems/JNC)
 [pyang](https://github.com/mbj4668/pyang)
# play-jnc
Modify based on [JNC](https://github.com/tail-f-systems/JNC)

# yang-maven-plugin

Download
--------

Download [the latest JAR](https://search.maven.org/remote_content?g=com.airlenet.yang&a=yang-maven-plugin&v=LATEST) or grab via Maven:
```xml
<dependency>
  <groupId>com.airlenet.yang</groupId>
  <artifactId>yang-maven-plugin</artifactId>
  <version>LATEST</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.airlenet.yang:yang-maven-plugin:LATEST'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository](https://oss.sonatype.org/content/repositories/snapshots/com/airlenet/yang/yang-maven-plugin).

Usage
--------

```
<plugins>
    <plugin>
        <groupId>com.airlenet.yang</groupId>
        <artifactId>yang-maven-plugin</artifactId>
        <version>2.0.0-SNAPSHOT</version>
        <configuration>
            <skip>false</skip>
            <showWarnings>false</showWarnings>
            <errorAbort>false</errorAbort>
            <excludes>
                <exclude>tailf/*.yang</exclude>
                <exclude>ietf/*.yang</exclude>
                <exclude>iana/*.yang</exclude>
            </excludes>
        </configuration>
        <executions>
            <execution>
                <id>process</id>
                <goals>
                    <goal>process</goal>
                </goals>
                <configuration>
                    <outputDirectory>target/generated-sources/java</outputDirectory><!-- src/main/java -->
                    <packageName>com.airlenet.yang.model</packageName>
                </configuration>
            </execution>
        </executions>
    </plugin>
    <!-- add source resource: java class & resource -->
    <plugin>
       <groupId>org.codehaus.mojo</groupId>
       <artifactId>build-helper-maven-plugin</artifactId>
       <version>1.7</version>
       <executions>
           <execution>
               <id>add-source</id>
               <phase>generate-sources</phase>
               <goals>
                   <goal>add-source</goal>
               </goals>
               <configuration>
                   <sources>
                       <source>target/generated-sources/java</source>
                   </sources>
               </configuration>
           </execution>
           <execution>
               <id>add-resource</id>
               <phase>generate-resources</phase>
               <goals>
                   <goal>add-resource</goal>
               </goals>
               <configuration>
                   <resources>
                       <resource>
                           <directory>target/generated-sources/java</directory>
                       </resource>
                   </resources>
               </configuration>
           </execution>
       </executions>
    </plugin>
</plugins>
```
```
mvn package                 # generator java class from yang model,compile,package

mvn yang:validate           # validate yang model
```
 [yang-example](https://github.com/airshiplay/play-yang/tree/master/yang-example)


## License
See [License File](LICENSE).
