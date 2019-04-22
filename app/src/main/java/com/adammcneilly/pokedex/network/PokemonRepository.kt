package com.adammcneilly.pokedex.network

import com.adammcneilly.pokedex.models.Pokemon
import com.adammcneilly.pokedex.models.PokemonResponse
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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