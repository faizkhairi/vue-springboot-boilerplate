plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.app.boilerplate"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Generate OpenAPI spec for frontend API client generation
tasks.register<JavaExec>("generateOpenApiDocs") {
    group = "documentation"
    description = "Generates OpenAPI JSON specification for API client generation"

    dependsOn("classes")
    mainClass.set("org.springframework.boot.SpringApplication")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf(
        "--spring.profiles.active=openapi",
        "--server.port=0",
        "--springdoc.api-docs.path=/v3/api-docs",
        "--springdoc.api-docs.enabled=true"
    )

    doLast {
        val openApiUrl = "http://localhost:8080/v3/api-docs"
        val outputFile = file("${layout.buildDirectory.get()}/openapi.json")
        println("OpenAPI spec would be fetched from: $openApiUrl")
        println("Output file: $outputFile")
        println("\nTo generate the spec:")
        println("1. Start the application: ./gradlew bootRun")
        println("2. Fetch spec: curl http://localhost:8080/v3/api-docs -o build/openapi.json")
        println("3. Generate client: cd ../packages/api-client && npm run generate")
    }
}
