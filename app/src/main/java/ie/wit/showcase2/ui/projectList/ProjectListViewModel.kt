package ie.wit.showcase2.ui.projectList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseUser
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioManager
import ie.wit.showcase2.models.PortfolioModel

class ProjectListViewModel : ViewModel() {
    private val projectsList =
        MutableLiveData<List<NewProject>>()

    val observableProjectsList: LiveData<List<NewProject>>
        get() = projectsList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()







    /*init {
        load(state["portfolioid"]!!)
    }*/

    fun load(id: Long) {
        val portfolio = PortfolioManager.findPortfolioById(id)
        projectsList.value = portfolio?.projects?.toMutableList()
    }

    fun delete(email: String, project: NewProject, id: Long) {
        val portfolio = PortfolioManager.findPortfolioById(id)
        PortfolioManager.deleteProject(project, portfolio!!)
    }
}