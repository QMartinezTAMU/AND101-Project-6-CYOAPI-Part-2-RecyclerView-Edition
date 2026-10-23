package com.example.mobileappsdevelopmentproject5

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileappsdevelopmentproject5.databinding.ItemPokemonBinding
import java.util.Locale

// The adapter connects Pokemon data to the reusable row layout.
class PokemonAdapter : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {
    private val pokemonList = mutableListOf<Pokemon>()

    class PokemonViewHolder(val binding: ItemPokemonBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val binding = ItemPokemonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PokemonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]
        val context = holder.binding.root.context
        with(holder.binding) {
            rowName.text = pokemon.name.replaceFirstChar { it.titlecase(Locale.US) }
            rowId.text = context.getString(R.string.p6_id_format, pokemon.id)
            // Convert decimetres to metres and hectograms to kilograms.
            rowHeight.text = context.getString(R.string.p6_height_format, pokemon.height / 10.0)
            rowWeight.text = context.getString(R.string.p6_weight_format, pokemon.weight / 10.0)
        }
    }

    override fun getItemCount(): Int = pokemonList.size

    fun addPokemon(pokemon: Pokemon) {
        pokemonList.add(pokemon)
        // Only the newly added row needs an update.
        notifyItemInserted(pokemonList.lastIndex)
    }
}
