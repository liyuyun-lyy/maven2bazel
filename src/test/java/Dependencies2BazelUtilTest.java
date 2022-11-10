import org.junit.Test;

import static com.alibaba.aone.maven2bazel.convert.utils.Dependencies2BazelUtil.dependency2Bazel;

public class Dependencies2BazelUtilTest {
    @Test
    public void test_dependency2Bazel() {
        String pomBaseDirectory = "/Users/liyuyun/Downloads/java/build-buddha";
        dependency2Bazel(null, pomBaseDirectory);
    }
}
