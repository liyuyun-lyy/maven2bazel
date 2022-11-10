package com.alibaba.aone.maven2bazel.convert.pattern;

import com.alibaba.aone.maven2bazel.convert.global.Global;
import com.alibaba.aone.maven2bazel.convert.pattern.model.MavenDependencyModel;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyPattern {

    public static void main(String[] args) {
        String pomBaseDirectory = "/Users/liyuyun/Downloads/java/build-buddha";
        Map<Integer, MavenDependencyModel> workSpaceAndBuild =
            getLineNumber2MavenDependencyModelMap(null, null, pomBaseDirectory);
        System.out.println(JSON.toJSONString(workSpaceAndBuild));
    }

    /**
     * 获取行数->Maven依赖映射
     *
     * @param childName2ModelMap
     * @param pomBaseDirectory
     * @return
     */
    public static Map<Integer, MavenDependencyModel> getLineNumber2MavenDependencyModelMap(ProgressIndicator indicator,
        Map<String, Model> childName2ModelMap, String pomBaseDirectory) {

        try {
            File file = new File(pomBaseDirectory + "/dependencies.txt");
            List<String> lines = FileUtils.readLines(file, Charset.forName("UTF-8"));
            Pattern pattern = Pattern.compile("([\\|\\s\\+-\\\\]*)(.*):(compile|runtime|test|provided)");

            //行号对应的
            Map<Integer, MavenDependencyModel> lineNumber2DependencyMap = new HashMap<Integer, MavenDependencyModel>();
            //层次
            Multimap<Integer, MavenDependencyModel> level2DependencyMap = LinkedHashMultimap.create();
            int maxLineNumber = 0;
            //组装为树状结构数字结构
            //获取 spring-boot-starter的版本
            String springbootStarterVersion = "";

            //判断是否有lombok
            MavenDependencyModel lombokDependency = null;

            for (int i = 1; i < lines.size(); i++) {
                Matcher matcher = pattern.matcher(lines.get(i));
                boolean find = matcher.find();
                if (!find) {
                    continue;
                }
                //group(1) 为前缀分隔;
                //group(2)为maven依赖groupId:artifactId:version
                //group(3)为scope，分为 compile,runtime,test
                Integer lineNumber = i;
                maxLineNumber = lineNumber;
                String prefix = matcher.group(1);
                Integer level = StringUtils.length(prefix) / 3;

                String mavenDependency = matcher.group(2);

                Pattern dependencyPattern = Pattern.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)");

                Matcher dependencyMatcher = dependencyPattern.matcher(mavenDependency);
                if (!dependencyMatcher.find()) {
                    continue;
                }
                String groupId = dependencyMatcher.group(1);
                String artifactId = dependencyMatcher.group(2);
                String type = get(dependencyMatcher.group(4), "jar");
                String classifier = get(dependencyMatcher.group(6), "");
                String version = dependencyMatcher.group(7);

                String scope = StringUtils.trim(matcher.group(3));

                MavenDependencyModel mavenDependencyModel = new MavenDependencyModel();
                mavenDependencyModel.setLineNumber(lineNumber);
                mavenDependencyModel.setLevel(level);
                mavenDependencyModel.setPrefix(prefix);

                mavenDependencyModel.setGroupId(groupId);
                mavenDependencyModel.setArtifactId(artifactId);
                mavenDependencyModel.setType(type);
                mavenDependencyModel.setVersion(version);
                mavenDependencyModel.setScope(scope);

                mavenDependencyModel.setDependencyContent(mavenDependency);

                if (StringUtils.equals(groupId, "org.springframework.boot") && StringUtils.equals(artifactId,
                    "spring-boot-starter")) {
                    springbootStarterVersion = version;
                }

                if (StringUtils.equals(groupId, "org.projectlombok") && StringUtils.equals(artifactId, "lombok")) {
                    lombokDependency = mavenDependencyModel;
                }

                lineNumber2DependencyMap.put(lineNumber, mavenDependencyModel);
                level2DependencyMap.put(level, mavenDependencyModel);
            }

            if (StringUtils.isNotBlank(springbootStarterVersion)) {
                Integer lineNumber = -100;
                MavenDependencyModel mavenDependencyModel = new MavenDependencyModel();
                mavenDependencyModel.setLineNumber(lineNumber);
                mavenDependencyModel.setLevel(1);
                mavenDependencyModel.setPrefix("---");

                mavenDependencyModel.setGroupId("org.springframework.boot");
                mavenDependencyModel.setArtifactId("spring-boot-loader");
                mavenDependencyModel.setType("jar");
                mavenDependencyModel.setVersion(springbootStarterVersion);
                mavenDependencyModel.setScope("compile");
                lineNumber2DependencyMap.put(lineNumber, mavenDependencyModel);
                level2DependencyMap.put(1, mavenDependencyModel);
            }

            //			System.out.println(JSON.toJSONString(lineNumber2DependencyMap));
            //			System.out.println(JSON.toJSONString(level2DependencyMap));

            //遍历每一层 得到每一层的数据
            //            for (int i = 0; i < level2DependencyMap.size(); i++) {
            //                Collection<MavenDependencyModel> firstLevelList = level2DependencyMap.get(i);
            //                if (CollectionUtils.isEmpty(firstLevelList)) {
            //                    continue;
            //                }
            //                Iterator<MavenDependencyModel> iterator = firstLevelList.iterator();
            //                List<MavenDependencyModel> list = new ArrayList<MavenDependencyModel>();
            //                //遍历两个之间的值
            //                while (iterator.hasNext()) {
            //                    MavenDependencyModel mavenDependencyModel = iterator.next();
            //                    list.add(mavenDependencyModel);
            //                }
            //                for (int j = 0; j < list.size(); j++) {
            //                    MavenDependencyModel mavenDependencyModel = list.get(j);
            //                    //包含开始子节点
            //                    int startChildLineNumber = mavenDependencyModel.getLineNumber() + 1;
            //                    //包含结束子节点
            //                    int endChildLineNumber = 0;
            //                    if (j == (list.size() - 1)) {
            //                        endChildLineNumber = maxLineNumber;
            //                    } else {
            //                        endChildLineNumber = list.get(j + 1).getLineNumber() - 1;
            //                    }
            //                    //查找直接子节点
            //                    Collection<MavenDependencyModel> chilDependencyModels = level2DependencyMap.get(i + 1);
            //                    if (CollectionUtils.isNotEmpty(chilDependencyModels)) {
            //                        //查找位于[startChildLineNumber,endChildLineNumber]之间的值
            //                        Iterator<MavenDependencyModel> childIterator = chilDependencyModels.iterator();
            //                        while (childIterator.hasNext()) {
            //                            MavenDependencyModel childMavenModel = childIterator.next();
            //                            if (childMavenModel.getLineNumber() < startChildLineNumber) {
            //                                continue;
            //                            }
            //
            //                            if (childMavenModel.getLineNumber() > endChildLineNumber) {
            //                                continue;
            //                            }
            //                            List<MavenDependencyModel> children = mavenDependencyModel.getChildren();
            //                            if (CollectionUtils.isEmpty(children)) {
            //                                children = new ArrayList<MavenDependencyModel>();
            //                            }
            //                            children.add(childMavenModel);
            //                            mavenDependencyModel.setChildren(children);
            //                        }
            //                        //查询出maven依赖原本的child的，与此时的children,计算出children
            //                        String groupId = mavenDependencyModel.getGroupId();
            //                        String artifactId = mavenDependencyModel.getArtifactId();
            //                        String extension = mavenDependencyModel.getType();
            //                        String version = mavenDependencyModel.getVersion();
            //                        //						List<Artifact> oldCompileList = MavenDependencyUtils.getChildDependency(groupId,artifactId,extension,version);
            //
            //                        if (MapUtils.isNotEmpty(childName2ModelMap)) {
            //                            //若groupId与artifactId相等 则直接返回路径
            //                            for (String name : childName2ModelMap.keySet()) {
            //                                Model model = childName2ModelMap.get(name);
            //                                if (StringUtils.equals(StringUtils.trim(model.getGroupId()), StringUtils.trim(groupId))
            //                                    && StringUtils.equals(StringUtils.trim(model.getArtifactId()),
            //                                    StringUtils.trim(artifactId))) {
            //                                    continue;
            //                                }
            //                            }
            //                        }
            //                        List<Artifact> oldCompileList = MavenDependencyUtils.getDependencyArtifact(indicator,
            //                            mavenDependencyModel.getDependencyContent());
            //                        List<MavenDependencyModel> nowChildren = mavenDependencyModel.getChildren();
            //                        if (CollectionUtils.isEmpty(nowChildren)) {
            //                            nowChildren = new ArrayList<MavenDependencyModel>();
            //                        }
            //                        //过滤掉oldCompileList有而childMavenModel.getChildren()没有的
            //                        if (CollectionUtils.isNotEmpty(oldCompileList)) {
            //                            for (Artifact artifact : oldCompileList) {
            //                                String exclusionsGroupId = StringUtils.trim(artifact.getGroupId());
            //                                String exclusionsArtifactId = StringUtils.trim(artifact.getArtifactId());
            //                                String exclusionsVersion = StringUtils.trim(artifact.getVersion());
            //                                //								System.out.println("exclusionsGroupId:"+exclusionsGroupId+",exclusionsArtifactId:"+exclusionsArtifactId+",mavenDependencyModel:"+JSON.toJSONString(mavenDependencyModel));
            //                                //								System.out.println("exclusionsGroupId:"+exclusionsGroupId+",exclusionsArtifactId:"+exclusionsArtifactId);
            //                                boolean isExsitCompile = false;
            //                                for (MavenDependencyModel mavenDependencyModel2 : nowChildren) {
            //                                    //groupId与artifact 一致说明是一致的,否则
            //                                    if (StringUtils.equals(exclusionsGroupId,
            //                                        StringUtils.trim(mavenDependencyModel2.getGroupId())) && StringUtils.equals(
            //                                        exclusionsArtifactId, StringUtils.trim(mavenDependencyModel2.getArtifactId()))
            //                                        && StringUtils.equals(exclusionsVersion,
            //                                        StringUtils.trim(mavenDependencyModel2.getVersion()))) {
            //                                        isExsitCompile = true;
            //                                    }
            //                                }
            //                                //去掉已经存在的并且版本相同的数据
            //                                for (Integer lineNumber : lineNumber2DependencyMap.keySet()) {
            //                                    MavenDependencyModel mavenDependencyModel2 =
            //                                        lineNumber2DependencyMap.get(lineNumber);
            //                                    //groupId与artifact 一致说明是一致的,否则
            //                                    if (StringUtils.equals(exclusionsGroupId,
            //                                        StringUtils.trim(mavenDependencyModel2.getGroupId())) && StringUtils.equals(
            //                                        exclusionsArtifactId, StringUtils.trim(mavenDependencyModel2.getArtifactId()))
            //                                        && StringUtils.equals(StringUtils.trim(artifact.getVersion()),
            //                                        StringUtils.trim(mavenDependencyModel2.getVersion()))) {
            //                                        isExsitCompile = true;
            //                                    }
            //                                }
            //                                //若不存在 则加入exclusions列表中
            //                                if (!isExsitCompile) {
            //                                    List<MavenDependencyModel> exclusions = mavenDependencyModel.getExclusions();
            //                                    if (CollectionUtils.isEmpty(exclusions)) {
            //                                        exclusions = new ArrayList<MavenDependencyModel>();
            //                                    }
            //                                    MavenDependencyModel exclusionsMavenDependencyModel = new MavenDependencyModel();
            //                                    exclusionsMavenDependencyModel.setGroupId(exclusionsGroupId);
            //                                    exclusionsMavenDependencyModel.setArtifactId(exclusionsArtifactId);
            //                                    exclusions.add(exclusionsMavenDependencyModel);
            //                                    mavenDependencyModel.setExclusions(exclusions);
            //
            //                                }
            //                            }
            //                            //此时记录mavenDependencyModel数据
            //                            //                            List<MavenDependencyModel> exclusions = mavenDependencyModel.getExclusions();
            //                            //若不为空则记录此mavenDependencyModel
            //                        }
            //                    }
            //                }
            //            }

            return lineNumber2DependencyMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * maven.artifact("org.mybatis.spring.boot", "mybatis-spring-boot-starter", "2.1.4"),
     * 生成Workpace文件
     *
     * @return
     */
    public static String getWorkSpace(Map<String, Model> childName2ModelMap,
        MavenDependencyModel mavenDependencyModel) {

        String groupId = mavenDependencyModel.getGroupId();
        String artifactId = mavenDependencyModel.getArtifactId();
        String version = mavenDependencyModel.getVersion();
        String scope = mavenDependencyModel.getScope();
        List<MavenDependencyModel> exclusionList = mavenDependencyModel.getExclusions();
        //判断scope的范围
        //若是runtime不需要打到包里
        if (StringUtils.equals(scope, "runtime")) {
            return StringUtils.EMPTY;
        }

        if (MapUtils.isNotEmpty(childName2ModelMap)) {
            //若groupId与artifactId相等 则直接返回空
            for (String name : childName2ModelMap.keySet()) {
                Model model = childName2ModelMap.get(name);
                if (StringUtils.equals(StringUtils.trim(model.getGroupId()), StringUtils.trim(groupId))
                    && StringUtils.equals(StringUtils.trim(model.getArtifactId()), StringUtils.trim(artifactId))) {
                    return StringUtils.EMPTY;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("maven.artifact(");
        if (CollectionUtils.isEmpty(exclusionList)) {
            if (!StringUtils.equals(scope, "provided")) {
                sb.append("\"").append(StringUtils.trim(groupId)).append("\"").append(", ").append("\"")
                    .append(StringUtils.trim(artifactId)).append("\"").append(", ").append("\"")
                    .append(StringUtils.trim(version)).append("\"");
                //只在编译期有作用不会打到包里
            } else {
                sb.append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                    .append(Global.FOUR_BLANK_CHARACTER).append("\"").append(StringUtils.trim(groupId)).append("\"")
                    .append(",").append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                    .append(Global.FOUR_BLANK_CHARACTER).append("\"").append(StringUtils.trim(artifactId)).append("\"")
                    .append(",").append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                    .append(Global.FOUR_BLANK_CHARACTER).append("\"").append(StringUtils.trim(version)).append("\"")
                    .append(",").append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                    .append(Global.FOUR_BLANK_CHARACTER).append("neverlink = True,");
                sb.append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER);
                ;
            }

            sb.append(")").append(",");
        } else {
            sb.append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                .append(Global.FOUR_BLANK_CHARACTER).append("\"").append(StringUtils.trim(groupId)).append("\"")
                .append(",").append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                .append(Global.FOUR_BLANK_CHARACTER).append("\"").append(StringUtils.trim(artifactId)).append("\"")
                .append(",").append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                .append(Global.FOUR_BLANK_CHARACTER).append("\"").append(StringUtils.trim(version)).append("\"");
            sb.append(",").append('\n').append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                .append(Global.FOUR_BLANK_CHARACTER).append("exclusions = [");
            StringBuilder exclusionBuilder = new StringBuilder();
            for (MavenDependencyModel exclusionMavenDependencyModel : exclusionList) {
                //				if (StringUtils.isNotBlank(exclusionBuilder.toString())) {
                //					exclusionBuilder.append(",");
                //				}
                exclusionBuilder.append('\n').append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                    .append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER).append("maven.exclusion(")
                    .append('\n').append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                    .append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                    .append(Global.FOUR_BLANK_CHARACTER).append("group = ").append("\"")
                    .append(exclusionMavenDependencyModel.getGroupId()).append("\"").append(",").append('\n')
                    .append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                    .append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                    .append(Global.FOUR_BLANK_CHARACTER).append("artifact = ").append("\"")
                    .append(exclusionMavenDependencyModel.getArtifactId()).append("\"").append(",").append("\n")
                    .append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                    .append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER).append("),");
                ;
            }
            sb.append(exclusionBuilder.toString());
            sb.append('\n');
            sb.append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                .append(Global.FOUR_BLANK_CHARACTER).append("]");
            if (StringUtils.equals(scope, "provided")) {
                sb.append(",").append("\n");
                sb.append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER)
                    .append(Global.FOUR_BLANK_CHARACTER);
                sb.append("neverlink = True");
            }

            sb.append(",").append('\n');
            sb.append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER).append(")").append(",");
        }
        return sb.toString();
    }

    private static String get(String value, String defaultValue) {
        return value != null && value.length() > 0 ? value : defaultValue;
    }
}
