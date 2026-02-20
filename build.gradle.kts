plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

version = "0.4.0"
group = "com.abissell"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.abissell.javautil:javautil:0.11.0")
    api(platform("org.apache.logging.log4j:log4j-bom:2.25.3"))
    api("org.apache.logging.log4j:log4j-api")
    api("org.apache.logging.log4j:log4j-core")
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.javaModuleVersion = provider { version.toString() }
}

tasks.test {
    useJUnitPlatform()
}
