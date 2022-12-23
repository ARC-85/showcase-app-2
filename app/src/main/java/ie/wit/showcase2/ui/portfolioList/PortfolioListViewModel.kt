package ie.wit.showcase2.ui.portfolioList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.showcase2.firebase.FirebaseDBManager
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.main.Showcase2App

import timber.log.Timber
import java.lang.Exception

class PortfolioListViewModel : ViewModel() {

    private val portfoliosList =
        MutableLiveData<List<PortfolioModel>>()

    val observablePortfoliosList: LiveData<List<PortfolioModel>>
        get() = portfoliosList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    var readOnly = MutableLiveData(false)

    init {
        load()
    }

    fun load() {
        try {
            readOnly.value = false
            FirebaseDBManager.findUserAll(liveFirebaseUser.value?.uid!!,portfoliosList)
            Timber.i("Report Load Success : ${portfoliosList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }

    fun loadAll() {
        try {
            readOnly.value = true
            FirebaseDBManager.findAll(portfoliosList)
            Timber.i("Report LoadAll Success : ${portfoliosList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report LoadAll Error : $e.message")
        }
    }

    fun delete(userid: String, id: String) {
        try {
            FirebaseDBManager.delete(userid,id)
            Timber.i("Report Delete Success")
        }
        catch (e: Exception) {
            Timber.i("Report Delete Error : $e.message")
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
}

