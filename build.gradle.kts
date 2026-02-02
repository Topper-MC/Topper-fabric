import com.vanniktech.maven.publish.DeploymentValidation
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import groovy.util.Node
import groovy.util.NodeList

plugins {
    id("com.vanniktech.maven.publish") version "0.36.0"
    id("fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
}

version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
}

/**
 * Courtesy to jakobkmar for the hack to include transitive dependencies: https://gist.github.com/jakobkmar/3c7e68ff57957d647a37ed568e5068c7
 *
 * To be honest, I don't know why there is no official documentation on the FabricMC wiki to support this.
 * And the way to use shadow to shade the library causes an issue with Fabric Loom (include all runtime artifacts), which I still haven't found any solution to this, nor any official tutorial about this.
 * I find it difficult for any newcomer to transfer knowledge from Maven to Gradle with its nonsense syntax that is called "flexibility" and all the hacks that are likely came out of nowhere.
 * Still, there is no "superior" build tool, just one that one developer finds themselves convenient using.
 */
val transitiveInclude: Configuration by configurations.creating

fun DependencyHandler.transitiveImpl(
    notation: Any,
    configure: (ExternalModuleDependency.() -> Unit)? = null
) {
    val dep = implementation(notation)
    if (dep is ExternalModuleDependency && configure != null) {
        dep.configure()
    }
    transitiveInclude.dependencies.add(dep!!)
}

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
    /**
     * Fetches only the required Fabric API modules to not waste time downloading all of them for each version.
     * @see <a href="https://github.com/FabricMC/fabric">List of Fabric API modules</a>
     */
    fun fapi(vararg modules: String) {
        for (it in modules) modImplementation(fabricApi.module(it, property("deps.fabric_api") as String))
    }

    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings("net.fabricmc:yarn:${property("deps.yarn")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    fapi("fabric-lifecycle-events-v1", "fabric-networking-api-v1", "fabric-command-api-v2")
    modImplementation("me.lucko:fabric-permissions-api:${property("deps.permissions_api")}")

    modApi(include("eu.pb4:placeholder-api:${property("deps.text_placeholder_api")}")!!)
    implementation("io.github.miniplaceholders:miniplaceholders-api:${property("deps.mini_placeholders")}")
    modImplementation("net.kyori:adventure-platform-fabric:${property("deps.adventure_fabric")}")

    include(api("me.hsgamer:hscore-common:${property("deps.hscore")}")!!)
    include(api("me.hsgamer:hscore-builder:${property("deps.hscore")}")!!)
    transitiveApi("me.hsgamer:hscore-config-proxy:${property("deps.hscore")}")
    transitiveApi("me.hsgamer:hscore-config-configurate:${property("deps.hscore")}")
    transitiveApi("me.hsgamer:hscore-database-client-java:${property("deps.hscore")}")
    transitiveApi("org.spongepowered:configurate-gson:${property("deps.configurate")}") {
        exclude("com.google.code.gson") // Use Minecraft's gson
    }

    transitiveApi("me.hsgamer:topper-template-top-player-number:${property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-template-storage-supplier:${property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-storage-flat-converter:${property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-storage-flat-properties:${property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-storage-sql-converter:${property("deps.topper")}")
    transitiveApi("me.hsgamer:topper-storage-sql-config:${property("deps.topper")}")

    transitiveApi("me.hsgamer:topper-storage-sql-mysql:${property("deps.topper")}") {
        exclude("com.mysql", "mysql-connector-j")
    }
    transitiveApi("me.hsgamer:topper-storage-sql-sqlite:${property("deps.topper")}") {
        exclude("org.xerial", "sqlite-jdbc")
    }

    transitiveInclude.resolvedConfiguration.resolvedArtifacts.forEach {
        include(it.moduleVersion.id.toString())
    }
}

loom {
    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}

java {
    val requiresJava21: Boolean = stonecutter.eval(stonecutter.current.version, ">=1.20.6")
    val javaVersion: JavaVersion =
        if (requiresJava21) JavaVersion.VERSION_21
        else JavaVersion.VERSION_17
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion
}

tasks {
    processResources {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep")
        )

        filesMatching("fabric.mod.json") { expand(props) }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

publishMods {
    file = tasks.remapJar.map { it.archiveFile.get() }
    displayName = "${property("mod.version")} for FabricMC ${stonecutter.current.version}"
    version = property("mod.version") as String
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        property("mod.mc_targets").toString().split(" ").forEach { it ->
            minecraftVersions.add(it)
        }
        requires {
            slug = "fabric-api"
        }
        requires {
            slug = "placeholder-api"
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
            sourcesJar = true,
        )
    )

    coordinates("${property("mod.group")}", "${property("mod.id")}", "${property("mod.version")}+${stonecutter.current.version}")

    pom {
        name = "Topper Fabric"
        description = "A FabricMC project to implement Topper"
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

        withXml {
            var root = asNode()
            var dependencies = root["dependencies"] as NodeList
            dependencies.forEach { dependency -> root.remove(dependency as Node?) }
        }
    }
}
