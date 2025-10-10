plugins {
    `maven-publish`
    id("fabric-loom")
//    id("me.modmuss50.mod-publish-plugin")
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
    maven("https://maven.fzzyhmstrs.me/") { name = "FzzyMaven" }
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    maven("https://repo.alessiodp.com/releases/") { name = "AlessioDP" }
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

    fapi("fabric-lifecycle-events-v1", "fabric-networking-api-v1")

    modImplementation("eu.pb4:placeholder-api:${property("deps.text_placeholder_api")}+${stonecutter.current.version}")
    api("io.github.miniplaceholders:miniplaceholders-api:${property("deps.mini_placeholders")}")
    include(implementation("net.byteflux:libby-core:${property("deps.libby")}")!!)

    include(api("me.hsgamer:hscore-common:${property("deps.hscore")}")!!)
    include(api("me.hsgamer:hscore-builder:${property("deps.hscore")}")!!)
    transitiveImpl("me.hsgamer:hscore-config-proxy:${property("deps.hscore")}")
    transitiveImpl("me.hsgamer:hscore-config-configurate:${property("deps.hscore")}")
    transitiveImpl("me.hsgamer:hscore-database-client-java:${property("deps.hscore")}")
    transitiveImpl("org.spongepowered:configurate-gson:${property("deps.configurate")}") {
        exclude("com.google.code.gson") // Use Minecraft's gson
    }

    transitiveImpl("me.hsgamer:topper-template-top-player-number:${property("deps.topper")}")
    transitiveImpl("me.hsgamer:topper-storage-flat-properties:${property("deps.topper")}")
    transitiveImpl("me.hsgamer:topper-storage-sql-config:${property("deps.topper")}")

    transitiveImpl("me.hsgamer:topper-storage-sql-mysql:${property("deps.topper")}") {
        exclude("com.mysql", "mysql-connector-j")
    }
    transitiveImpl("me.hsgamer:topper-storage-sql-sqlite:${property("deps.topper")}") {
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
    withSourcesJar()
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
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

/*
publishMods {
    file = tasks.remapJar.map { it.archiveFile.get() }
    additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile.get() })
    displayName = "${property("mod.name")} ${property("mod.version")} for ${stonecutter.current.version}"
    version = property("mod.version") as String
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null
        || providers.environmentVariable("CURSEFORGE_TOKEN").getOrNull() == null

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.add(stonecutter.current.version)
        requires {
            slug = "fabric-api"
        }
    }

    curseforge {
        projectId = property("publish.curseforge") as String
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        minecraftVersions.add(stonecutter.current.version)
        requires {
            slug = "fabric-api"
        }
    }
}
*/
/*
publishing {
    repositories {
        maven("...") {
            name = "..."
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${property("mod.group")}.${property("mod.id")}"
            artifactId = property("mod.version") as String
            version = stonecutter.current.version

            from(components["java"])
        }
    }
}
*/