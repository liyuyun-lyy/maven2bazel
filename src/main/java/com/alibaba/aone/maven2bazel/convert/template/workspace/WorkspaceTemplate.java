package com.alibaba.aone.maven2bazel.convert.template.workspace;

import com.alibaba.aone.maven2bazel.convert.global.Global;
import com.alibaba.aone.maven2bazel.convert.model.LombokModel;
import com.alibaba.aone.maven2bazel.convert.pattern.model.MavenDependencyModel;
import com.alibaba.aone.maven2bazel.convert.template.model.ArchiveModel;
import com.alibaba.aone.maven2bazel.convert.utils.LombokUtil;
import com.alibaba.aone.maven2bazel.enums.Constants;
import com.alibaba.aone.maven2bazel.model.RemoteRepositoryModel;
import com.alibaba.aone.maven2bazel.state.BazelSettings;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WorkspaceTemplate {

    private static BazelSettings bazelSettings = BazelSettings.getInstance();

    public static String getWorkspaceString(MavenDependencyModel lombokDependency, String projectName,
        Multimap<String, String> mavenPrefix2WorkspaceMap, boolean isExistSpringBoot, boolean isExistWar) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(projectName)) {
            sb.append("workspace(name = \"");
            sb.append(StringUtils.trim(projectName));
            sb.append("\"");
            sb.append(")");
            sb.append("\n");
            sb.append("\n");
        }

        //加载http_archive
        if (lombokDependency == null) {
            sb.append("load(\"@bazel_tools//tools/build_defs/repo:http.bzl\", \"http_archive\")");
            sb.append("\n");
            sb.append("\n");
        } else {
            sb.append("load(\"@bazel_tools//tools/build_defs/repo:http.bzl\", \"http_archive\", \"http_jar\")");
            sb.append("\n");
            sb.append("\n");
            String httpJar = getHttpJar(lombokDependency.getVersion());
            sb.append(httpJar);
        }
        //判断是否有lombok

        sb.append(getRulesJvmExternal());
        sb.append(getRulesJava());

        sb.append(getRulesProto());
        sb.append(getComGoogleProtobuf());

        //是否是SpringBoot
        if (isExistSpringBoot) {
            sb.append(getRulesSpring());
        }

        //是否存在war包
        if (isExistWar) {
            sb.append(getWar());
        }

        if (mavenPrefix2WorkspaceMap != null && mavenPrefix2WorkspaceMap.size() > 0) {
            for (String mavenPrefix : mavenPrefix2WorkspaceMap.keySet()) {
                Collection<String> collection = mavenPrefix2WorkspaceMap.get(mavenPrefix);
                if (CollectionUtils.isEmpty(collection)) {
                    continue;
                }
                //加载maven插件
                sb.append("maven_install(");
                //maven前缀
                sb.append("\n");
                sb.append(Global.FOUR_BLANK_CHARACTER);
                sb.append("name = \"");
                if (StringUtils.isBlank(mavenPrefix)) {
                    sb.append("maven");
                } else {
                    sb.append(mavenPrefix);
                }
                sb.append("\"");
                sb.append(",");
                sb.append("\n");

                sb.append("\n");
                sb.append(Global.FOUR_BLANK_CHARACTER);
                sb.append("artifacts = [");

                for (String mavenDependency : collection) {
                    sb.append("\n");
                    sb.append(Global.FOUR_BLANK_CHARACTER);
                    sb.append(Global.FOUR_BLANK_CHARACTER);
                    sb.append(mavenDependency);
                }

                sb.append("\n").append(Global.FOUR_BLANK_CHARACTER);

                sb.append("],");

                sb.append("\n");

                //加载maven私服
                List<String> repositories = new ArrayList<String>();

                List<String> remoteRepositories = bazelSettings.getRemoteRepositories();
                if (CollectionUtils.isNotEmpty(remoteRepositories)) {
                    for (String remoteRepository : remoteRepositories) {
                        if (StringUtils.isBlank(remoteRepository)) {
                            continue;
                        }
                        RemoteRepositoryModel remoteRepositoryModel =
                            JSON.parseObject(remoteRepository, RemoteRepositoryModel.class);
                        if (remoteRepositoryModel == null) {
                            continue;
                        }

                        String id = remoteRepositoryModel.getId();
                        String type = remoteRepositoryModel.getType();
                        String url = remoteRepositoryModel.getUrl();
                        repositories.add(url);
                    }
                }

                //		repositories.add("http://admin:arpa2019@192.168.31.41:8081/repository/maven-public");
                //
                //                //TODO 加载阿里云私服,后面解析为maven的setting文件,加载配置文件中的私服地址
                //		repositories.add("https://maven.aliyun.com/repository/public");
                //		repositories.add("https://maven.aliyun.com/repository/google");

                sb.append(getRepositories(repositories));

                //加载其他属性
                sb.append("\n");
                sb.append(Global.FOUR_BLANK_CHARACTER);
                sb.append("use_unsafe_shared_cache = True,");
                sb.append("\n");
                sb.append(")");
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 得到http_jar插件
     *
     * @return
     */
    private static String getHttpJar(String version) {
        LombokModel lombokModel = LombokUtil.getLombokModel(version);

        ArchiveModel archiveModel = new ArchiveModel();
        archiveModel.setArchiveType(Constants.WorkspaceArchiveTypeEnums.HTTP_JAR);
        archiveModel.setName("lombok_edge");
        archiveModel.setSha256(lombokModel.getSha256());
        List<String> urlList = new ArrayList<String>();
        urlList.add(lombokModel.getUrl());
        archiveModel.setUrls(urlList);
        Multimap<String, String> loadMap = TreeMultimap.create();

        archiveModel.setLoadMap(loadMap);

        return archiveModel.getResult();
    }

    /**
     * 得到rules_jvm_external插件
     *
     * @return
     */
    private static String getRulesJvmExternal() {
        ArchiveModel archiveModel = new ArchiveModel();
        archiveModel.setName("rules_jvm_external");
        archiveModel.setSha256("f36441aa876c4f6427bfb2d1f2d723b48e9d930b62662bf723ddfb8fc80f0140");
        archiveModel.setStripPrefix("rules_jvm_external-4.1");
        List<String> urlList = new ArrayList<String>();
        urlList.add("https://github.com/bazelbuild/rules_jvm_external/archive/4.1.zip");
        archiveModel.setUrls(urlList);
        Multimap<String, String> loadMap = TreeMultimap.create();

        loadMap.put("@rules_jvm_external//:defs.bzl", "artifact");
        loadMap.put("@rules_jvm_external//:defs.bzl", "maven_install");

        loadMap.put("@rules_jvm_external//:specs.bzl", "maven");
        archiveModel.setLoadMap(loadMap);

        //		List<String> dependenyList=new ArrayList<String>();
        //
        //		dependenyList.add("rules_java_dependencies()");
        //		dependenyList.add("rules_java_toolchains()");
        //		archiveModel.setDependencies(dependenyList);
        return archiveModel.getResult();
    }

    /**
     * 得到rules_java插件
     *
     * @return
     */
    private static String getRulesJava() {
        ArchiveModel archiveModel = new ArchiveModel();
        archiveModel.setName("rules_java");
        archiveModel.setSha256("ccf00372878d141f7d5568cedc4c42ad4811ba367ea3e26bc7c43445bbc52895");
        archiveModel.setStripPrefix("rules_java-d7bf804c8731edd232cb061cb2a9fe003a85d8ee");
        List<String> urlList = new ArrayList<String>();
        urlList.add(
            "https://mirror.bazel.build/github.com/bazelbuild/rules_java/archive/d7bf804c8731edd232cb061cb2a9fe003a85d8ee.tar.gz");
        urlList.add("https://github.com/bazelbuild/rules_java/archive/d7bf804c8731edd232cb061cb2a9fe003a85d8ee.tar.gz");
        archiveModel.setUrls(urlList);
        Multimap<String, String> loadMap = TreeMultimap.create();

        loadMap.put("@rules_java//java:repositories.bzl", "rules_java_dependencies");
        loadMap.put("@rules_java//java:repositories.bzl", "rules_java_toolchains");

        archiveModel.setLoadMap(loadMap);

        List<String> dependenyList = new ArrayList<String>();

        dependenyList.add("rules_java_dependencies()");
        dependenyList.add("rules_java_toolchains()");
        archiveModel.setDependencies(dependenyList);
        return archiveModel.getResult();
    }

    /**
     * 得到rules_proto插件
     *
     * @return
     */
    private static String getRulesProto() {
        ArchiveModel archiveModel = new ArchiveModel();
        archiveModel.setName("rules_proto");
        archiveModel.setSha256("602e7161d9195e50246177e7c55b2f39950a9cf7366f74ed5f22fd45750cd208");
        archiveModel.setStripPrefix("rules_proto-97d8af4dc474595af3900dd85cb3a29ad28cc313");
        List<String> urlList = new ArrayList<String>();
        urlList.add(
            "https://mirror.bazel.build/github.com/bazelbuild/rules_proto/archive/97d8af4dc474595af3900dd85cb3a29ad28cc313.tar.gz");
        urlList.add(
            "https://github.com/bazelbuild/rules_proto/archive/97d8af4dc474595af3900dd85cb3a29ad28cc313.tar.gz");
        archiveModel.setUrls(urlList);
        Multimap<String, String> loadMap = TreeMultimap.create();

        loadMap.put("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies");
        loadMap.put("@rules_proto//proto:repositories.bzl", "rules_proto_toolchains");

        archiveModel.setLoadMap(loadMap);

        List<String> dependenyList = new ArrayList<String>();

        dependenyList.add("rules_proto_dependencies()");
        dependenyList.add("rules_proto_toolchains()");
        archiveModel.setDependencies(dependenyList);
        return archiveModel.getResult();
    }

    /**
     * 得到com_google_protobuf插件
     *
     * @return
     */
    private static String getComGoogleProtobuf() {
        ArchiveModel archiveModel = new ArchiveModel();
        archiveModel.setName("com_google_protobuf");
        archiveModel.setSha256("c96e8f755b278dc885fad43ec6afcb6e8345d2b5eb823ea717229076c17ca779");
        archiveModel.setStripPrefix("protobuf-3.14.x");
        List<String> urlList = new ArrayList<String>();
        urlList.add("https://github.com/protocolbuffers/protobuf/archive/3.14.x.zip");
        archiveModel.setUrls(urlList);
        Multimap<String, String> loadMap = TreeMultimap.create();

        loadMap.put("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps");

        archiveModel.setLoadMap(loadMap);

        List<String> dependenyList = new ArrayList<String>();

        dependenyList.add("protobuf_deps()");
        archiveModel.setDependencies(dependenyList);
        return archiveModel.getResult();
    }

    /**
     * 得到rules_spring插件
     *
     * @return
     */
    private static String getRulesSpring() {
        ArchiveModel archiveModel = new ArchiveModel();
        archiveModel.setName("rules_spring");
        archiveModel.setSha256("9385652bb92d365675d1ca7c963672a8091dc5940a9e307104d3c92e7a789c8e");

        List<String> urlList = new ArrayList<String>();
        urlList.add("https://github.com/salesforce/rules_spring/releases/download/2.1.4/rules-spring-2.1.4.zip");
        archiveModel.setUrls(urlList);
        Multimap<String, String> loadMap = TreeMultimap.create();

        loadMap.put("@rules_spring//springboot:springboot.bzl", "springboot");
        archiveModel.setLoadMap(loadMap);

        return archiveModel.getResult();
    }

    /**
     * 得到war插件
     *
     * @return
     */
    private static String getWar() {
        ArchiveModel archiveModel = new ArchiveModel();
        archiveModel.setName("io_bazel_rules_java_war");
        archiveModel.setSha256("38011f979713c4aefd43ab56675ce4c6c14bc949b128c3a303f1f57ebe4bfeac");
        String RULES_JAVA_WAR_TAG = "0.1.0";
        archiveModel.setStripPrefix("rules_java_war-" + RULES_JAVA_WAR_TAG);

        List<String> urlList = new ArrayList<String>();
        urlList.add("https://github.com/bmuschko/rules_java_war/archive/" + RULES_JAVA_WAR_TAG + ".tar.gz");
        archiveModel.setUrls(urlList);
        Multimap<String, String> loadMap = TreeMultimap.create();

        //		loadMap.put("@rules_spring//springboot:springboot.bzl", "springboot");
        //		archiveModel.setLoadMap(loadMap);

        return archiveModel.getResult();
    }

    /**
     * 得到maven私服
     *
     * @return
     */
    private static String getRepositories(List<String> repositories) {
        StringBuilder sb = new StringBuilder();
        //		sb.append('\n');
        sb.append(Global.FOUR_BLANK_CHARACTER);
        sb.append("repositories = [");

        for (String repo : repositories) {
            sb.append("\n");
            sb.append(Global.FOUR_BLANK_CHARACTER);
            sb.append(Global.FOUR_BLANK_CHARACTER);
            sb.append("\"");
            sb.append(StringUtils.trim(repo));
            sb.append("\"");
            sb.append(",");
        }
        sb.append("\n").append(Global.FOUR_BLANK_CHARACTER).append("],");
        return sb.toString();
    }
}
