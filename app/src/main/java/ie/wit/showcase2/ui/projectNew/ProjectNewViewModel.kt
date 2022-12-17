package ie.wit.showcase2.ui.projectNew

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseUser
import ie.wit.showcase2.firebase.FirebaseDBManager
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioManager
import ie.wit.showcase2.models.PortfolioModel
import timber.log.Timber

class ProjectNewViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    lateinit var map : GoogleMap

    val observableStatus: LiveData<Boolean>
        get() = status

    private val portfolio = MutableLiveData<PortfolioModel>()

    var observablePortfolio: LiveData<PortfolioModel>
        get() = portfolio
        set(value) {
            portfolio.value = value.value
        }

    fun addProject(userid: String, project: NewProject) {

        //PortfolioManager.create(portfolio)
        status.value = try {
            //DonationManager.create(donation)
            //FirebaseDBManager.create(firebaseUser, project)

            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun getPortfolio(userid: String, id: String) {
        //var currentPortfolio = FirebaseDBManager.findPortfolioById(userid, id, portfolio)
        //println("this is currentportfolio $currentPortfolio")
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
}