package com.adammcneilly.pokedex.network

import com.adammcneilly.pokedex.models.Pokemon
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

open class PokemonRepository(
    private val api: PokemonAPI,
    private val disposables: CompositeDisposable,
    private val processScheduler: Scheduler = Schedulers.io(),
    private val observerScheduler: Scheduler = AndroidSchedulers.mainThread()
) {
    val pokemonResponseState = PublishSubject.create<NetworkState>()
    val pokemonSpecies = PublishSubject.create<NetworkState>()

    fun fetchPokemon() {
        val subscription = api.getPokemon()
            .subscribeOn(processScheduler)
            .observeOn(observerScheduler)
            .map {
                NetworkState.Loaded(it) as NetworkState
            }
            .doOnSubscribe {
                pokemonResponseState.onNext(NetworkState.Loading)
            }
            .onErrorReturn {
                NetworkState.Error(it)
            }
            .subscribe(pokemonResponseState::onNext)

        disposables.add(subscription)
    }

    suspend fun getPokemonDetail(name: String): Pokemon {
        return api.getPokemonDetailAsync(name).await()
    }

    fun fetchPokemonSpecies(name: String) {
        val subscription = api.getPokemonSpecies(name)
            .subscribeOn(processScheduler)
            .observeOn(observerScheduler)
            .map {
                NetworkState.Loaded(it) as NetworkState
            }
            .doOnSubscribe {
                pokemonSpecies.onNext(NetworkState.Loading)
            }
            .onErrorReturn {
                NetworkState.Error(it)
            }
            .subscribe(pokemonSpecies::onNext)

        disposables.add(subscription)
    }
}