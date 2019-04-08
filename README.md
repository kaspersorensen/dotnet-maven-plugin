# dotnet-maven-plugin

A Maven plugin for building dotnet projects based on `.csproj` or `project.json` files.

This plugin lets you use the power of Maven to drive .NET core builds.

[![Build Status: Linux](https://travis-ci.org/kaspersorensen/dotnet-maven-plugin.svg?branch=master)](https://travis-ci.org/kaspersorensen/dotnet-maven-plugin)

## Features

 * Drives the invocation of `dotnet` and `nuget` command line tools for build, test, deploy of .NET core projects.
 * Runs unittests via `dotnet test` on projects that have a defined Test Runner.
 * Offers a `mvn clean` option which arguably is currently missing from the `dotnet` CLI tools.
 * Propagates versioning scheme from Maven parent to the `.csproj` or `project.json` files of the modules.
 * Supports two project layouts: A pom-per-dotnet-module or a single-pom-for-all-dotnet-modules approach. See [example projects](example-projects).
 * The plugin has Maven extensions, allowing it to be idiomatically bound to standard Maven phases by declaring certain packging types:
  * `<packaging>dotnet</packaging>` will bind relevant Maven phases for dotnet component and application projects, including `dotnet pack`, `dotnet publish` and `dotnet nuget add`.
    * If a `<repository>` element is defined in the projects configuration, `dotnet nuget push`  is also bound to the `mvn deploy` phase.
  * `<packaging>dotnet-library</packaging>` will bind relevant Maven phases for dotnet class libraries, i.e. excluding `dotnet publish`.
  * `<packaging>dotnet-test</packaging>` will bind relevant Maven phases for dotnet test-only projects, including  `dotnet test`.
  * `<packaging>dotnet-integration-test</packaging>` will bind Maven phase `integration-test` to `dotnet test` and Maven phase to `verify` to the gathering/evaluation of those results, allowing for `post-integration-test` clean-up tasks to take place exiting on test failures.
  * All above packaging types also have `clean`, `restore`, `build` bindings

## Installing

The plugin leverages the `dotnet` (required) and `nuget` (optional) command line tools, which have to be installed prior to usage of the plugin.

The simples use-case it to simply define your project with the packaging type, `dotnet` and enabled it with a plugin in `pom.xml` like this:

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
        <version>0.24</version>
        <extensions>true</extensions>
      </plugin>
    </plugins>
  </build>
  [...]
</project>
```

## Configuration

Should you want to, you can configure many aspects of the `dotnet` and `nuget` invocations. The following should provide a handy overview by example:

```
<plugin>
  <groupId>org.eobjects.build</groupId>
  <artifactId>dotnet-maven-plugin</artifactId>
  <version>0.23</version>
  <extensions>true</extensions>
  <configuration>
    <buildConfiguration>Release</buildConfiguration>
    <buildEnabled>true</buildEnabled>
    <cleanEnabled>true</cleanEnabled>
    <environment>
      <MY_ENVVAR_1>hello</MY_ENVVAR_1>
      <MY_ENVVAR_2>world</MY_ENVVAR_2>
    </environment>
    <integrationTestRunEnabled>true</integrationTestRunEnabled>
    <integrationTestVerifyEnabled>true</integrationTestVerifyEnabled>
    <nugetAddEnabled>true</nugetAddEnabled>
    <nugetAddSource>~/.nuget/Packages</nugetAddSource>
    <nugetPushEnabled>true</nugetPushEnabled>
    <packEnabled>true</packEnabled>
    <packOutput>bin</packOutput>
    <publishEnabled>true</publishEnabled>
    <publishOutput>bin/my-publish-output</publishOutput>
    <repository>https://path/to/repository</repository>
    <restoreEnabled>true</restoreEnabled>
    <testEnabled>true</testEnabled>
  </configuration>
</plugin>
```

Take a look at the [Example projects](example-projects) for more inspiration.
