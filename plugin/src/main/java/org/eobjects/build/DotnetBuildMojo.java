package org.eobjects.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "build", defaultPhase = LifecyclePhase.COMPILE)
public class DotnetBuildMojo extends AbstractDotnetMojo {

    @Parameter(property = "dotnet.build.enabled", required = false, defaultValue = "true")
    private boolean buildEnabled;

    @Parameter(property = "dotnet.build.framework", required = false)
    private String buildFramework;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!buildEnabled) {
            getLog().debug("Disabled, skipping");
            return;
        }

        final PluginHelper helper = getPluginHelper();
        for (File subDirectory : helper.getProjectDirectories()) {
            if (!new File(subDirectory, "project.lock.json").exists()) {
                // restore first if needed
                helper.executeCommand(subDirectory, "dotnet", "restore");
            }
            final List<String> cmd = new ArrayList<>();
            cmd.add("dotnet");
            cmd.add("build");
            cmd.add("-c");
            cmd.add(helper.getBuildConfiguration());
            if(buildFramework != null && !buildFramework.isEmpty()){
                cmd.add("-f");
                cmd.add(buildFramework);
            }
            helper.executeCommand(subDirectory, cmd.toArray(new String[cmd.size()]));
        }
    }
}
