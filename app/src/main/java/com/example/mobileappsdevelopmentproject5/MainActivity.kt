package com.example.mobileappsdevelopmentproject5

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.mobileappsdevelopmentproject5.databinding.ActivityMainBinding
import okhttp3.Headers
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val client = AsyncHttpClient()
    private val pokemonAdapter = PokemonAdapter()
    private val cache by lazy { getSharedPreferences("project6_pokemon_cache", MODE_PRIVATE) }
    private var nextId = 1
    private var loading = false
    private val lastId = 151

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // LinearLayoutManager arranges the rows as a vertical, scrollable list.
        binding.pokemonRecyclerView.layoutManager = LinearLayoutManager(this)
        pokemonAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.pokemonRecyclerView.adapter = pokemonAdapter

        binding.retryButton.setOnClickListener { loadNextPokemon() }
        loadNextPokemon()
    }

    private fun loadNextPokemon() {
        if (loading || isDestroyed || isFinishing) return
        binding.retryButton.visibility = View.GONE
        binding.loadingIndicator.visibility = View.VISIBLE

        // Rebuild previously downloaded rows from the local cache first.
        // A loop avoids creating a long recursive chain for cached data.
        while (nextId <= lastId) {
            val cached = cache.getString("pokemon_$nextId", null) ?: break
            try {
                val pokemon = Pokemon.fromJson(JSONObject(cached))
                if (pokemon.id != nextId) throw JSONException("Wrong cached ID")
                pokemonAdapter.addPokemon(pokemon)
                nextId++
            } catch (error: JSONException) {
                cache.edit().remove("pokemon_$nextId").apply()
                break
            }
        }

        if (nextId > lastId) {
            binding.loadingIndicator.visibility = View.GONE
            binding.statusText.text = getString(R.string.p6_complete, pokemonAdapter.itemCount)
            return
        }

        binding.statusText.text = getString(
            R.string.p6_loading, pokemonAdapter.itemCount, lastId
        )
        loading = true
        val requestedId = nextId
        val url = "https://pokeapi.co/api/v2/pokemon/$requestedId/"

        // Download one entry at a time without blocking scrolling or other UI work.
        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                if (isDestroyed || isFinishing) return
                try {
                    val data = json.jsonObject ?: throw JSONException("Expected an object")
                    val pokemon = Pokemon.fromJson(data)
                    if (pokemon.id != requestedId) throw JSONException("Unexpected Pokemon ID")

                    // Every displayed row has name, height, and weight before insertion.
                    cache.edit().putString("pokemon_$requestedId", pokemon.toJson().toString())
                        .apply()
                    pokemonAdapter.addPokemon(pokemon)
                    nextId++
                    loading = false
                    loadNextPokemon()
                } catch (error: JSONException) {
                    showLoadError()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String,
                throwable: Throwable?
            ) {
                if (isDestroyed || isFinishing) return
                showLoadError()
            }
        })
    }

    private fun showLoadError() {
        loading = false
        binding.loadingIndicator.visibility = View.GONE
        binding.retryButton.visibility = View.VISIBLE
        binding.statusText.text = getString(
            R.string.p6_error, pokemonAdapter.itemCount, nextId
        )
        // nextId was not advanced, so Retry resumes at the failed request.
    }
}
