package ie.wit.showcase2.ui.projectNew

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.wit.showcase2.main.Showcase2App
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioManager
import ie.wit.showcase2.models.PortfolioModel

class ProjectNewViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

    fun addProject(project: NewProject, id: Long) {
        val portfolio = PortfolioManager.findPortfolioById(id)
        PortfolioManager.createProject(project, portfolio!!)
    }

    fun getPortfolio(email:String, id: Long): PortfolioModel? {
        var updatedPortfolio = PortfolioManager.findPortfolioById(id)
        PortfolioManager.update(updatedPortfolio!!)
        return updatedPortfolio
    }
}