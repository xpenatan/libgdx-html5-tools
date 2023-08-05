val moduleName = "gdx-bullet-desktop"

val jarFile = "$projectDir/../gdx-bullet-build/build/c++/desktop/bullet-natives.jar"

tasks.jar {
    from(zipTree(jarFile))
}

tasks.register<Jar>("platformAll") {
    from(zipTree(jarFile))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}