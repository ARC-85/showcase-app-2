package ie.wit.showcase2.ui.portfolioList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.showcase2.firebase.FirebaseDBManager
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.main.Showcase2App
import ie.wit.showcase2.models.PortfolioManager
import timber.log.Timber
import java.lang.Exception

class PortfolioListViewModel : ViewModel() {

    private val portfoliosList =
        MutableLiveData<List<PortfolioModel>>()

    val observablePortfoliosList: LiveData<List<PortfolioModel>>
        get() = portfoliosList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init {
        load()
    }

    fun load() {
        try {
            //DonationManager.findAll(liveFirebaseUser.value?.email!!, donationsList)
            FirebaseDBManager.findAll(liveFirebaseUser.value?.uid!!,portfoliosList)
            Timber.i("Report Load Success : ${portfoliosList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }

    fun delete(userid: String, id: String) {
        try {
            //DonationManager.delete(userid,id)
            FirebaseDBManager.delete(userid,id)
            Timber.i("Report Delete Success")
        }
        catch (e: Exception) {
            Timber.i("Report Delete Error : $e.message")
        }
    }
}

