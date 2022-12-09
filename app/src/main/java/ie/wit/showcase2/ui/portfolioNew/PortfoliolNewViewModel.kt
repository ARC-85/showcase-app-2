package ie.wit.showcase2.ui.portfolioNew

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.showcase2.firebase.FirebaseDBManager
import ie.wit.showcase2.firebase.FirebaseImageManager
import ie.wit.showcase2.main.Showcase2App
import ie.wit.showcase2.models.PortfolioManager
import ie.wit.showcase2.models.PortfolioModel

class PortfoliolNewViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

    fun addPortfolio(firebaseUser: MutableLiveData<FirebaseUser>, portfolio: PortfolioModel) {
        //PortfolioManager.create(portfolio)
        status.value = try {
            portfolio.image = FirebaseImageManager.imageUriPortfolio.value.toString()
            FirebaseDBManager.create(firebaseUser, portfolio)

            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

}