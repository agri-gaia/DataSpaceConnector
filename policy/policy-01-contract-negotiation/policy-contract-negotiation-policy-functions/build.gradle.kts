plugins {
    `java-library`
    id("application")
}

val groupId: String by project
val edcVersion: String by project

dependencies {

    implementation(project(":spi:data-plane:data-plane-spi"))

    implementation(project(":core:control-plane:control-plane-core"))

}
