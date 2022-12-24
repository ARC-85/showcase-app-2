package ie.wit.showcase2.ui.projectList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseUser
import ie.wit.showcase2.firebase.FirebaseDBManager
import ie.wit.showcase2.models.Favourite
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioModel
import timber.log.Timber
import java.lang.Exception

class ProjectListViewModel : ViewModel() {

    private val projectsList =
        MutableLiveData<List<NewProject>>()

    val observableProjectsList: LiveData<List<NewProject>>
        get() = projectsList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    private val portfolio = MutableLiveData<PortfolioModel>()

    var observablePortfolio: LiveData<PortfolioModel>
        get() = portfolio
        set(value) {portfolio.value = value.value}

    private val favouritesList =
        MutableLiveData<List<Favourite>>()

    val observableFavouritesList: LiveData<List<Favourite>>
        get() = favouritesList

    fun load(portfolioid: String) {
        try {
            FirebaseDBManager.findProjects(liveFirebaseUser.value?.uid!!,portfolioid, portfolio, projectsList)
            Timber.i("Report Load Success : ${projectsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }

    fun loadAll() {
        try {
            FirebaseDBManager.findAllProjects(projectsList)
            Timber.i("Report LoadAll Success : ${projectsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report LoadAll Error : $e.message")
        }
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

    fun delete(userid: String, projectid: String, portfolioid: String) {
        try {
            //DonationManager.delete(userid,id)
            FirebaseDBManager.delete(userid,projectid)
            Timber.i("Report Delete Success")
        }
        catch (e: Exception) {
            Timber.i("Report Delete Error : $e.message")
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

    fun removeFavourite(userid: String, projectId: String) {

        try {
            FirebaseDBManager.deleteFavourite(userid, projectId)
            Timber.i("Detail delete() Success : $projectId")
        } catch (e: Exception) {
            Timber.i("Detail delete() Error : $e.message")
        }

    }

    fun loadFavourites() {
        try {
            FirebaseDBManager.findUserUserFavourites(liveFirebaseUser.value?.uid!!,favouritesList)
            Timber.i("Report Load Success : ${favouritesList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }

    fun loadAllFavourites() {
        try {

            FirebaseDBManager.findUserAllFavourites(liveFirebaseUser.value?.uid!!,favouritesList)
            Timber.i("Report LoadAll Success : ${favouritesList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report LoadAll Error : $e.message")
        }
    }
}