plugins {
    id("dev.kikugie.loom-back-compat")
}

val moduleId = property("mod.module_id").toString()

version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = moduleId

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
}

dependencies {
    fun fapi(vararg modules: String) {
        for (it in modules) modImplementation(fabricApi.module(it, property("deps.fabric_api") as String))
    }

    implementation(project(":common"))

    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    fapi("fabric-lifecycle-events-v1", "fabric-networking-api-v1", "fabric-command-api-v2")
    modImplementation("me.lucko:fabric-permissions-api:${property("deps.permissions_api")}")

    modApi(include("eu.pb4:placeholder-api:${property("deps.text_placeholder_api")}")!!)
    modApi(include("io.github.projectunified:faststats-fabric:${property("deps.faststats")}")!!)

    implementation("io.github.miniplaceholders:miniplaceholders-api:${property("deps.mini_placeholders")}")
    modImplementation("net.kyori:adventure-platform-fabric:${property("deps.adventure_fabric")}")
}

loom {
    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true")
        runDir = "../../run"
    }
}

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks {
    processResources {
        inputs.property("id", moduleId)
        inputs.property("name", project.property("mod.name"))
        inputs.property("mc_title", project.property("mod.mc_title"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))

        val props = mapOf(
            "id" to moduleId,
            "name" to "${project.property("mod.name")} for ${project.property("mod.mc_title")}",
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep")
        )

        filesMatching("fabric.mod.json") { expand(props) }
    }

    register<Copy>("collectModule") {
        group = "build"
        description = "Copies the version module jar where the common project nests it"
        from(loomx.modJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("modules"))
    }
}
