package com.alibaba.aone.maven2bazel.convert.template.model;

import com.alibaba.aone.maven2bazel.convert.global.Global;
import com.alibaba.aone.maven2bazel.enums.Constants;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * WORKSPACE的存储的类型
 *
 * @author Administrator
 */
public class ArchiveModel implements Serializable {

    /**
     * 存储类型:http,git,默认为http
     */
    private Constants.WorkspaceArchiveTypeEnums archiveType;

    /**
     * 存储名
     */
    private String name;

    /**
     * 存储的SHA256支
     */
    private String sha256;

    /**
     * 存储的前缀
     */
    private String stripPrefix;

    /**
     * 存储的url列表
     */
    private List<String> urls;

    /**
     * 加载插件 格式如下
     * load("@rules_jvm_external//:defs.bzl", "artifact", "maven_install")
     * load("@rules_jvm_external//:specs.bzl", "maven")
     **/
    private Multimap<String, String> loadMap;

    /**
     * 加载依赖列表
     */
    private List<String> dependencies;

    /**
     * 得到结果
     *
     * @return
     */
    public String getResult() {
        StringBuilder sb = new StringBuilder();
        if (archiveType == null) {
            archiveType = Constants.WorkspaceArchiveTypeEnums.HTTP;
        }

        sb.append(archiveType.value).append("(");

        sb.append(getArchiveValue("name", name));
        sb.append(getArchiveValue("sha256", sha256));
        //stripPrefix有空的情况 例如 rules_spring :https://github.com/salesforce/rules_spring
        //		http_archive(
        //			    name = "rules_spring",
        //			    sha256 = "9385652bb92d365675d1ca7c963672a8091dc5940a9e307104d3c92e7a789c8e",
        //			    urls = [
        //			        "https://github.com/salesforce/rules_spring/releases/download/2.1.4/rules-spring-2.1.4.zip",
        //			    ],
        //			)
        if (StringUtils.isNotBlank(stripPrefix)) {
            sb.append(getArchiveValue("strip_prefix", stripPrefix));
        }
        ;
        if (CollectionUtils.isNotEmpty(urls)) {
            if (urls.size() == 1) {
                sb.append(getArchiveValue("url", urls.get(0)));
            } else {
                StringBuilder urlBuilder = new StringBuilder();
                urlBuilder.append("[");
                for (String url : urls) {
                    urlBuilder.append("\n");
                    urlBuilder.append(Global.FOUR_BLANK_CHARACTER);
                    urlBuilder.append(Global.FOUR_BLANK_CHARACTER);
                    urlBuilder.append("\"");
                    urlBuilder.append(StringUtils.trim(url));
                    urlBuilder.append("\"");
                    urlBuilder.append(",");
                }
                urlBuilder.append("\n");
                urlBuilder.append(Global.FOUR_BLANK_CHARACTER);
                urlBuilder.append("]");

                sb.append("\n");
                sb.append(Global.FOUR_BLANK_CHARACTER);
                sb.append("urls = ");
                sb.append(urlBuilder.toString());
                sb.append(",");
            }
        }
        sb.append("\n");
        sb.append(")");

        //记载需要的插件
        if (loadMap != null && loadMap.size() > 0) {
            sb.append("\n");

            for (String key : loadMap.keySet()) {
                Collection<String> collection = loadMap.get(key);
                if (CollectionUtils.isEmpty(collection)) {
                    continue;
                }
                sb.append("\n");
                sb.append("load(\"").append(key).append("\"");
                for (String str : collection) {
                    sb.append(",");
                    sb.append(" ");
                    sb.append("\"");
                    sb.append(StringUtils.trim(str));
                    sb.append("\"");
                }
                sb.append(")");
            }
        }
        if (CollectionUtils.isNotEmpty(dependencies)) {
            for (String dependency : dependencies) {
                sb.append("\n");
                sb.append("\n");
                sb.append(StringUtils.trim(dependency));
            }
        }

        sb.append("\n");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * 得到加载的每一个信息
     *
     * @param key
     * @param value
     * @return
     */
    private static String getArchiveValue(String key, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(Global.FOUR_BLANK_CHARACTER).append(key).append(" = ").append("\"")
            .append(StringUtils.trim(value)).append("\"").append(",");
        return sb.toString();
    }

    public Constants.WorkspaceArchiveTypeEnums getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(Constants.WorkspaceArchiveTypeEnums archiveType) {
        this.archiveType = archiveType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getStripPrefix() {
        return stripPrefix;
    }

    public void setStripPrefix(String stripPrefix) {
        this.stripPrefix = stripPrefix;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public Multimap<String, String> getLoadMap() {
        return loadMap;
    }

    public void setLoadMap(Multimap<String, String> loadMap) {
        this.loadMap = loadMap;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
}
