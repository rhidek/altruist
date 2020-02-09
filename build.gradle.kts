import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val spockVersion: String by project

plugins {
    groovy
    idea
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
}

group = "com.altruist"
version = "0.0.1-SNAPSHOT"

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(KotlinCompile::class.java).all {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        javaParameters = true
        jvmTarget = "1.8"
    }
}

tasks.withType(Test::class.java).all {
    this.include("**/*Test.*")
    this.exclude("**/*TestBase.*", "**/*IntegrationTest.*")
}

tasks.withType<Wrapper> {
    gradleVersion = "5.6.4"
    distributionType = Wrapper.DistributionType.ALL
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://repo.spring.io/milestone")
}

dependencies {
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
//    implementation("org.springframework.boot:spring-boot-starter-data-jpa")


    testImplementation("org.codehaus.groovy:groovy-all:2.5.8")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.spockframework:spock-core:$spockVersion")
    testImplementation("org.spockframework:spock-spring:$spockVersion")
}
