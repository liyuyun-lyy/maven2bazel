package com.alibaba.aone.maven2bazel.state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

@State(name = "com.alibaba.aone.maven2bazel.state.BazelSettings", storages = {
    @Storage("com.alibaba.aone.maven2bazel.state.xml")})
public class BazelSettings implements PersistentStateComponent<BazelSettings> {
    private static BazelSettings bazelSettings = null;

    /**
     * mavenHome
     */
    private String mavenHome;

    /**
     * mavenHome的历史记录
     */
    private Set<String> mavenHomeHistories;

    /**
     * 本地maven仓库
     */
    private String localRepository;

    /**
     * 本地maven仓库的历史记录
     */
    private Set<String> localRepositoryHistories;

    /**
     * bazel的JdkHome
     */
    private String bazelJdkHome;

    /**
     * bazel的JdkHome仓库的历史记录
     */
    private Set<String> bazelJdkHomeHistories;

    /**
     * bazel的Maven仓库缓存
     */
    private String bazelMavenRepositoryCache;

    /**
     * bazel的Maven仓库缓存历史记录
     */
    private Set<String> bazelMavenRepositoryCacheHistories;

    /**
     * RemoteRepositoryModel集合
     */
    //    private Map<String, RemoteRepositoryModel> url2RemoteRepositoryMap;
    private List<String> remoteRepositories;

    /**
     * JDK水平
     */
    private String languageLevel;

    /**
     * 编码
     */
    private String encoding;

    public static BazelSettings getInstance() {
        try {
            if (bazelSettings != null) {
                return bazelSettings;
            }
            bazelSettings = ServiceManager.getService(BazelSettings.class);
        } catch (Exception e) {
            if (bazelSettings != null) {
                return bazelSettings;
            }
        }
        return bazelSettings;
    }

    @Override
    public @Nullable BazelSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull BazelSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static BazelSettings getBazelSettings() {
        return bazelSettings;
    }

    public static void setBazelSettings(BazelSettings bazelSettings) {
        BazelSettings.bazelSettings = bazelSettings;
    }

    public String getMavenHome() {
        return mavenHome;
    }

    public void setMavenHome(String mavenHome) {
        this.mavenHome = mavenHome;
    }

    public Set<String> getMavenHomeHistories() {
        return mavenHomeHistories;
    }

    public void setMavenHomeHistories(Set<String> mavenHomeHistories) {
        this.mavenHomeHistories = mavenHomeHistories;
    }

    public String getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(String localRepository) {
        this.localRepository = localRepository;
    }

    public Set<String> getLocalRepositoryHistories() {
        return localRepositoryHistories;
    }

    public void setLocalRepositoryHistories(Set<String> localRepositoryHistories) {
        this.localRepositoryHistories = localRepositoryHistories;
    }

    public String getBazelJdkHome() {
        return bazelJdkHome;
    }

    public void setBazelJdkHome(String bazelJdkHome) {
        this.bazelJdkHome = bazelJdkHome;
    }

    public Set<String> getBazelJdkHomeHistories() {
        return bazelJdkHomeHistories;
    }

    public void setBazelJdkHomeHistories(Set<String> bazelJdkHomeHistories) {
        this.bazelJdkHomeHistories = bazelJdkHomeHistories;
    }

    public String getBazelMavenRepositoryCache() {
        return bazelMavenRepositoryCache;
    }

    public void setBazelMavenRepositoryCache(String bazelMavenRepositoryCache) {
        this.bazelMavenRepositoryCache = bazelMavenRepositoryCache;
    }

    public Set<String> getBazelMavenRepositoryCacheHistories() {
        return bazelMavenRepositoryCacheHistories;
    }

    public void setBazelMavenRepositoryCacheHistories(Set<String> bazelMavenRepositoryCacheHistories) {
        this.bazelMavenRepositoryCacheHistories = bazelMavenRepositoryCacheHistories;
    }

    public List<String> getRemoteRepositories() {
        return remoteRepositories;
    }

    public void setRemoteRepositories(List<String> remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
    }

    public String getLanguageLevel() {
        return languageLevel;
    }

    public void setLanguageLevel(String languageLevel) {
        this.languageLevel = languageLevel;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
