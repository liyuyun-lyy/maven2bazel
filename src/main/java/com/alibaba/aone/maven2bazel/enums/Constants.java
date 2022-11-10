package com.alibaba.aone.maven2bazel.enums;

public class Constants {
    /**
     * bazel的远程maven仓库的对话框弹出类型 add-新加,edit编辑
     */
    public enum RemoteRepositoriesTypeEnums {
        ADD("add"), EDIT("edit");
        public String value;

        private RemoteRepositoriesTypeEnums(String value) {
            this.value = value;
        }
    }

    /**
     * 插件支持的转化类型 pom(企业级父项目),jar(jar包),war(war包)
     */
    public enum PackingTypeEnums {
        POM("pom"), JAR("jar"), WAR("war");
        public String value;

        private PackingTypeEnums(String value) {
            this.value = value;
        }
    }

    /**
     * bazel的WORKSPACE的存储类型 http,git
     */
    public enum WorkspaceArchiveTypeEnums {
        HTTP("http_archive"), GIT("git_archive"), HTTP_JAR("http_jar");
        public String value;

        private WorkspaceArchiveTypeEnums(String value) {
            this.value = value;
        }
    }
}
