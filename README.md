# yang-java
Yang file is parsed by Java antlr4; Yang file is parsed by java code and JNC code is generated.
Its principle is based on JNC and onos-yang-tools
 

 Reference
 [JNC](https://github.com/tail-f-systems/JNC)
 [pyang](https://github.com/mbj4668/pyang)
 [antlr](https://www.antlr.org/)
 [onos-yang-tools](https://github.com/opennetworkinglab/onos-yang-tools)
# yang-jnc

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
            <yangFilesDir>src/main/yang</yangFilesDir>
            <packageName>com.test.yang.model.gen</packageName>
            <classFileDir>src/main/java</classFileDir>
        </configuration>
        <executions>
            <execution>
                <id>default</id>
                <goals>
                    <goal>yang2java</goal>
                </goals>
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
```
mvn package                 # generator java class from yang model,compile,package

```
 [yang-example](https://github.com/airshiplay/play-yang/tree/master/yang-example)


## License
See [License File](LICENSE).
