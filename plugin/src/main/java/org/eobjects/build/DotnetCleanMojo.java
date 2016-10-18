package org.eobjects.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "clean", defaultPhase = LifecyclePhase.CLEAN)
public class DotnetCleanMojo extends AbstractDotnetMojo {
    
    @Parameter(property = "dotnet.clean.enabled", required = false, defaultValue = "true")
    private boolean cleanEnabled;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!cleanEnabled) {
            getLog().debug("Disabled, skipping");
            return;
        }

        final PluginHelper helper = getPluginHelper();
        for (File subDirectory : helper.getProjectDirectories(false)) {
            delete(new File(subDirectory, "bin"));
            delete(new File(subDirectory, "obj"));
            delete(new File(subDirectory, "project.lock.json"));

            getLog().info("Cleaned project: " + subDirectory);
        }
    }

    private void delete(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            if (file.isFile()) {
                // delete single file
                Files.delete(file.toPath());
                getLog().debug("Deleted file: " + file);
                return;
            }

            // delete directory by walking through it
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            getLog().debug("Deleted directory: " + file);
        } catch (IOException e) {
            getLog().warn("Could not delete directory: " + file, e);
        }
    }
}
