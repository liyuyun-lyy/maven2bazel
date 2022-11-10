package com.alibaba.aone.maven2bazel.convert.pattern.model;

import java.io.Serializable;
import java.util.List;

public class MavenDependencyModel implements Serializable {

    /**
     * mvn dependency:tree生成后的行数
     */
    private Integer lineNumber;

    /**
     * 依赖的完整内容
     */
    private String dependencyContent;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 前缀
     */
    private String prefix;

    /**
     * maven的groupId
     */
    private String groupId;

    /**
     * maven的artifactId
     */
    private String artifactId;

    /**
     * maven的type,例如jar,pom
     */
    private String type;

    /**
     * maven的version
     */
    private String version;

    /**
     * maven的依赖作用范围 compile,runtime,test,provided暂时不支持system
     */
    private String scope;

    /**
     * 依赖子节点
     */
    private List<MavenDependencyModel> children;

    /**
     * 排除子节点
     */
    private List<MavenDependencyModel> exclusions;

    /**
     * maven的前缀:默认为maven,若出现多个子模块不同的插件，使用maven_name
     */
    private String mavenPrefix;

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<MavenDependencyModel> getChildren() {
        return children;
    }

    public void setChildren(List<MavenDependencyModel> children) {
        this.children = children;
    }

    public List<MavenDependencyModel> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<MavenDependencyModel> exclusions) {
        this.exclusions = exclusions;
    }

    public String getDependencyContent() {
        return dependencyContent;
    }

    public void setDependencyContent(String dependencyContent) {
        this.dependencyContent = dependencyContent;
    }

    public String getMavenPrefix() {
        return mavenPrefix;
    }

    public void setMavenPrefix(String mavenPrefix) {
        this.mavenPrefix = mavenPrefix;
    }
}

