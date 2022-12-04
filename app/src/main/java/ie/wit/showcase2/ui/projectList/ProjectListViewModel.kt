package ie.wit.showcase2.ui.projectList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseUser
import ie.wit.showcase2.firebase.FirebaseDBManager
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioManager
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

    /*var observablePortfolio: LiveData<PortfolioModel>
        get() = portfolio
        set(value) {portfolio.value = value.value}*/

    fun load(portfolioid: String) {
        try {
            //DonationManager.findAll(liveFirebaseUser.value?.email!!, donationsList)
            FirebaseDBManager.findProjects(liveFirebaseUser.value?.uid!!,portfolioid, portfolio, projectsList)
            Timber.i("Report Load Success : ${projectsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }



    /*init {
        load(state["portfolioid"]!!)
    }*/

    /*fun load(portfolioid: String) {
        try {
            //DonationManager.findAll(liveFirebaseUser.value?.email!!, donationsList)
            FirebaseDBManager.findProjects(liveFirebaseUser.value?.uid!!,projectsList)
            Timber.i("Report Load Success : ${projectsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }*/

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
}