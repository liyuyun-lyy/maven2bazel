package com.alibaba.aone.maven2bazel.convert.utils;

import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.SystemOutHandler;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

public class Pom2DependenciesUtil {

    public static void main(String[] args) {
        pom2Dependencies(null, null, null);
    }

    /**
     * 将pom转为dependencies.txt
     *
     * @param pomBaseDirectory 项目pom.xml所在目录
     * @param mavenPath        maven所在路径
     */
    public static void pom2Dependencies(ProgressIndicator indicator, String pomBaseDirectory, String mavenPath) {
        try {

            InvocationRequest request = new DefaultInvocationRequest();
            //设置生成dependency目录
            String dependenciesPath = "dependencies.txt";
            //            pomBaseDirectory = "/Users/liyuyun/Downloads/java/build-buddha";
            mavenPath = "/usr/local/Cellar/maven/3.8.3/libexec";
            request.setBaseDirectory(new File(pomBaseDirectory));
            request.setPomFileName("pom.xml");
            request.setInputStream(null);
            request.setGoals(Arrays.asList("dependency:tree"));
            request.setShowVersion(true);

            Properties properties = new Properties();
            properties.setProperty("outputFile", dependenciesPath); // redirect output to a file
            properties.setProperty("outputAbsoluteArtifactFilename", "false"); // with paths
            properties.setProperty("includeScope", "runtime"); // only runtime (scope compile + runtime)
            //			properties.setProperty("verbose", "false"); // only runtime (scope compile + runtime)
            //			properties.setProperty("includeScope", "compile"); // only runtime (scope compile + runtime)
            // if only interested in scope runtime, you may replace with excludeScope = compile
            request.setProperties(properties);

            //			request.setOutputHandler(new SystemOutHandler());

            Invoker invoker = new DefaultInvoker();
            // the Maven home can be omitted if the "maven.home" system property is set
            invoker.setWorkingDirectory(new File(pomBaseDirectory));
            //            String mavenPath="/usr/local/bin/mvn";
            invoker.setMavenHome(new File(mavenPath));
            //			invoker.setOutputHandler(null); // not interested in Maven output itself
            invoker.setOutputHandler(new SystemOutHandler()); // not interested in Maven output itself
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                ProgressIndicatorPrintUtil.println(indicator, "pom convert to dependencies Failed!", 1.0);
                throw new IllegalStateException("Build failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
