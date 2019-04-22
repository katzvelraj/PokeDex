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
import org.junit.Assert.fail
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
    fun getPokemon() {
        runBlocking {
            val testResult = PokemonResponse(count = 10)

            val mockDeferred = mock(Deferred::class.java) as Deferred<PokemonResponse>
            whenever(mockDeferred.await()).thenReturn(testResult)
            whenever(mockAPI.getPokemonAsync()).thenReturn(mockDeferred)

            val result = repository.getPokemon()
            assertEquals(testResult, result)
        }
    }

    @Test
    fun getPokemonError() {
        runBlocking {
            val mockDeferred = mock(Deferred::class.java) as Deferred<PokemonResponse>
            whenever(mockDeferred.await()).thenThrow(java.lang.IllegalArgumentException())
            whenever(mockAPI.getPokemonAsync()).thenReturn(mockDeferred)

            try {
                repository.getPokemon()
                fail("Expected network call to fail.")
            } catch (error: Throwable) {

            }
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
    fun getPokemonDetail() {
        runBlocking {
            val testPokemon = Pokemon(name = "Adam")

            val mockDeferred = mock(Deferred::class.java) as Deferred<Pokemon>
            whenever(mockDeferred.await()).thenReturn(testPokemon)
            whenever(mockAPI.getPokemonDetailAsync(anyString())).thenReturn(mockDeferred)

            val result = repository.getPokemonDetail("")
            assertEquals(testPokemon, result)
        }
    }

    @Test
    fun getPokemonDetailError() {
        runBlocking {
            val mockDeferred = mock(Deferred::class.java) as Deferred<Pokemon>
            whenever(mockDeferred.await()).thenThrow(IllegalArgumentException())
            whenever(mockAPI.getPokemonDetailAsync(anyString())).thenReturn(mockDeferred)

            try {
                repository.getPokemonDetail("")
                fail("Expected network call to fail.")
            } catch (error: Throwable) {

            }
        }
    }
}