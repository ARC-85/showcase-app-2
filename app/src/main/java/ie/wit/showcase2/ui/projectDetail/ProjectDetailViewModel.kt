package ie.wit.showcase2.ui.projectDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioManager
import ie.wit.showcase2.models.PortfolioModel

class ProjectDetailViewModel : ViewModel() {
    private val project = MutableLiveData<NewProject>()

    var observableProject: LiveData<NewProject>
        get() = project
        set(value) {project.value = value.value}

    fun getProject(email: String, portfolioId: Long, projectId: Long): NewProject? {
        return PortfolioManager.findProjectById(projectId, portfolioId)
    }

    fun getPortfolio(email:String, id: Long): PortfolioModel? {
        var updatedPortfolio = PortfolioManager.findPortfolioById(id)
        PortfolioManager.update(updatedPortfolio!!)
        return updatedPortfolio
    }

    fun updateProject(email:String, project: NewProject, portfolioId: Long) {
        var updatedPortfolio = PortfolioManager.findPortfolioById(portfolioId)
        PortfolioManager.updateProject(project, updatedPortfolio!!)
    }

    fun deleteProject(email:String, projectId: Long, portfolioId: Long) {
        var deletedProject = PortfolioManager.findProjectById(projectId, portfolioId)
        var deletedPortfolio = PortfolioManager.findPortfolioById(portfolioId)
        PortfolioManager.deleteProject(deletedProject!!, deletedPortfolio!!)
    }
}