// A plugin on the build classpath pulls commons-lang3 3.16.0
// (GHSA-j288-q9x7-2f5v, fixed in 3.18.0). Build-time only; the app already
// resolves 3.20.0. Drop this once the plugin moves past 3.18.
buildscript {
    configurations.classpath {
        resolutionStrategy.force("org.apache.commons:commons-lang3:3.20.0")
    }
}

plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.10.3"
    jacoco
}

group = "com.app.boilerplate"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// Boot 4.1.1 manages Tomcat 11.0.24, which has three critical advisories
// (GHSA-9xv2-5v5q-p794, GHSA-gcx9-497g-6cp6, GHSA-h3x4-894j-xpx5) fixed in
// 11.0.25. Drop this once a Boot release manages 11.0.25 or later.
extra["tomcat.version"] = "11.0.26"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")

    // jjwt-jackson 0.13.0 still depends on the old com.fasterxml.jackson.core:jackson-databind
    // (Jackson 2), which Spring Boot 4 no longer ships (it now ships Jackson 3 under
    // tools.jackson.*). Pulling jjwt-jackson in would drag an unrelated Jackson 2 copy onto
    // the classpath for no benefit, so jjwt-gson is used instead: it depends only on Gson and
    // has no Jackson-version coupling at all.
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-gson:0.13.0")

    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

jacoco {
    toolVersion = "0.8.15"
}

// Coverage floors below are the measured line/branch coverage of the full test suite
// on a clean build (~74.7% line, 55.3% branch), rounded DOWN to the nearest 5%.
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.70".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = "0.55".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

dependencyLocking {
    lockAllConfigurations()
}
