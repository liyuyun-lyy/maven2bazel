package com.alibaba.aone.maven2bazel.convert.model.workspace;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * WorkSpace的依赖Key
 */
public class WorkDependencyKey implements Serializable {

    /**
     * maven的groupId
     */
    private String groupId;

    /**
     * maven的artifactId
     */
    private String artifactId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WorkDependencyKey that = (WorkDependencyKey)o;
        return StringUtils.equals(StringUtils.trimToEmpty(groupId), StringUtils.trimToEmpty(that.groupId))
            && StringUtils.equals(StringUtils.trimToEmpty(artifactId), StringUtils.trimToEmpty(that.artifactId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId);
    }
}

