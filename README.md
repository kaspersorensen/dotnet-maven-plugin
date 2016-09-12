# dotnet-maven-plugin

A Maven plugin for building dotnet projects based on `project.json`.

This plugin lets you use the power of Maven to drive .NET core builds.

## Features

 * Drives the invocation of `dotnet` and `nuget` command line tools for build, test, deploy of .NET core projects.
 * Offers a `mvn clean` option which arguably is currently missing from the `dotnet` CLI tools.
 * Propagates versioning scheme from Maven parent to the `project.json` files of the modules.
 * Supports two project layouts: A pom-per-dotnet-module or a single-pom-for-all-dotnet-modules approach. See [example projects](example-projects).
 * The plugin has Maven extensions, allowing it to be automatically bound to standard Maven phases with simply a plugin declaration and a new packaging type `dotnet`. 

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

Take a look at the [Example projects](example-projects) for more inspiration.