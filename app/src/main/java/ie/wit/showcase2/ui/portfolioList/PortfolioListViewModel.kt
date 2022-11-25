package ie.wit.showcase2.ui.portfolioList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.main.Showcase2App
import ie.wit.showcase2.models.PortfolioManager

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
        portfoliosList.value = PortfolioManager.findAll()
    }

    fun delete(email: String, portfolio: PortfolioModel) {
        PortfolioManager.delete(portfolio)
    }
}

