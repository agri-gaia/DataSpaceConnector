plugins {
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {

        create("DependencyRulesPlugin") {
            id = "org.eclipse.dataspaceconnector.dependency-rules"
            implementationClass = "org.eclipse.dataspaceconnector.gradle.DependencyRulesPlugin"
        }

        create("TestSummaryPlugin") {
            id = "org.eclipse.dataspaceconnector.test-summary"
            implementationClass = "org.eclipse.dataspaceconnector.gradle.TestSummaryPlugin"
        }
    }
}