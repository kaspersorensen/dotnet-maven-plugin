package org.eobjects.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "publish", defaultPhase = LifecyclePhase.VERIFY)
public class DotnetPublishMojo extends AbstractDotnetMojo {

    @Parameter(property = "dotnet.publish.enabled", required = false, defaultValue = "true")
    private boolean publishEnabled;

    @Parameter(property = "dotnet.publish.output", required = false)
    private String publishOutput;

    @Parameter(property = "dotnet.publish.framework", required = false)
    private String publishFramework;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!publishEnabled) {
            getLog().debug("Disabled, skipping");
            return;
        }

        final PluginHelper helper = getPluginHelper();
        for (File subDirectory : helper.getProjectDirectories()) {
            final List<String> cmd = new ArrayList<>();
            cmd.add("dotnet");
            cmd.add("publish");
            cmd.add("-c");
            cmd.add(helper.getBuildConfiguration());
            if (publishOutput != null && !publishOutput.isEmpty()) {
                cmd.add("-o");
                final String targetPath;
                if (publishOutput.startsWith("~")) {
                    targetPath = System.getProperty("user.home") + publishOutput.substring(1);
                } else {
                    targetPath = publishOutput;
                }
                cmd.add(targetPath);
            }
            if (publishFramework != null && !publishFramework.isEmpty()) {
                cmd.add("-f");
                cmd.add(publishFramework);
            }
            helper.executeCommand(subDirectory, cmd.toArray(new String[cmd.size()]));
        }
    }
}
