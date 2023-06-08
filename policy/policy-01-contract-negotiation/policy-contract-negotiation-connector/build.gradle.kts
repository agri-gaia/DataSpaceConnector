import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":core:control-plane:control-plane-core"))
    implementation(project(":core:data-plane:data-plane-core"))
    implementation(project(":extensions:data-plane:data-plane-minio"))
    implementation(project(":extensions:control-plane:data-plane-transfer:data-plane-transfer-client"))
    implementation(project(":extensions:data-plane-selector:selector-client"))
    implementation(project(":core:data-plane-selector:data-plane-selector-core"))

    implementation(project(":extensions:common:api:observability"))

    implementation(project(":extensions:common:configuration:filesystem-configuration"))

    implementation(project(":extensions:common:http"))

    implementation(project(":extensions:control-plane:provision:minio-provision"))

    implementation(project(":extensions:common:iam:oauth2:oauth2-core"))
    implementation(project(":extensions:common:vault:filesystem-vault"))
    //implementation(project(":extensions:common:iam:iam-mock"))

    implementation(project(":extensions:common:auth:auth-tokenbased"))
    implementation(project(":extensions:control-plane:api:data-management"))

    implementation(project(":data-protocols:ids"))

    implementation(project(":policy:policy-01-contract-negotiation:policy-contract-negotiation-policy-functions"))

}

application {
    mainClass.set("org.eclipse.dataspaceconnector.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("connector.jar")
    setProperty("zip64", true)
}

