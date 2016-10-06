package org.eobjects.build;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.maven.plugin.MojoFailureException;

public final class PluginHelper {

    public static final String PROPERTY_BASEDIR = "${project.basedir}";

    public static PluginHelper get(File basedir, Map<String, String> environment, boolean skip) {
        return new PluginHelper(basedir, environment, skip);
    }

    private final File basedir;
    private final Map<String, String> environment;
    private final boolean skip;

    private PluginHelper(File basedir, Map<String, String> environment, boolean skip) {
        this.basedir = basedir;
        this.environment = environment == null ? Collections.<String, String> emptyMap() : environment;
        this.skip = skip;
    }
    
    public boolean isSkip() {
        return skip;
    }

    private final FileFilter projectJsonDirectoryFilter = new FileFilter() {
        public boolean accept(File dir) {
            if (dir.isDirectory()) {
                if (new File(dir, "project.json").exists()) {
                    return true;
                }
            }
            return false;
        }
    };

    public File getNugetPackage(File subDirectory) {
        final File binDirectory = new File(subDirectory, "bin");
        final File packageDirectory = new File(binDirectory, getBuildConfiguration());
        final File[] nugetPackages = packageDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".nupkg");
            }
        });

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
        if (projectJsonDirectoryFilter.accept(directory)) {
            return new File[] { directory };
        }

        final File[] directories = directory.listFiles(projectJsonDirectoryFilter);
        if (directories == null || directories.length == 0) {
            if (throwExceptionWhenNotFound) {
                throw new MojoFailureException("Could not find any directories with a 'project.json' file.");
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
        processBuilder.environment().putAll(environment);
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
        // hardcoded, but encapsulated for making it dynamic in the future.
        return "Release";
    }

    public boolean isNugetAvailable() {
        // This is pretty clunky, but I think the only manageable way to
        // determine it.
        try {
            final int exitCode = new ProcessBuilder("nuget", "help").start().waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
