package com.adammcneilly.pokedex.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.adammcneilly.pokedex.BaseObservableViewModel
import com.adammcneilly.pokedex.models.Pokemon
import com.adammcneilly.pokedex.models.PokemonResponse
import com.adammcneilly.pokedex.network.PokemonRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivityViewModel(
    repository: PokemonRepository,
    processDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseObservableViewModel() {
    private val state = MutableLiveData<MainActivityState>()

    private val currentState: MainActivityState
        get() = state.value ?: MainActivityState()

    private var job: Job? = null

    val pokemon: LiveData<List<Pokemon>> = Transformations.map(state) {
        it.data?.results
    }

    val showLoading: Boolean
        get() = currentState.loading

    val showError: Boolean
        get() = currentState.error != null

    val showData: Boolean
        get() = currentState.data != null

    init {
        job = CoroutineScope(processDispatcher).launch {
            startLoading()

            @Suppress("TooGenericExceptionCaught")
            try {
                val response = repository.getPokemon()
                processPokemon(response)
            } catch (error: Throwable) {
                handleError(error)
            }
        }
    }

    private fun startLoading() {
        val newState = currentState.copy(loading = true, data = null, error = null)
        postState(newState)
    }

    private fun processPokemon(response: PokemonResponse) {
        val newState = currentState.copy(loading = false, data = response, error = null)
        postState(newState)
    }

    private fun handleError(error: Throwable) {
        val newState = currentState.copy(loading = false, data = null, error = error)
        postState(newState)
    }

    private fun postState(newState: MainActivityState) {
        this.state.postValue(newState)
        notifyChange()
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}