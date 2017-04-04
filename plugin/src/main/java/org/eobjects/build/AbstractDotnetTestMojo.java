package org.eobjects.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractDotnetTestMojo extends AbstractDotnetMojo {

    @Parameter(property = "dotnet.test.outputxml", required = false)
    private File outputXml;

    public void executeInternal() throws MojoFailureException {
        final PluginHelper helper = getPluginHelper();
        ArrayList<String> argsList = new ArrayList<String>(Arrays.asList("dotnet", "test", "-c", helper.getBuildConfiguration()));
        if(outputXml != null) {
            outputXml.getParentFile().mkdirs();
            argsList.add("-xml");
            argsList.add(outputXml.getPath());
        }
        for (File subDirectory : helper.getProjectDirectories()) {
            if (isTestRunnable(subDirectory)) {
                helper.executeCommand(subDirectory, argsList.toArray(new String[argsList.size()]));
            }
        }
    }

    private boolean isTestRunnable(File subDirectory) {
        return getPluginHelper().getProjectFile(subDirectory).isTestProject();
    }
}
