package org.eobjects.build;

import java.io.File;
import java.util.List;

public interface DotnetProjectFile {

    public File getFile();

    public boolean isTestProject();

    public String getVersion();

    public void setVersion(String version);

    public List<DotnetProjectDependency> getDependencies();

    public void setDependencyVersion(DotnetProjectDependency dependency, String version);

    public void saveChanges();
}
