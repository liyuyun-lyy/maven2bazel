package com.alibaba.aone.maven2bazel.convert.model;

import java.io.Serializable;

/**
 * lombok的实体类
 */
public class LombokModel implements Serializable {
    /**
     * 下载url
     */
    private String url;

    /**
     * sha256
     */
    private String sha256;

    public LombokModel() {
    }

    public LombokModel(String url, String sha256) {
        this.url = url;
        this.sha256 = sha256;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }
}

