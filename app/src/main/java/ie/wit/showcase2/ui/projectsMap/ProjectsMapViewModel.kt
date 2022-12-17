package ie.wit.showcase2.ui.projectsMap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseUser
import ie.wit.showcase2.firebase.FirebaseDBManager
import ie.wit.showcase2.models.Location
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioModel
import timber.log.Timber
import java.lang.Exception

class ProjectsMapViewModel : ViewModel() {

    lateinit var map : GoogleMap



    private val projectsList =
        MutableLiveData<List<NewProject>>()

    val observableProjectsList: LiveData<List<NewProject>>
        get() = projectsList

    private val portfoliosList =
        MutableLiveData<List<PortfolioModel>>()

    val observablePortfoliosList: LiveData<List<PortfolioModel>>
        get() = portfoliosList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()






    fun load() {
        try {
            //DonationManager.findAll(liveFirebaseUser.value?.email!!, donationsList)

            FirebaseDBManager.findUserAll(liveFirebaseUser.value?.uid!!,portfoliosList)
            Timber.i("Report Load Success : ${portfoliosList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }

    fun loadAll() {
        try {

            FirebaseDBManager.findAll(portfoliosList)
            Timber.i("Report LoadAll Success : ${portfoliosList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report LoadAll Error : $e.message")
        }
    }





}