package com.alibaba.aone.maven2bazel.convert.utils;

import com.alibaba.aone.maven2bazel.state.BazelSettings;
import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MavenDependencyUtils {

    private static BazelSettings bazelSettings = BazelSettings.getInstance();
    private static Map<String, List<Artifact>> dependencyContent2DependencyArtifactMap = new HashMap<>();

    private static List<RemoteRepository> remoteRepositoryList = new ArrayList<>();

    public static List<Artifact> getDependencyArtifact(ProgressIndicator indicator, String dependencyContent) {
        if (StringUtils.isEmpty(dependencyContent)) {
            return null;
        }
        try {
            List<Artifact> artifactList1 = dependencyContent2DependencyArtifactMap.get(dependencyContent);
            if (artifactList1 != null) {
                return artifactList1;
            }
            DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
            RepositorySystem system = newRepositorySystem(locator);
            RepositorySystemSession session = newSession(system);

            //        Artifact artifact = new DefaultArtifact("group.id:artifact.id:version");
            //            Artifact artifact = new DefaultArtifact("com.alibaba:fastjson:1.2.78");
            //            Artifact artifact = new DefaultArtifact("org.flywaydb:flyway-core:7.14.0");
            Artifact artifact = new DefaultArtifact(dependencyContent);
            //"org.flywaydb", "flyway-core", "jar", "7.14.0"
            //            CollectRequest collectRequest = new CollectRequest(new Dependency(artifact, JavaScopes.COMPILE), Arrays.asList(arpaPlubic,central));
            // Test
            RemoteRepository.Builder builder1 =
                new RemoteRepository.Builder("1", "default", "http://mvnrepo.alibaba-inc.com/mvn/repository/");
            RemoteRepository.Builder builder2 =
                new RemoteRepository.Builder("2", "default", "http://repo.alibaba-inc.com/mvn/snapshots/");
            remoteRepositoryList.add(builder1.build());
            remoteRepositoryList.add(builder2.build());
            CollectRequest collectRequest =
                new CollectRequest(new Dependency(artifact, JavaScopes.COMPILE), remoteRepositoryList);
            DependencyFilter filter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

            /*DependencyFilter dependencyFilter = new DependencyFilter() {

                @Override
                public boolean accept(DependencyNode node, List<DependencyNode> parents) {
//							System.out.print(node.getDependency());
                    if (CollectionUtils.isEmpty(parents)) {
                        return false;
                    }
                    if (CollectionUtils.size(parents)>1) {
                        return false;
                    }
                    //排除掉optional
                    Dependency dependency = node.getDependency();
                    if (dependency.isOptional()) {
                        return false;
                    }
//							System.out.println(JSON.toJSONString(node));
                    return true;
                }
            };*/

            DependencyRequest request = new DependencyRequest(collectRequest, filter);
            DependencyResult result = system.resolveDependencies(session, request);
            List<Artifact> artifactList = new ArrayList<>();
            for (ArtifactResult artifactResult : result.getArtifactResults()) {
                Artifact artifact2 = artifactResult.getArtifact();
                if (StringUtils.equals(artifact2.getGroupId(), artifact.getGroupId()) && StringUtils.equals(
                    artifact2.getArtifactId(), artifact.getArtifactId())) {
                    continue;
                }
                //                System.out.println(artifact2.getFile()+",111");
                ProgressIndicatorPrintUtil.println(indicator,
                    artifact2.getGroupId() + "," + artifact2.getArtifactId() + "," + artifact2.getVersion());
                artifactList.add(artifact2);
            }
            dependencyContent2DependencyArtifactMap.put(dependencyContent, artifactList);
            return artifactList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private static RepositorySystem newRepositorySystem(DefaultServiceLocator locator) {
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private static RepositorySystemSession newSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        String localRepository = "/Users/liyuyun/.m2/repository";
        //        String localRepository = bazelSettings.getLocalRepository();

        LocalRepository localRepo = new LocalRepository(StringUtils.trimToEmpty(localRepository));

        session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        return session;
    }
}
