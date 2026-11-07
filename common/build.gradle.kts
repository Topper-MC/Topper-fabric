import com.vanniktech.maven.publish.DeploymentValidation
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import org.gradle.api.tasks.bundling.Jar

plugins {
    `java-library`
    id("net.fabricmc.fabric-loom")
    id("com.vanniktech.maven.publish") version "0.36.0"
    id("me.modmuss50.mod-publish-plugin") version "1.1.0"
}

group = rootProject.property("mod.group").toString()
version = rootProject.property("mod.version").toString()
base.archivesName = rootProject.property("mod.id").toString()

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
}

val transitiveInclude: Configuration by configurations.creating

fun DependencyHandler.transitiveApi(
    notation: Any,
    configure: (ExternalModuleDependency.() -> Unit)? = null
) {
    val dep = api(notation)
    if (dep is ExternalModuleDependency && configure != null) {
        dep.configure()
    }
    transitiveInclude.dependencies.add(dep!!)
}

dependencies {
    minecraft("com.mojang:minecraft:26.1")
    compileOnly("net.fabricmc:fabric-loader:${rootProject.property("deps.fabric_loader")}")
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("org.jetbrains:annotations:26.0.2")

    include(api("me.hsgamer:hscore-common:${rootProject.property("deps.hscore")}")!!)
    include(api("me.hsgamer:hscore-builder:${rootProject.property("deps.hscore")}")!!)

    transitiveApi("io.github.projectunified:craftconfig-proxy:${rootProject.property("deps.craftconfig")}")
    transitiveApi("io.github.projectunified:craftconfig-configurate:${rootProject.property("deps.craftconfig")}")

    transitiveApi("io.github.projectunified:craftdatabase-client-java:${rootProject.property("deps.craftdatabase")}")

    transitiveApi("org.spongepowered:configurate-gson:${rootProject.property("deps.configurate")}") {
        exclude("com.google.code.gson")
    }

    transitiveApi("me.hsgamer:topper-query-forward:${rootProject.property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-template-top-player-number:${rootProject.property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-template-storage-supplier:${rootProject.property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-template-snapshot-display-line:${rootProject.property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-storage-flat-converter:${rootProject.property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-storage-flat-properties:${rootProject.property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-storage-sql-converter:${rootProject.property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-storage-sql-config:${rootProject.property("deps.topper")}")

    transitiveApi("me.hsgamer:topper-storage-sql-mysql:${rootProject.property("deps.topper")}") {
        exclude("com.mysql", "mysql-connector-j")
    }
    transitiveApi("me.hsgamer:topper-storage-sql-sqlite:${rootProject.property("deps.topper")}") {
        exclude("org.xerial", "sqlite-jdbc")
    }

    transitiveInclude.resolvedConfiguration.resolvedArtifacts.forEach {
        include(it.moduleVersion.id.toString())
    }

    include(api("io.github.projectunified:faststats-core:${rootProject.property("deps.faststats-core")}")!!)
    include(api("io.github.projectunified:faststats-gson:${rootProject.property("deps.faststats-core")}")!!)
    include(api("io.github.projectunified:faststats-httpclient:${rootProject.property("deps.faststats-core")}")!!)
}

val moduleProjects = rootProject.subprojects.filter { it.path != project.path }
@Suppress("UNCHECKED_CAST")
val mcVersions = rootProject.property("mc_versions") as List<String>

val containerJar = tasks.named<Jar>("jar")

tasks {
    processResources {
        val props = mapOf(
            "id" to rootProject.property("mod.id"),
            "name" to rootProject.property("mod.name"),
            "version" to rootProject.property("mod.version"),
        )
        inputs.properties(props)
        filesMatching("fabric.mod.json") { expand(props) }
    }

    jar {
        from(rootProject.layout.projectDirectory.dir("src/main/resources/assets")) { into("assets") }
    }

    register<Jar>("distJar") {
        group = "build"
        description = "Builds the distributable jar: common code plus one nested module per Minecraft version"
        archiveFileName.set("${rootProject.property("mod.id")}-${rootProject.property("mod.version")}.jar")
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("dist"))
        from(zipTree(containerJar.flatMap { it.archiveFile }))
        dependsOn(moduleProjects.map { "${it.path}:collectModule" })
    }

    register("buildAndCollect") {
        group = "build"
        description = "Builds the distributable jar into build/dist"
        dependsOn("build")
        dependsOn(moduleProjects.map { "${it.path}:build" })
        dependsOn("distJar")
    }

    register("checkCommonPurity") {
        group = "verification"
        description = "Fails when the common code imports Minecraft or Fabric API classes"
        val sources = fileTree("src/main/java") { include("**/*.java") }
        inputs.files(sources)
        doLast {
            val banned = listOf("import net.minecraft.", "import com.mojang.", "import net.fabricmc.fabric.")
            val violations = sources.files.flatMap { file ->
                file.readLines().withIndex()
                    .filter { (_, line) -> banned.any { line.startsWith(it) } }
                    .map { (index, line) -> "${file.path}:${index + 1}: ${line.trim()}" }
            }
            if (violations.isNotEmpty()) {
                throw GradleException("Common code must only use Fabric Loader:\n" + violations.joinToString("\n"))
            }
        }
    }
}

tasks.named("check") {
    dependsOn("checkCommonPurity")
}

loom {
    runConfigs.clear()
    nestJars(
        tasks.named("distJar", Jar::class),
        rootProject.fileTree(rootProject.layout.buildDirectory.dir("modules")) { include("*.jar") }
    )
}

publishMods {
    file.set(tasks.named<Jar>("distJar").flatMap { it.archiveFile })
    displayName = "${rootProject.property("mod.version")} for FabricMC ${mcVersions.first()}-${mcVersions.last()}"
    version = rootProject.property("mod.version").toString()
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null

    modrinth {
        projectId = rootProject.property("publish.modrinth").toString()
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.addAll(mcVersions)
        requires {
            slug = "fabric-api"
        }
        optional {
            slug = "miniplaceholders"
        }
        optional {
            slug = "luckperms"
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.VALIDATED)
    if (gradle.startParameter.taskNames.contains("publishToMavenCentral")) {
        signAllPublications()
    }

    configure(
        JavaLibrary(
            javadocJar = JavadocJar.Javadoc(),
            sourcesJar = com.vanniktech.maven.publish.SourcesJar.Sources(),
        )
    )

    coordinates(
        rootProject.property("mod.group").toString(),
        rootProject.property("mod.id").toString(),
        rootProject.property("mod.version").toString()
    )

    pom {
        name = "Topper Fabric"
        description = "Topper for FabricMC: common code plus one nested module per Minecraft version"
        url = "https://github.com/Topper-MC/Topper-fabric"

        licenses {
            license {
                name = "MIT License"
                url = "https://github.com/Topper-MC/Topper-fabric/blob/master/LICENSE"
            }
        }

        developers {
            developer {
                name = "HSGamer"
                email = "huynhqtienvtag@gmail.com"
                url = "https://github.com/HSGamer"
            }
        }

        issueManagement {
            system = "github"
            url = "https://github.com/Topper-MC/Topper-fabric/issues"
        }

        scm {
            connection = "scm:git:https://github.com/Topper-MC/Topper-fabric.git"
            developerConnection = "scm:git:git@github.com:Topper-MC/Topper-fabric.git"
            url = "https://github.com/Topper-MC/Topper-fabric"
        }
    }
}
