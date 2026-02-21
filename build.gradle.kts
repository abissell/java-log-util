import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    `java-library`
    id("org.gradlex.extra-java-module-info") version "1.14"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

version = "0.9.0"
group = "com.abissell"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

tasks.withType<JavaCompile>().configureEach {
    options.javaModuleVersion = provider { version.toString() }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("com.abissell:javautil:0.13.0")
    api(platform("org.apache.logging.log4j:log4j-bom:2.25.3"))
    api("org.apache.logging.log4j:log4j-api")
    api("org.apache.logging.log4j:log4j-core")
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

extraJavaModuleInfo {
    failOnAutomaticModules = true
    module("biz.aQute.bnd.annotation-7.1.0.jar", "biz.aQute.bnd.annotation")
    module("org.osgi.annotation.bundle-2.0.0.jar", "org.osgi.annotation.bundle")
    module("org.osgi.resource-1.0.0.jar", "org.osgi.resource")
    module("org.osgi.service.serviceloader-1.0.0.jar", "org.osgi.service.serviceloader")
    module("org.osgi.annotation.versioning-1.1.2.jar", "org.osgi.annotation.versioning")
}

mavenPublishing {
    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = SourcesJar.Sources(),
    ))

    publishToMavenCentral()

    signAllPublications()

    coordinates("com.abissell", "logutil", version.toString())

    pom {
        name.set("java-log-util")
        description.set("A set of Java utilities for working with log4j2")
        inceptionYear.set("2023")
        url.set("https://github.com/abissell/java-log-util")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("abissell")
                name.set("Andrew Bissell")
                email.set("abissell@gmail.com")
                url.set("https://www.abissell.com")
            }
        }
        scm {
            connection.set("scm:git:git@github.com:abissell/java-log-util.git")
            developerConnection.set("scm:git:ssh://github.com:abissell/java-log-util.git")
            url.set("https://github.com/abissell/java-log-util/tree/main")
        }
    }
}
