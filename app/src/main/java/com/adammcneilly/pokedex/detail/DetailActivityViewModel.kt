package com.adammcneilly.pokedex.detail

import androidx.lifecycle.MutableLiveData
import com.adammcneilly.pokedex.BaseObservableViewModel
import com.adammcneilly.pokedex.R
import com.adammcneilly.pokedex.models.Pokemon
import com.adammcneilly.pokedex.models.Type
import com.adammcneilly.pokedex.network.PokemonRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DetailActivityViewModel(
    private val repository: PokemonRepository,
    private val pokemonName: String,
    processDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseObservableViewModel() {
    private val compositeDisposable = CompositeDisposable()
    private val state = MutableLiveData<DetailActivityState>()

    private val currentState: DetailActivityState
        get() = state.value ?: DetailActivityState()

    val title: String
        get() = pokemonName.capitalize()

    val toolbarColorRes: Int
        get() = currentState.pokemon?.sortedTypes?.firstOrNull()?.getColorRes() ?: R.color.colorPrimary

    val toolbarTextColorRes: Int
        get() = currentState.pokemon?.sortedTypes?.firstOrNull()?.getComplementaryColorRes() ?: R.color.mds_white

    val imageUrl: String
        get() = currentState.pokemon?.sprites?.frontDefault.orEmpty()

    val showLoading: Boolean
        get() = currentState.loading

    val showData: Boolean
        get() = currentState.pokemon != null

    val showError: Boolean
        get() = currentState.error != null

    val firstType: Type?
        get() = currentState.pokemon?.sortedTypes?.firstOrNull()

    val secondType: Type?
        get() = currentState.pokemon?.sortedTypes?.getOrNull(1)

    val showFirstType: Boolean
        get() = firstType != null

    val showSecondType: Boolean
        get() = secondType != null

    private var job: Job? = null

    init {
        job = CoroutineScope(processDispatcher).launch {
            startLoading()

            @Suppress("TooGenericExceptionCaught")
            try {
                val pokemon = repository.getPokemonDetail(pokemonName)
                processPokemon(pokemon)
            } catch (error: Throwable) {
                handleError(error)
            }
        }
    }

    private fun startLoading() {
        val newState = currentState.copy(loading = true, pokemon = null, error = null)
        postState(newState)
    }

    private fun processPokemon(pokemon: Pokemon) {
        val newState = currentState.copy(loading = false, pokemon = pokemon, error = null)
        postState(newState)
    }

    private fun handleError(error: Throwable) {
        val newState = currentState.copy(loading = false, pokemon = null, error = error)
        postState(newState)
    }

    private fun postState(newState: DetailActivityState) {
        this.state.postValue(newState)
        notifyChange()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
        job?.cancel()
    }
}