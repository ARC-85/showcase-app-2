package ie.wit.showcase2.ui.portfolioDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.wit.showcase2.main.Showcase2App
import ie.wit.showcase2.models.PortfolioManager
import ie.wit.showcase2.models.PortfolioModel

class PortfolioDetailViewModel : ViewModel() {

    private val portfolio = MutableLiveData<PortfolioModel>()

    var observablePortfolio: LiveData<PortfolioModel>
        get() = portfolio
        set(value) {portfolio.value = value.value}

    fun getPortfolio(email:String, id: Long): PortfolioModel? {
        var updatedPortfolio = PortfolioManager.findPortfolioById(id)
        PortfolioManager.update(updatedPortfolio!!)
        return updatedPortfolio
    }

    fun updatePortfolio(email:String, id: Long, portfolio: PortfolioModel) {
        var updatedPortfolio = PortfolioManager.findPortfolioById(id)
        PortfolioManager.update(portfolio)
    }

    fun deletePortfolio(email:String, id: Long) {
        var deletedPortfolio = PortfolioManager.findPortfolioById(id)
        PortfolioManager.delete(deletedPortfolio!!)
    }
}

