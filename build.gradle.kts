import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "2.0.0"
}

group = "me.mohamad82.pensieve"
version = "1.0"
description = "A well-supported recording and replaying system for Minecraft"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.screamingsandals.org/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.pkg.github.com/mohamad82bz/ruom") {
        credentials {
            username = project.findProperty("gpr.user") as String
            password = project.findProperty("gpr.key") as String
        }
    }
}

dependencies {
    implementation("me.mohamad82:ruom-bukkit:4.17.3")
    implementation("commons-io:commons-io:2.11.0")

    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.intellectualsites.fawe:FAWE-Bukkit:1.16-637")
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("me.clip:placeholderapi:2.11.1")

    testCompileOnly("org.spigotmc:spigot:1.18.1-R0.1-SNAPSHOT:remapped-mojang")
    //testCompileOnly 'org.spigotmc:spigot:1.12.2-R0.1-SNAPSHOT'
}

tasks {
    runServer {
        minecraftVersion("1.20.1")
        serverJar(file("run/purpur-1.20.1-2056.jar"))
    }

    val relocate = task<ConfigureShadowRelocation>("relocateShadowJar") {
        println(actions)
        target = shadowJar.get()
        prefix = "me.mohamad82.pensieve"
    }

    shadowJar {
        val prefix = "me.mohamad82.pensieve"
        relocate("me.mohamad82.ruom", "$prefix.ruom")
        relocate("org.apache", "$prefix.apache")
        relocate("org.patheloper", "$prefix.patheloper")

        //dependsOn(relocate)
        archiveClassifier.set("")
        exclude("META-INF/**")
        minimize()
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
