package com.adammcneilly.pokedex.network

import com.adammcneilly.pokedex.models.Pokemon
import com.adammcneilly.pokedex.models.PokemonResponse
import io.reactivex.subjects.PublishSubject

open class PokemonRepository(
    private val api: PokemonAPI
) {
    val pokemonSpecies = PublishSubject.create<NetworkState>()

    suspend fun getPokemon(): PokemonResponse {
        return api.getPokemonAsync().await()
    }

    suspend fun getPokemonDetail(name: String): Pokemon {
        return api.getPokemonDetailAsync(name).await()
    }
}