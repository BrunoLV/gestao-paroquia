plugins {
    alias(libs.plugins.sonarqube)
}

sonarqube {
    properties {
        property("sonar.projectKey", "calendario-paroquia")
        property("sonar.projectName", "Calendario Paroquia")
    }
}
