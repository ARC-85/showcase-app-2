package ie.wit.showcase2.ui.favouritesMap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseUser
import ie.wit.showcase2.firebase.FirebaseDBManager
import ie.wit.showcase2.models.Favourite
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioModel
import timber.log.Timber
import java.lang.Exception

class FavouritesMapViewModel : ViewModel() {
    lateinit var map : GoogleMap

    private val favouritesList =
        MutableLiveData<List<Favourite>>()

    val observableFavouritesList: LiveData<List<Favourite>>
        get() = favouritesList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    private val portfoliosList =
        MutableLiveData<List<PortfolioModel>>()

    val observablePortfoliosList: LiveData<List<PortfolioModel>>
        get() = portfoliosList

    //function to load favourites belonging to user
    fun load() {
        try {
            FirebaseDBManager.findUserUserFavourites(liveFirebaseUser.value?.uid!!,favouritesList)
            FirebaseDBManager.findUserAll(liveFirebaseUser.value?.uid!!,portfoliosList)
            Timber.i("Report Load Success : ${favouritesList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }

    //function to load favourites belonging to other users
    fun loadAll() {
        try {

            FirebaseDBManager.findUserAllFavourites(liveFirebaseUser.value?.uid!!,favouritesList)
            Timber.i("Report LoadAll Success : ${favouritesList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report LoadAll Error : $e.message")
        }
    }
}