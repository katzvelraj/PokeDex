package com.adammcneilly.pokedex.detail

import androidx.lifecycle.MutableLiveData
import com.adammcneilly.pokedex.BaseObservableViewModel
import com.adammcneilly.pokedex.R
import com.adammcneilly.pokedex.models.Pokemon
import com.adammcneilly.pokedex.models.Type
import com.adammcneilly.pokedex.network.NetworkState
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
            processPokemonState(NetworkState.Loading)

            @Suppress("TooGenericExceptionCaught")
            try {
                val pokemon = repository.getPokemonDetail("blahblah")
                val state = NetworkState.Loaded(pokemon)
                processPokemonState(state)
            } catch (error: Throwable) {
                processPokemonState(NetworkState.Error(error))
            }
        }
    }

    private fun processPokemonState(networkState: NetworkState) {
        val newState: DetailActivityState = when (networkState) {
            NetworkState.Loading -> currentState.copy(loading = true, pokemon = null, error = null)
            is NetworkState.Loaded<*> -> {
                currentState.copy(loading = false, pokemon = networkState.data as? Pokemon, error = null)
            }
            is NetworkState.Error -> currentState.copy(loading = false, pokemon = null, error = networkState.error)
        }

        this.state.postValue(newState)
        notifyChange()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
        job?.cancel()
    }
}