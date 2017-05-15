package org.eobjects.build;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

public final class PluginHelper {

    public static final String PROPERTY_BASEDIR = "${project.basedir}";

    public static final String PROPERTY_BUILD_DIR = "${project.build.directory}";

    public static PluginHelper get(Log log, File basedir, Map<String, String> environment, File dotnetPackOutput,
            String buildConfiguration, boolean skip) {
        return new PluginHelper(log, basedir, environment, dotnetPackOutput, buildConfiguration, skip);
    }

    private final File basedir;
    private final Map<String, String> environment;
    private final boolean skip;
    private final File dotnetPackOutput;
    private final String buildConfiguration;
    private final Log log;

    private PluginHelper(Log log, File basedir, Map<String, String> environment, File dotnetPackOutput,
            String buildConfiguration, boolean skip) {
        this.log = log;
        this.basedir = basedir;
        this.environment = environment == null ? Collections.<String, String> emptyMap() : environment;
        this.buildConfiguration = buildConfiguration == null ? "Release" : buildConfiguration;
        this.dotnetPackOutput = dotnetPackOutput == null ? new File("bin") : dotnetPackOutput;
        this.skip = skip;
    }

    public boolean isSkip() {
        return skip;
    }

    private final FileFilter projectFileDirectoryFilter = new FileFilter() {
        public boolean accept(File dir) {
            if (dir.isDirectory()) {
                if (getProjectFile(dir, false) != null) {
                    return true;
                }
            }
            return false;
        }
    };

    private final FilenameFilter csProjFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            if (name.endsWith(".csproj")) {
                return true;
            }
            return false;
        }
    };

    public File getNugetPackageDir(File subDirectory) {
        if (dotnetPackOutput.isAbsolute()) {
            return dotnetPackOutput;
        }
        final File directory = new File(subDirectory, dotnetPackOutput.getPath());
        return directory;
    }

    public File getNugetPackage(File subDirectory) {
        final File packageDirectory = getNugetPackageDir(subDirectory);
        final File[] nugetPackages = packageDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".nupkg");
            }
        });

        if (nugetPackages == null || nugetPackages.length == 0) {
            throw new IllegalStateException("Could not find NuGet package! ModuleDir=" + subDirectory + ", PackageDir="
                    + packageDirectory + ", PackOutput=" + dotnetPackOutput);
        }

        return nugetPackages[0];
    }

    public File[] getProjectDirectories() throws MojoFailureException {
        return getProjectDirectories(true);
    }

    public File[] getProjectDirectories(boolean throwExceptionWhenNotFound) throws MojoFailureException {
        if (skip) {
            return new File[0];
        }

        final File directory = basedir;
        if (projectFileDirectoryFilter.accept(directory)) {
            return new File[] { directory };
        }

        final File[] directories = directory.listFiles(projectFileDirectoryFilter);
        if (directories == null || directories.length == 0) {
            if (throwExceptionWhenNotFound) {
                throw new MojoFailureException("Could not find any directories with a project/build file.");
            } else {
                return new File[0];
            }
        }
        return directories;
    }

    public void executeCommand(File subDirectory, String command) throws MojoFailureException {
        executeCommand(subDirectory, command.split(" "));
    }

    public void executeCommand(File subDirectory, String... command) throws MojoFailureException {
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(subDirectory);
        for (Entry<String, String> entry : environment.entrySet()) {
            final String key = entry.getKey();
            if (key != null) {
                String value = entry.getValue();
                if (value == null) {
                    value = "";
                }
                processBuilder.environment().put(key, value);
            }
        }
        processBuilder.inheritIO();

        final int exitCode;
        try {
            final Process process = processBuilder.start();
            exitCode = process.waitFor();
        } catch (Exception e) {
            throw new MojoFailureException("Command (in " + subDirectory + ") " + Arrays.toString(command) + " failed",
                    e);
        }

        if (exitCode == 0) {
            // success
        } else {
            throw new MojoFailureException("Command (in " + subDirectory + ") " + Arrays.toString(command)
                    + " returned non-zero exit code: " + exitCode);
        }
    }

    public String getBuildConfiguration() {
        return buildConfiguration;
    }

    public boolean isNugetAvailable() {
        // This is pretty clunky, but I think the only manageable way to
        // determine it.
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("nuget", "help");
            processBuilder.inheritIO();
            final Process process = processBuilder.start();
            final boolean exited = process.waitFor(30, TimeUnit.SECONDS);
            if (!exited) {
                process.destroy();
                throw new RuntimeException("The command 'nuget help' failed to finish within 30 seconds!");
            }
            final int exitCode = process.exitValue();
            return exitCode == 0;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            return false;
        }
    }

    public DotnetProjectFile getProjectFile(File directory) {
        return getProjectFile(directory, true);
    }

    public DotnetProjectFile getProjectFile(File directory, boolean throwExceptionIfNotFound) {
        final File projectJson = new File(directory, "project.json");
        if (projectJson.exists()) {
            return new ProjectJsonFile(projectJson, log);
        }
        final File[] csProjFiles = directory.listFiles(csProjFilter);
        if (csProjFiles == null || csProjFiles.length == 0) {
            if (throwExceptionIfNotFound) {
                throw new IllegalArgumentException("Could not resolve any project/build file in directory: "
                        + directory);
            }
            return null;
        }
        return new CsProjFile(csProjFiles[0], log);
    }
}
