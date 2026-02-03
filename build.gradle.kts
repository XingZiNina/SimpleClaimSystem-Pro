plugins {
    id("java-library")
    id("maven-publish")
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "1.7.1" apply false
}

group = "fr.xyness"
version = "1.4"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://ci.ender.zone/plugin/repository/everything/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven {
        url = uri("https://repo.opencollab.dev/main/")
    }
    maven("https://maven.enginehub.org/repo/")
    maven {
        url = uri("https://repo.mikeprimm.com/")
        content {
            includeGroup("us.dynmap")
        }
    }
    maven("https://repo.bluecolored.de/releases/")
    maven("https://api.modrinth.com/maven/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.2.3-SNAPSHOT")
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("com.mojang:authlib:1.5.21")
    implementation("com.flowpowered:flow-math:1.0.3")
    compileOnly("net.momirealms:craft-engine-core:0.0.67")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.67")
    compileOnly("net.momirealms:craft-engine-adventure:0.0.67")
    compileOnly("xyz.jpenilla", "squaremap-api", "1.3.11")
    compileOnly(files("libs/PlaceholderAPI-2.11.7.jar"))
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.4") {
        exclude(group = "org.bstats", module = "bstats-bukkit")
    }

    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }

    compileOnly(files("libs/Dynmap-3.7-beta-6-spigot.jar"))
    compileOnly("de.bluecolored.bluemap:BlueMapAPI:2.7.2")
    compileOnly("maven.modrinth:pl3xmap:1.21-500")
    compileOnly("com.github.GriefPrevention:GriefPrevention:16.18.2")
    compileOnly("net.md-5:bungeecord-chat:1.21-R0.4")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "fr.xyness.SCS.SimpleClaimSystem"
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("SimpleClaimSystem")

    manifest {
        attributes["Main-Class"] = "fr.xyness.SCS.SimpleClaimSystem"
    }

    dependencies {
        include(dependency("com.zaxxer:HikariCP:7.0.2"))
        include(dependency("org.slf4j:slf4j-api:2.0.9"))
    }

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Xyness/SimpleClaimSystem")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}
