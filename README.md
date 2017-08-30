# play-yang
 Play Platform is to build a high performance, high scalability java development platform, complete the general management function. It adopts the background management, centralized deployment, and the separation of the member business system. It can separate the background management system, the front-end display system and the user center system. Developers can expand on this basis, and then use a core, you can develop a variety of Internet products.
 
 [JNC](https://github.com/tail-f-systems/JNC)<br/>
 [pyang](https://github.com/mbj4668/pyang)
# play-jnc
[pyang](http://www.yang-central.org/twiki/pub/Main/YangTools/pyang.1.html)
# yang-maven-plugin

Download
--------

Download [the latest JAR](https://search.maven.org/remote_content?g=com.airlenet.yang&a=yang-maven-plugin&v=LATEST) or grab via Maven:
```xml
<dependency>
  <groupId>com.airlenet.yang</groupId>
  <artifactId>yang-maven-plugin</artifactId>
  <version>1.0.0.RELEASE</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.airlenet.yang:yang-maven-plugin:1.0.0.RELEASE'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].

Usage
--------

```
<plugins>
   <plugin>
       <groupId>com.airlenet.yang</groupId>
       <artifactId>yang-maven-plugin</artifactId>
       <version>${project.version}</version>
       <executions>
           <execution>
               <id>process</id>
               <goals>
                   <goal>process</goal>
               </goals>
               <configuration>
                   <outputDirectory>target/generated-sources/java</outputDirectory>
                   <skip>false</skip>
                   <yangs>
                       <yang>demo.yang</yang>
                       <yang>iana-if-type.yang</yang>
                   </yangs>
               </configuration>
           </execution>
       </executions>
   </plugin>
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

参考 [yang-example](https://github.com/airshiplay/play-yang/tree/master/yang-example)


## License
See [License File](LICENSE).
