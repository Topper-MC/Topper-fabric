plugins {
    id("dev.kikugie.stonecutter")
    id("com.vanniktech.maven.publish") version "0.36.0" apply false
    id("me.modmuss50.mod-publish-plugin") version "1.1.0" apply false // Publishes builds to hosting websites
}

stonecutter active "26.1.2"

stonecutter tasks {
    order("publishModrinth")
}

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String
}
