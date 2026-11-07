plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String
}

val versionOrder = Comparator<String> { a, b ->
    val pa = a.split(".").map { it.toIntOrNull() ?: Int.MAX_VALUE }
    val pb = b.split(".").map { it.toIntOrNull() ?: Int.MAX_VALUE }
    for (i in 0 until maxOf(pa.size, pb.size)) {
        val result = (pa.getOrNull(i) ?: 0).compareTo(pb.getOrNull(i) ?: 0)
        if (result != 0) return@Comparator result
    }
    0
}

val mcVersions = stonecutter.tree.nodes
    .flatMap { node ->
        val targets = node.project.findProperty("mod.mc_targets") as String?
            ?: error("Missing mod.mc_targets for Stonecutter node ${node.metadata.version}")
        targets.split(" ")
    }
    .filter(String::isNotBlank)
    .distinct()
    .sortedWith(versionOrder)

require(mcVersions.isNotEmpty()) { "No Minecraft versions found in mod.mc_targets" }

extra["mc_versions"] = mcVersions

tasks.register<Delete>("clean") {
    group = "build"
    description = "Deletes the root build directory."
    delete(layout.buildDirectory)
}
