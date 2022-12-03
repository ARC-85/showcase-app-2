package ie.wit.showcase2.ui.projectDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.wit.showcase2.firebase.FirebaseDBManager
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioManager
import ie.wit.showcase2.models.PortfolioModel
import timber.log.Timber

class ProjectDetailViewModel : ViewModel() {
    private val project = MutableLiveData<NewProject>()

    var observableProject: LiveData<NewProject>
        get() = project
        set(value) {project.value = value.value}

    private val portfolio = MutableLiveData<PortfolioModel>()

    var observablePortfolio: LiveData<PortfolioModel>
        get() = portfolio
        set(value) {portfolio.value = value.value}

    private lateinit var currentPortfolio : PortfolioModel

    fun getProject(email: String, portfolioId: String, projectId: String): NewProject? {
        return PortfolioManager.findProjectById(projectId, portfolioId)
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

    /*fun updateProject(userid:String, project: NewProject, portfolioId: String) {
        var updatedPortfolio = PortfolioManager.findPortfolioById(userid, portfolioId, portfolio)
        PortfolioManager.updateProject(project, updatedPortfolio!!)
    }

    fun deleteProject(userid:String, projectId: String, portfolioId: String) {
        var deletedProject = PortfolioManager.findProjectById(projectId, portfolioId)
        println("this is deleted project $deletedProject")
        var deletedPortfolio = PortfolioManager.findPortfolioById(userid, portfolioId, portfolio)
        println("this is deleted project portfolio $deletedPortfolio")
        PortfolioManager.deleteProject(deletedProject!!, deletedPortfolio!!)
    }*/
}