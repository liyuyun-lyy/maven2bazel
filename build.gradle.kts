plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.5.2"
}

group = "com.alibaba.aone"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        url = uri("http://mvnrepo.alibaba-inc.com/mvn/repository")
        isAllowInsecureProtocol = true
    }
    maven {
        url = uri("http://repo.alibaba-inc.com/mvn/snapshots")
        isAllowInsecureProtocol = true
    }
    mavenCentral()
}

buildscript {
    repositories {
        maven {
            url = uri("http://mvnrepo.alibaba-inc.com/mvn/repository")
            isAllowInsecureProtocol = true
        }
        maven {
            url = uri("http://repo.alibaba-inc.com/mvn/snapshots")
            isAllowInsecureProtocol = true
        }
    }
}

dependencies {
    implementation("com.alibaba:fastjson:1.2.78")
    implementation("org.eclipse.aether:aether-connector-basic:1.1.0")
    implementation("org.eclipse.aether:aether-transport-file:1.1.0")
    implementation("org.eclipse.aether:aether-transport-http:1.1.0")
    implementation("org.apache.maven.shared:maven-invoker:3.1.0")
    implementation("cn.hutool:hutool-all:5.7.10")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2021.2")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("212")
        untilBuild.set("222.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
