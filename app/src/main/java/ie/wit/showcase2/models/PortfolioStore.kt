package ie.wit.showcase2.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface PortfolioStore {
    fun findAll(userid: String, portfolioList: MutableLiveData<List<PortfolioModel>>)
    fun findPortfolioById2(userid:String, id: String, portfolio: MutableLiveData<PortfolioModel>): PortfolioModel?
    fun create(firebaseUser: MutableLiveData<FirebaseUser>, portfolio: PortfolioModel)
    fun update(userid:String, portfolioId: String, portfolio: PortfolioModel)
    fun delete(userid:String, portfolioId: String)
    fun createProject(project: NewProject, portfolio: PortfolioModel)
    fun updateProject(project: NewProject, portfolio: PortfolioModel)
    fun deleteProject(project: NewProject, portfolio: PortfolioModel)
    fun findProjects(userid: String, projectList: MutableLiveData<List<NewProject>>)
    fun findProject(id: String): NewProject?
    fun findPortfolio(portfolio: PortfolioModel): PortfolioModel?
    fun findSpecificPortfolios(portfolioType: String): List<PortfolioModel>
    fun findSpecificTypeProjects(portfolioType: String): MutableList<NewProject>
    fun findPortfolioById(userid:String, id: String, portfolio: MutableLiveData<PortfolioModel>)
    fun findProjectById(projectId: String, portfolioId: String): NewProject?
}

