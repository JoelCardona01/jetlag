plugins {
    java
}

group = "dev.jetlag"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // We compile against the OLDEST supported API (1.16.5). Anything that
    // compiles here exists on every newer version too, which is what makes
    // one jar work from 1.16.5 to the latest release.
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
}

java {
    // Java 8 bytecode: runs on Java 8 (1.16.5 servers) up to Java 21+ (modern servers).
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(8)
    options.compilerArgs.add("-Xlint:-options") // silence "release 8 is deprecated" on new JDKs
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveBaseName.set("JetLag")
}
