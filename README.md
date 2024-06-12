# mirth-plugin-maven-plugin-kt

> Note: `mirth-plugin-maven-plugin-kt` is a successor/continuation to the previous [mirth-plugin-maven-plugin](https://github.com/kpalang/mirth-plugin-maven-plugin). It's a rewrite in, as you might guess, Kotlin.

A maven plugin to simplify and automate [NextGen Connect](https://github.com/nextgenhealthcare/connect) plugin development.

---
## Installation
`pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    ...

    <repositories>
        <repository>
            <id>repsy</id>
            <url>https://repo.repsy.io/mvn/kpalang/default</url>
        </repository>
    </repositories>

    ...

     <dependencies>
        <dependency>
            <groupId>com.kaurpalang</groupId>
            <artifactId>mirth-plugin-maven-plugin</artifactId>
            <version>2.0.4</version>
        </dependency>
     </dependencies>

    ...

    <pluginRepositories>
        <pluginRepository>
            <id>repsy-default</id>
            <url>https://repo.repsy.io/mvn/kpalang/default</url>
        </pluginRepository>
    </pluginRepositories>
</project>
```
---
## Usage

See [Sample project](https://github.com/kpalang/mirth-sample-plugin) on GitHub

### `@MirthServerClass`
Annotated class will be generated as a `<string>` entry.
```xml
<serverClasses>
    <string>com.kaurpalang.mirthpluginsample.server.ServerPlugin</string>
    <string>com.kaurpalang.mirthpluginsample.server.ServerPlugin2</string>
</serverClasses>
```

### `@MirthClientClass`
Annotated class will be generated as a `<string>` entry.
```xml
<clientClasses>
    <string>com.kaurpalang.mirthpluginsample.client.ClientPlugin</string>
    <string>com.kaurpalang.mirthpluginsample.client.ClientPlugin2</string>
</clientClasses>
```

### `@MirthApiProvider(ApiProviderType type)`
Annotated class will be generated as a `<apiProvider>` entry.
```xml
<apiProvider name="com.kaurpalang.mirthpluginsample.shared.ApiProviderSample" type="SERVLET_INTERFACE"/>
```

### Libraries

All libraries inside `pluginroot/libs/runtime/{type}` are packaged into the `.zip` archive into `libs` directory.
```xml
<library path="libs/sample-external.jar" type="{type}"/>
```

---

## Maven goals overview

#### generate-aggregator
Purpose is to generate a file to store all found classes before annotation processing.

#### generate-plugin-xml
Purpose is to generate the actual plugin.xml file.

| Parameter | Description                                                                        | Default                  |
| ------ |------------------------------------------------------------------------------------|--------------------------|
| \<name> | Plugin's name                                                                      | `default_name`           |
| \<author> | Plugin's author                                                                    | `default_author`         |
| \<pluginVersion> | Plugin version                                                                     | `default_plugin_version` |
| \<mirthVersion> | Mirth versions this plugin is compatible with                                      | `default_mirth_version`  |
| \<url> | Plugin's website                                                                   | blank                    |
| \<description> | Plugin's description                                                               | blank                    |
| \<path> | The name of the directory that will be extracted into Mirth's extensions directory | `default_path`           |
| \<pluginXmlOutputPath> | Where to put the generated `plugin.xml`                                             | `/plugin.xml`            |
