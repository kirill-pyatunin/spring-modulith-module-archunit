project.description =  "Spring Boot starter for spring-modulith-module-archunit library."

plugins {
    id("java-library")
}

dependencies {
    api(project(":spring-modulith-module-archunit"))

    implementation("org.springframework.boot:spring-boot-autoconfigure")
}