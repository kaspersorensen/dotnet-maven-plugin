package org.eobjects.build;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.apache.maven.plugin.MojoFailureException;

public final class DotnetHelper {

    public static DotnetHelper get() {
        return new DotnetHelper();
    }

    private DotnetHelper() {
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
        final File directory = new File(".");
        if (projectJsonDirectoryFilter.accept(directory)) {
            return new File[] { directory };
        }

        final File[] directories = directory.listFiles(projectJsonDirectoryFilter);
        if (directories == null || directories.length == 0) {
            throw new MojoFailureException("Could not find any directories with a 'project.json' file.");
        }
        return directories;
    }

    public void executeCommand(File subDirectory, String command) throws MojoFailureException {
        executeCommand(subDirectory, command.split(" "));
    }

    public void executeCommand(File subDirectory, String... command) throws MojoFailureException {
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(subDirectory);
        processBuilder.inheritIO();

        final int exitCode;
        try {
            final Process process = processBuilder.start();
            exitCode = process.waitFor();
        } catch (Exception e) {
            throw new MojoFailureException("Command " + Arrays.toString(command) + " failed", e);
        }

        if (exitCode == 0) {
            // success
        } else {
            throw new MojoFailureException("Command " + Arrays.toString(command) + " returned non-zero exit code: "
                    + exitCode);
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
