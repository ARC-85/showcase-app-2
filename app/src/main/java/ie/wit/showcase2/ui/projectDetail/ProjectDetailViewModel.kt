package ie.wit.showcase2.ui.projectDetail

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

class ProjectDetailViewModel : ViewModel() {

    lateinit var map : GoogleMap


    private val portfolio = MutableLiveData<PortfolioModel>()

    var observablePortfolio: LiveData<PortfolioModel>
        get() = portfolio
        set(value) {
            portfolio.value = value.value
        }

    private val favourite = MutableLiveData<Favourite>()

    var observableFavourite: LiveData<Favourite>
        get() = favourite
        set(value) {
            favourite.value = value.value
        }

    fun getPortfolio(userid: String, id: String) {
        try {
            FirebaseDBManager.findPortfolioById(userid, id, portfolio)
            Timber.i(
                "Detail getPortfolio() Success : ${
                    portfolio.value.toString()
                }"
            )
        } catch (e: Exception) {
            Timber.i("Detail getPortfolio() Error : $e.message")
        }
    }

    fun updatePortfolio(userid: String, id: String, portfolio: PortfolioModel) {
        try {
            FirebaseDBManager.update(userid, id, portfolio)
            Timber.i("Detail update() Success : $portfolio")
        } catch (e: Exception) {
            Timber.i("Detail update() Error : $e.message")
        }
    }

    fun addFavourite(firebaseUser: MutableLiveData<FirebaseUser>, favourite: Favourite) {

        try {
            FirebaseDBManager.createFavourite(firebaseUser, favourite)
            Timber.i("Detail update() Success : $favourite")
        } catch (e: Exception) {
            Timber.i("Detail update() Error : $e.message")
        }
    }

    fun removeFavourite(userid: String, projectId: String) {
        try {
            FirebaseDBManager.deleteFavourite(userid, projectId)
            Timber.i("Detail delete() Success : $projectId")
        } catch (e: Exception) {
            Timber.i("Detail delete() Error : $e.message")
        }

    }

    fun updateFavourite(userid: String, project: NewProject) {

        try {
            FirebaseDBManager.updateFavourite(userid, project)
            Timber.i("Detail delete() Success : $project")
        } catch (e: Exception) {
            Timber.i("Detail delete() Error : $e.message")
        }
    }
}