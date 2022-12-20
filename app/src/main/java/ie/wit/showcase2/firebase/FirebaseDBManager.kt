package ie.wit.showcase2.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import ie.wit.showcase2.models.*
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


    override fun findAll(portfoliosList: MutableLiveData<List<PortfolioModel>>) {
        database.child("portfolios")
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
                    database.child("portfolios")
                        .removeEventListener(this)
                    println("findAll localList $localList")

                    portfoliosList.value = localList
                }
            })
    }

    override fun findAllFavourites(favouritesList: MutableLiveData<List<Favourite>>) {
        database.child("favourites")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Favourite error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<Favourite>()
                    val children = snapshot.children
                    children.forEach {
                        val favourite = it.getValue(Favourite::class.java)
                        localList.add(favourite!!)
                    }
                    database.child("favourites")
                        .removeEventListener(this)
                    println("findAllFavourites localList $localList")

                    favouritesList.value = localList
                }
            })
    }

    override fun findUserAll(userid: String, portfoliosList: MutableLiveData<List<PortfolioModel>>) {
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
                    println("findUserAll localList $localList")

                    portfoliosList.value = localList
                }
            })

    }

    override fun findUserAllFavourites(userid: String, favouritesList: MutableLiveData<List<Favourite>>) {
        database.child("user-favourites").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Favourite error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<Favourite>()
                    val children = snapshot.children
                    children.forEach {
                        val favourite = it.getValue(Favourite::class.java)
                        localList.add(favourite!!)
                    }
                    database.child("user-favourites").child(userid)
                        .removeEventListener(this)
                    println("findUserAllFavourites localList $localList")

                    favouritesList.value = localList
                }
            })

    }

    // Function for finding all projects on portfolio JSON file
    override fun findProjects(userid: String, portfolioId: String, portfolio: MutableLiveData<PortfolioModel>, projectsList: MutableLiveData<List<NewProject>>) {
        database.child("user-portfolios").child(userid).child(portfolioId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Project error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localPortfolio = snapshot.getValue(PortfolioModel::class.java)
                    val localList = localPortfolio?.projects?.toList()

                    database.child("user-portfolios").child(userid).child(portfolioId)
                        .removeEventListener(this)
                    projectsList.value = localList
                }
            })

    }

    override fun findAllProjects(projectsList: MutableLiveData<List<NewProject>>) {
        database.child("portfolios")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Project error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<PortfolioModel>()
                    val localProjectList = mutableListOf<NewProject>()
                    val children = snapshot.children
                    children.forEach {
                        val portfolio = it.getValue(PortfolioModel::class.java)
                        val portfolioProjects = portfolio?.projects?.toMutableList()
                        if (portfolioProjects != null) {
                            localProjectList += portfolioProjects.toMutableList()
                        }
                    }
                    database.child("portfolios")
                        .removeEventListener(this)
                    projectsList.value = localProjectList
                }
            })

    }

    override fun findUserProjects(userid: String, portfoliosList: MutableLiveData<List<PortfolioModel>>, projectsList: MutableLiveData<List<NewProject>>) {
        database.child("user-portfolios").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Portfolio error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localProjectList = mutableListOf<NewProject>()
                    val children = snapshot.children
                    children.forEach {
                        val portfolio = it.getValue(PortfolioModel::class.java)
                        val portfolioProjects = portfolio?.projects?.toMutableList()
                        if (portfolioProjects != null) {
                            localProjectList += portfolioProjects.toMutableList()
                        }
                    }
                    database.child("user-portfolios").child(userid)
                        .removeEventListener(this)

                    projectsList.value = localProjectList
                }
            })

    }

    // Function for finding individual project on portfolio JSON file, using passed project ID
    override fun findUserProject(userid: String, portfoliosList: MutableLiveData<List<PortfolioModel>>, projectsList: MutableLiveData<List<NewProject>>, projectId: String, project: MutableLiveData<NewProject>) {
        database.child("user-portfolios").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Portfolio error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localProjectList = mutableListOf<NewProject>()
                    val children = snapshot.children
                    children.forEach {
                        val portfolio = it.getValue(PortfolioModel::class.java)
                        val portfolioProjects = portfolio?.projects?.toMutableList()
                        if (portfolioProjects != null) {
                            localProjectList += portfolioProjects.toMutableList()
                        }
                    }
                    database.child("user-portfolios").child(userid)
                        .removeEventListener(this)

                    projectsList.value = localProjectList
                    project.value = localProjectList.find { p -> p.projectId == projectId }
                }
            })
    }

    // Function for finding individual project on portfolio JSON file, using passed project ID
    override fun findProject(projectsList: MutableLiveData<List<NewProject>>, projectId: String, project: MutableLiveData<NewProject>) {
        database.child("portfolios")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Portfolio error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localProjectList = mutableListOf<NewProject>()
                    val children = snapshot.children
                    children.forEach {
                        val portfolio = it.getValue(PortfolioModel::class.java)
                        val portfolioProjects = portfolio?.projects?.toMutableList()
                        if (portfolioProjects != null) {
                            localProjectList += portfolioProjects.toMutableList()
                        }
                    }
                    database.child("portfolios")
                        .removeEventListener(this)

                    projectsList.value = localProjectList
                    project.value = localProjectList.find { p -> p.projectId == projectId }
                }
            })
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
        val key = database.child("portfolios").push().key
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

    override fun createFavourite(firebaseUser: MutableLiveData<FirebaseUser>, favourite: Favourite) {
        Timber.i("Firebase DB Reference : $database")
        //portfolio.id = generateRandomId() // Generation of random id for portfolio

        val uid = firebaseUser.value!!.uid
        val key = database.child("favourites").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        favourite.uid = key
        val favouriteValues = favourite.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/favourites/$key"] = favouriteValues
        childAdd["/user-favourites/$uid/$key"] = favouriteValues

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


    }

    override fun updateFavourite(userid: String, project: NewProject) {
        database.child("favourites")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Favourite error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<Favourite>()
                    val children = snapshot.children
                    children.forEach {
                        val favourite = it.getValue(Favourite::class.java)
                        localList.add(favourite!!)
                    }
                    database.child("favourites")
                        .removeEventListener(this)
                    println("findAllFavourites localList $localList")

                    localList.forEach {
                        if (it.projectFavourite?.projectId == project.projectId) {
                            val favouriteId = it?.uid
                            val favourite = Favourite(uid = favouriteId, projectFavourite = project)
                            val favouriteValues = favourite.toMap()

                            val childUpdate : MutableMap<String, Any?> = HashMap()
                            childUpdate["favourites/$favouriteId"] = favouriteValues
                            childUpdate["user-favourites/$userid/$favouriteId"] = favouriteValues

                            database.updateChildren(childUpdate)
                        }
                    }

                }
            })


    }



    // Function for deleting portfolio on portfolio JSON file, using passed portfolio
    override fun delete(userid: String, portfolioId: String) {
        val childDelete : MutableMap<String, Any?> = HashMap()
        childDelete["/portfolios/$portfolioId"] = null
        childDelete["/user-portfolios/$userid/$portfolioId"] = null

        database.updateChildren(childDelete)
    }

    override fun deleteFavourite(userid: String, projectId: String) {
        database.child("favourites")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Favourite error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<Favourite>()
                    val children = snapshot.children
                    children.forEach {
                        val favourite = it.getValue(Favourite::class.java)
                        localList.add(favourite!!)
                    }
                    database.child("favourites")
                        .removeEventListener(this)
                    println("findAllFavourites localList $localList")

                    localList.forEach {
                        if (it.projectFavourite?.projectId == projectId) {
                            val favouriteId = it?.uid
                            val childDelete : MutableMap<String, Any?> = HashMap()
                            childDelete["/favourites/$favouriteId"] = null
                            childDelete["/user-favourites/$userid/$favouriteId"] = null

                            database.updateChildren(childDelete)
                        }
                    }

                    /*val foundFavourite = localList.find { p -> p.projectFavourite?.projectId == projectId }
                    val favouriteId = foundFavourite?.uid
                    val childDelete : MutableMap<String, Any?> = HashMap()
                    childDelete["/favourites/$favouriteId"] = null
                    childDelete["/user-favourites/$userid/$favouriteId"] = null

                    database.updateChildren(childDelete)*/
                }
            })


    }

    fun updateImageRef(userid: String,imageUri: String, path: String) {

        val userPortfolios = database.child("user-portfolios").child(userid)
        val allPortfolios = database.child("portfolios")

        userPortfolios.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        //Update Users imageUri
                        it.ref.child("$path").setValue(imageUri)
                        //Update all portfolios that match 'it'
                        val portfolio = it.getValue(PortfolioModel::class.java)
                        allPortfolios.child(portfolio!!.uid!!)
                            .child("$path").setValue(imageUri)
                    }
                }
            })
    }





    // Creation of projects list
    var projects = mutableListOf<NewProject>()








    // Function for updating a project using passed data for project and related portfolio
    override fun updateProject(project: NewProject, portfolio: PortfolioModel) {


    }

    // Function to delete a project based on passed data for project and portfolio
    override fun deleteProject(project: NewProject, portfolio: PortfolioModel) {

    }



}