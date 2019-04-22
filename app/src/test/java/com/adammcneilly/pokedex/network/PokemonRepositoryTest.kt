package com.adammcneilly.pokedex.network

import com.adammcneilly.pokedex.models.Pokemon
import com.adammcneilly.pokedex.models.PokemonResponse
import com.adammcneilly.pokedex.models.Species
import com.adammcneilly.pokedex.whenever
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock

@Suppress("UNCHECKED_CAST")
class PokemonRepositoryTest {
    private val mockAPI = mock(PokemonAPI::class.java)
    private val repository = PokemonRepository(
        mockAPI,
        CompositeDisposable(),
        Schedulers.trampoline(),
        Schedulers.trampoline()
    )

    @Test
    fun loadPokemonList() {
        val testSub = repository.pokemonResponseState.test()

        whenever(mockAPI.getPokemon()).thenReturn(Single.just(PokemonResponse()))
        repository.fetchPokemon()

        testSub
            .assertValueCount(2)
            .assertValueAt(0) {
                it is NetworkState.Loading
            }
            .assertValueAt(1) {
                it is NetworkState.Loaded<*>
            }
    }

    @Test
    fun loadPokemonSpecies() {
        val testSub = repository.pokemonSpecies.test()

        whenever(mockAPI.getPokemonSpecies(anyString())).thenReturn(Single.just(Species()))
        repository.fetchPokemonSpecies("")

        testSub
            .assertValueCount(2)
            .assertValueAt(0) {
                it is NetworkState.Loading
            }
            .assertValueAt(1) {
                it is NetworkState.Loaded<*>
            }
    }

    @Test
    fun loadingPokemonListError() {
        val testSub = repository.pokemonResponseState.test()

        whenever(mockAPI.getPokemon()).thenReturn(Single.error<PokemonResponse>(Throwable("Whoops")))
        repository.fetchPokemon()

        testSub
            .assertValueCount(2)
            .assertValueAt(0) {
                it is NetworkState.Loading
            }
            .assertValueAt(1) {
                it is NetworkState.Error
            }
    }

    @Test
    fun loadingPokemonSpeciesError() {
        val testSub = repository.pokemonSpecies.test()

        whenever(mockAPI.getPokemonSpecies(anyString())).thenReturn(Single.error<Species>(Throwable("whoops")))
        repository.fetchPokemonSpecies("")

        testSub
            .assertValueCount(2)
            .assertValueAt(0) {
                it is NetworkState.Loading
            }
            .assertValueAt(1) {
                it is NetworkState.Error
            }
    }

    @Test
    fun getPokemonDetailAsync() {
        runBlocking {
            val testPokemon = Pokemon(name = "Adam")

            val mockDeferred = mock(Deferred::class.java) as Deferred<Pokemon>
            whenever(mockDeferred.await()).thenReturn(testPokemon)
            whenever(mockAPI.getPokemonDetailAsync(anyString())).thenReturn(mockDeferred)

            val result = repository.getPokemonDetail("")
            assertEquals(testPokemon, result)
        }
    }
}