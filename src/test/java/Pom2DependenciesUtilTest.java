import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.alibaba.aone.maven2bazel.convert.utils.Pom2DependenciesUtil.pom2Dependencies;

public class Pom2DependenciesUtilTest {
    @Test
    public void test_pom2Dependencies() {
        String pomBaseDirectory = "/Users/liyuyun/Downloads/java/build-buddha";
        String mavenPath = "/usr/local/Cellar/maven/3.8.3/libexec";
        pom2Dependencies(null, pomBaseDirectory, mavenPath);
        Assert.assertTrue(Files.exists(Path.of(pomBaseDirectory, "dependencies.txt")));
    }
}
