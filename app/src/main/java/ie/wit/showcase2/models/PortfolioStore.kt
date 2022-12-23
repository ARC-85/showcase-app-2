package ie.wit.showcase2.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface PortfolioStore {
    fun findUserAll(userid: String, portfoliosList: MutableLiveData<List<PortfolioModel>>)
    fun create(firebaseUser: MutableLiveData<FirebaseUser>, portfolio: PortfolioModel)
    fun update(userid:String, portfolioId: String, portfolio: PortfolioModel)
    fun delete(userid:String, portfolioId: String)
    fun findProjects(userid: String, portfolioId: String, portfolio: MutableLiveData<PortfolioModel>, projectList: MutableLiveData<List<NewProject>>)
    fun findProject(projectsList: MutableLiveData<List<NewProject>>, projectId: String, project: MutableLiveData<NewProject>)
    fun findPortfolioById(userid:String, id: String, portfolio: MutableLiveData<PortfolioModel>)
    fun findAll(portfoliosList: MutableLiveData<List<PortfolioModel>>)
    fun findUserProjects(userid: String, portfoliosList: MutableLiveData<List<PortfolioModel>>, projectsList: MutableLiveData<List<NewProject>>)
    fun findUserProject(userid: String, portfoliosList: MutableLiveData<List<PortfolioModel>>, projectsList: MutableLiveData<List<NewProject>>, projectId: String, project: MutableLiveData<NewProject>)
    fun findAllProjects(projectsList: MutableLiveData<List<NewProject>>)
    fun createFavourite(firebaseUser: MutableLiveData<FirebaseUser>, favourite: Favourite)
    fun deleteFavourite(userid: String, favouriteId: String)
    fun findAllFavourites(favouritesList: MutableLiveData<List<Favourite>>)
    fun findUserAllFavourites(userid: String, favouritesList: MutableLiveData<List<Favourite>>)
    fun updateFavourite(userid: String, project: NewProject)
    fun findUserUserFavourites(userid: String, favouritesList: MutableLiveData<List<Favourite>>)
}

