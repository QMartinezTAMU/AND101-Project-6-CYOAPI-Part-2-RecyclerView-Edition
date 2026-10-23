package com.example.mobileappsdevelopmentproject5

import org.json.JSONException
import org.json.JSONObject

// One item in the RecyclerView. Measurements retain the API's original units.
data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int
) {
    // Cache the fields used by this screen so reopening it avoids repeat downloads.
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("height", height)
        .put("weight", weight)

    companion object {
        fun fromJson(json: JSONObject): Pokemon {
            val pokemon = Pokemon(
                id = json.getInt("id"),
                name = json.getString("name"),
                height = json.getInt("height"),
                weight = json.getInt("weight")
            )
            if (pokemon.id < 1 || pokemon.name.isBlank() ||
                pokemon.height < 0 || pokemon.weight < 0
            ) {
                throw JSONException("Invalid Pokemon data")
            }
            return pokemon
        }
    }
}
