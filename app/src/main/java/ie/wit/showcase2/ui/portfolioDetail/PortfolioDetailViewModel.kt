package ie.wit.showcase2.ui.portfolioDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.wit.showcase2.firebase.FirebaseDBManager
import ie.wit.showcase2.main.Showcase2App
import ie.wit.showcase2.models.PortfolioManager
import ie.wit.showcase2.models.PortfolioModel
import timber.log.Timber

class PortfolioDetailViewModel : ViewModel() {

    private val portfolio = MutableLiveData<PortfolioModel>()

    var observablePortfolio: LiveData<PortfolioModel>
        get() = portfolio
        set(value) {
            portfolio.value = value.value
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

        fun deletePortfolio(userid: String, id: String) {
            try {
                FirebaseDBManager.delete(userid, id)
                Timber.i("Detail delete() Success : $portfolio")
            } catch (e: Exception) {
                Timber.i("Detail delete() Error : $e.message")
            }
        }

}

