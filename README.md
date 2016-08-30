# dotnet-maven-plugin

A Maven plugin for building dotnet projects based on `project.json`.

This plugin lets you use the power of Maven to drive .NET core builds.


## Installing

The plugin leverages the `dotnet` (required) and `nuget` (optional) command line tools, which have to be installed prior to usage of the plugin.

The plugin defines a new packaging type, `dotnet` and is enabled in `pom.xml` like this:

```
<project>
  [...]
  <packaging>dotnet</packaging>

  [...]
  <build>
    <plugins>
      <plugin>
        <groupId>org.eobjects.build</groupId>
        <artifactId>dotnet-maven-plugin</artifactId>
        <extensions>true</extensions>
      </plugin>
    </plugins>
  </build>
  [...]
</project>
```

Take a look at the [Example project](example-project) for an example.