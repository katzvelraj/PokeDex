package com.adammcneilly.pokedex.detail

import androidx.lifecycle.MutableLiveData
import com.adammcneilly.pokedex.BaseObservableViewModel
import com.adammcneilly.pokedex.R
import com.adammcneilly.pokedex.models.Pokemon
import com.adammcneilly.pokedex.models.Species
import com.adammcneilly.pokedex.models.Type
import com.adammcneilly.pokedex.network.NetworkState
import com.adammcneilly.pokedex.network.PokemonRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*

class DetailActivityViewModel(
    private val repository: PokemonRepository,
    private val pokemonName: String
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
        compositeDisposable.add(repository.pokemonState.observeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::processPokemonState))
        compositeDisposable.add(repository.pokemonSpecies.subscribe(this::processSpecies))

        job = CoroutineScope(Dispatchers.IO).launch {
            val pokemon = repository.getPokemonDetail(pokemonName)
            val state = NetworkState.Loaded(pokemon)
            withContext(Dispatchers.Main) {
                processPokemonState(state)
            }
        }
    }

    private fun processPokemonState(networkState: NetworkState) {
        val newState: DetailActivityState = when (networkState) {
            NetworkState.Loading -> currentState.copy(loading = true, pokemon = null, error = null)
            is NetworkState.Loaded<*> -> {
                repository.fetchPokemonSpecies(pokemonName)
                currentState.copy(loading = false, pokemon = networkState.data as? Pokemon, error = null)
            }
            is NetworkState.Error -> currentState.copy(loading = false, pokemon = null, error = networkState.error)
        }

        this.state.value = newState
        notifyChange()
    }

    private fun processSpecies(networkState: NetworkState) {
        val newState: DetailActivityState = when (networkState) {
            is NetworkState.Loaded<*> -> currentState.copy(species = networkState.data as? Species)
            else -> currentState
        }

        this.state.value = newState
        notifyChange()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
        job?.cancel()
    }
}