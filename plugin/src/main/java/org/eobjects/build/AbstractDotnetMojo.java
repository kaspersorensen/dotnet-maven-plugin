package org.eobjects.build;

import java.io.File;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractDotnetMojo extends AbstractMojo {

    @Parameter(defaultValue = PluginHelper.PROPERTY_BASEDIR, readonly = true)
    private File basedir;

    @Parameter(property = "environment", required = false)
    private Map<String, String> environment;

    @Parameter(property = "skip", required = false, defaultValue = "false")
    private boolean skip;

    public PluginHelper getPluginHelper() {
        return PluginHelper.get(basedir, environment, skip);
    }
}
