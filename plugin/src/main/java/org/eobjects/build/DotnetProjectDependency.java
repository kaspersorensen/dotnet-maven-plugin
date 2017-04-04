package org.eobjects.build;

public class DotnetProjectDependency {

    private final String name;
    private final String version;

    public DotnetProjectDependency(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public DotnetProjectDependency(String name) {
        this.name = name;
        this.version = null;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DotnetProjectDependency other = (DotnetProjectDependency) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }
}
