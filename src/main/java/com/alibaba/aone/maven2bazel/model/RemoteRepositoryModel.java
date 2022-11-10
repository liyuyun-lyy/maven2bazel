package com.alibaba.aone.maven2bazel.model;

import java.io.Serializable;

public class RemoteRepositoryModel implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * 类型
     */
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * url地址
     */
    private String url;
}
