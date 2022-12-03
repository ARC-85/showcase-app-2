package ie.wit.showcase2.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioManager
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.models.PortfolioStore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList


// Function for generating random ID numbers
internal fun generateRandomId(): Long {
    return Random().nextLong()
}

object FirebaseDBManager : PortfolioStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    var portfolios = mutableListOf<PortfolioModel>()

    // Function for finding all portfolios on portfolio JSON file
    override fun findAll(userid: String, portfoliosList: MutableLiveData<List<PortfolioModel>>) {
        database.child("user-portfolios").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Portfolio error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<PortfolioModel>()
                    val children = snapshot.children
                    children.forEach {
                        val portfolio = it.getValue(PortfolioModel::class.java)
                        localList.add(portfolio!!)
                    }
                    database.child("user-portfolios").child(userid)
                        .removeEventListener(this)

                    portfoliosList.value = localList
                }
            })

    }

    // Function for finding all projects on portfolio JSON file
    override fun findProjects(userid: String, projectsList: MutableLiveData<List<NewProject>>) {
        database.child("user-portfolios").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Project error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<NewProject>()
                    val children = snapshot.children
                    children.forEach {
                        val project = it.getValue(NewProject::class.java)
                        localList.add(project!!)
                    }
                    database.child("user-projects").child(userid)
                        .removeEventListener(this)

                    projectsList.value = localList
                }
            })

    }

    // Function for finding individual project on portfolio JSON file, using passed project ID
    override fun findProject(id: String): NewProject? {
        logProjects()
        return projects.find { p -> p.projectId == id }
    }

    // Function for finding individual portfolio on portfolio JSON file, using passed portfolio
    override fun findPortfolio(portfolio: PortfolioModel): PortfolioModel? {
        logAll()
        return PortfolioManager.portfolios.find { p -> p.uid == portfolio.uid }
    }

    // Function for finding individual portfolio on portfolio JSON file, using passed portfolio
    override fun findPortfolioById(
        userid: String,
        id: String,
        portfolio: MutableLiveData<PortfolioModel>
    ) {

        database.child("user-portfolios").child(userid)
            .child(id.toString()).get().addOnSuccessListener {
                portfolio.value = it.getValue(PortfolioModel::class.java)!!
                println("this is foundportfolio ${portfolio.value}")
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener{
                Timber.e("firebase Error getting data $it")
            }

    }

    // Function for finding individual portfolio on portfolio JSON file, using passed portfolio
    override fun findPortfolioById2(
        userid: String,
        id: String,
        portfolio: MutableLiveData<PortfolioModel>
    ): PortfolioModel? {
        var currentPortfolio = PortfolioModel()
            val ref = database.child("user-portfolios").child(userid).child(id.toString())
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                currentPortfolio = dataSnapshot.getValue(PortfolioModel::class.java)!!
                println("this is the currentPort inside $currentPortfolio")

            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        ref.addListenerForSingleValueEvent(menuListener)
        println("this is the currentPort outside $currentPortfolio")

        return currentPortfolio

    }

    // Function for creating new portfolio on portfolio JSON file
    override fun create(firebaseUser: MutableLiveData<FirebaseUser>, portfolio: PortfolioModel) {
        Timber.i("Firebase DB Reference : $database")
        //portfolio.id = generateRandomId() // Generation of random id for portfolio

        val uid = firebaseUser.value!!.uid
        val key = database.child("donations").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        portfolio.uid = key
        val portfolioValues = portfolio.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/portfolios/$key"] = portfolioValues
        childAdd["/user-portfolios/$uid/$key"] = portfolioValues

        database.updateChildren(childAdd)
    }

    // Function for updating existing portfolio on portfolio JSON file, using passed portfolio
    override fun update(userid: String, portfolioid: String, portfolio: PortfolioModel) {

        val portfolioValues = portfolio.toMap()

        println("this is userid in update $userid")
        println("this is portfolioid in update $portfolioid")
        println("this is portfolio in update $portfolio")


        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["portfolios/$portfolioid"] = portfolioValues
        childUpdate["user-portfolios/$userid/$portfolioid"] = portfolioValues

        database.updateChildren(childUpdate)
        // Find portfolio based on ID
        /*var foundPortfolio: PortfolioModel? = PortfolioManager.portfolios.find { p -> p.uid == portfolio.uid }
        // Update values and store
        if (foundPortfolio != null) {
            foundPortfolio.title = portfolio.title
            foundPortfolio.description = portfolio.description
            foundPortfolio.image = portfolio.image
            foundPortfolio.projects = portfolio.projects
            foundPortfolio.type = portfolio.type
            //serialize()
            logAll()
        }*/
    }

    // Function for deleting portfolio on portfolio JSON file, using passed portfolio
    override fun delete(userid: String, portfolioId: String) {
        val childDelete : MutableMap<String, Any?> = HashMap()
        childDelete["/portfolios/$portfolioId"] = null
        childDelete["/user-portfolios/$userid/$portfolioId"] = null

        database.updateChildren(childDelete)
    }

    // Function for using Timber to log each portfolio in portfolio list
    private fun logAll() {
        PortfolioManager.portfolios.forEach { Timber.i("$it") }
    }

    // Function for using Timber to log each project associated to portfolios in portfolio list
    private fun logProjects() {
        PortfolioManager.portfolios.forEach {
            var portfolioProjects = it.projects?.toMutableList()
            if (portfolioProjects != null) {
                projects += portfolioProjects.toMutableList()
            }
        }
    }

    // Creation of projects list
    var projects = mutableListOf<NewProject>()


    // Function to find specific portfolios based on passed portfolio type
    override fun findSpecificPortfolios(portfolioType: String): MutableList<PortfolioModel> {
        var list =
            PortfolioManager.portfolios.filter { p -> p.type == portfolioType } // Create a list based on matching/filtering portfolio types
        return list.toMutableList() // Return mutable list and log
        println("this is list: $list")
        logAll()
        return PortfolioManager.portfolios
    }

    // Function to find projects that come from portfolios of specific type, using data on passed type
    override fun findSpecificTypeProjects(portfolioType: String): MutableList<NewProject> {
        var list =
            PortfolioManager.portfolios.filter { p -> p.type == portfolioType } // Create a list based on matching/filtering portfolio types
        println("this is list: $list")
        var portfolioTypeProjectsOverall: MutableList<NewProject> =
            arrayListOf() // Create a mutable list for following
        if (list.isNotEmpty()) { // If there are at least some portfolios (i.e. selection wasn't made on empty list)
            list.forEach { // For each portfolio in the list, make a list of the portfolio's projects. If there is a previous list of projects from other portfolios, add the current portfolio projects to that list
                println("project item: " + it.projects?.toMutableList())
                var portfolioTypeProjects = it.projects?.toMutableList()
                println("this is portfolioTypeProject: $portfolioTypeProjects")
                if (portfolioTypeProjects != null) {
                    portfolioTypeProjectsOverall += portfolioTypeProjects.toMutableList()
                    projects = portfolioTypeProjectsOverall.toMutableList()
                }
            }
        } else { // Otherwise return an empty array
            projects = arrayListOf()
        }
        println("this is final returned projects: $projects")
        return projects
    }

    // Function for creating a new project using passed data for project and portfolio
    override fun createProject(project: NewProject, portfolio: PortfolioModel) {
        //project.projectId = ie.wit.showcase2.models.generateRandomId()
        var foundPortfolio: PortfolioModel? =
            PortfolioManager.portfolios.find { p -> p.uid == portfolio.uid } // Finding matching portfolio
        if (foundPortfolio != null) {
            if (foundPortfolio.projects != null) { // If there are already projects in the portfolio, add this project to the list
                var portfolioProjects = foundPortfolio.projects
                portfolioProjects = portfolioProjects?.plus(project)
                foundPortfolio.projects = portfolioProjects
            } else {
                foundPortfolio.projects =
                    arrayOf(project) // Otherwise initiate a new array of projects
            }
            //serialize() // Add project to portfolio JSON file
            logAll()
        }
    }

    // Function for updating a project using passed data for project and related portfolio
    override fun updateProject(project: NewProject, portfolio: PortfolioModel) {

        // Process for updating portfolio JSON file
       /* var foundPortfolio: PortfolioModel? =
            PortfolioManager.portfolios.find { p -> p.id == portfolio.id } // Find the relevant portfolio from the portfolios list based on matching id of passed portfolio
        if (foundPortfolio != null) { // If the portfolio is found...
            if (foundPortfolio.projects != null) { // And the portfolio has projects (as expected)
                var projectIdList =
                    arrayListOf<Long>() // Create a arrayList variable for storing project IDs
                foundPortfolio.projects!!.forEach { // For each project in the relevant portfolio, add the project ID to the list of project IDs
                    projectIdList += it.projectId
                }
                println("this is projectIdList: $projectIdList")
                var projectId = project.projectId
                println("this is projectId: $projectId")
                val index =
                    projectIdList.indexOf(project.projectId) // Find the index position of the project ID that matches the ID of the project that was passed
                println("this is index: $index")
                var portfolioProjects1 =
                    foundPortfolio.projects!!.toMutableList() // Create a list of the projects from the passed portfolio
                var short =
                    portfolioProjects1.removeAt(index) // Remove the project at the previously found index position within the created project list
                println("this is short: $short")
                portfolioProjects1 =
                    portfolioProjects1.plus(project) as MutableList<NewProject> // Add the passed project to the shortened list of projects
                foundPortfolio.projects =
                    ArrayList(portfolioProjects1).toTypedArray() // Assign the new list of projects to the found portfolio
            }
            //serialize() // Update the portfolio JSON file
            logAll()
        }*/
    }

    // Function to delete a project based on passed data for project and portfolio
    override fun deleteProject(project: NewProject, portfolio: PortfolioModel) {
        /*var foundPortfolio: PortfolioModel? = PortfolioManager.portfolios.find { p -> p.id == portfolio.id }
        if (foundPortfolio != null) { // If the portfolio is found...
            if (foundPortfolio.projects != null) { // And the portfolio has projects (as expected)
                var projectIdList =
                    arrayListOf<Long>() // Create a arrayList variable for storing project IDs
                foundPortfolio.projects!!.forEach { // For each project in the relevant portfolio, add the project ID to the list of project IDs
                    projectIdList += it.projectId
                }
                println("this is projectIdList: $projectIdList")
                val index =
                    projectIdList.indexOf(project.projectId) // Find the index position of the project ID that matches the ID of the project that was passed
                println("this is index: $index")
                var portfolioProjects1 =
                    foundPortfolio.projects!!.toMutableList() // Create a list of the projects from the passed portfolio
                var short =
                    portfolioProjects1.removeAt(index) // Remove the project at the previously found index position within the created project list
                println("this is short: $short")
                foundPortfolio.projects =
                    ArrayList(portfolioProjects1).toTypedArray() // Assign the new list of projects to the found portfolio
            }
            //serialize() // Update the portfolio JSON file
            logAll()
        }*/
    }

    override fun findProjectById(projectId: String, portfolioId: String): NewProject? {
        var foundProject: NewProject? = null
        // Process for updating portfolio JSON file
        var foundPortfolio: PortfolioModel? =
            PortfolioManager.portfolios.find { p -> p.uid == portfolioId } // Find the relevant portfolio from the portfolios list based on matching id of passed portfolio
        if (foundPortfolio != null) { // If the portfolio is found...
            if (foundPortfolio.projects != null) { // And the portfolio has projects (as expected)

                foundPortfolio.projects!!.forEach { // For each project in the relevant portfolio, add the project ID to the list of project IDs
                    if (it.projectId == projectId) {
                        foundProject = it
                    }
                }
            }
        }
        return foundProject
    }

}