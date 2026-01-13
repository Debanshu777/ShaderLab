package com.debanshu.shaderlab.shaderlib

object ShaderRegistry {
    private val shaders = mutableMapOf<String, ShaderSpec>()
    private val registrationOrder = mutableListOf<String>()

    fun register(spec: ShaderSpec) {
        if (!shaders.containsKey(spec.id)) {
            registrationOrder.add(spec.id)
        }
        shaders[spec.id] = spec
    }

    fun registerAll(vararg specs: ShaderSpec) {
        specs.forEach { register(it) }
    }

    fun getAll(): List<ShaderSpec> {
        return registrationOrder.mapNotNull { shaders[it] }
    }

    fun getById(id: String): ShaderSpec? {
        return shaders[id]
    }

    fun clear() {
        shaders.clear()
        registrationOrder.clear()
    }

    fun contains(id: String): Boolean {
        return shaders.containsKey(id)
    }

    val size: Int
        get() = shaders.size
}

