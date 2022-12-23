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

    //initialise Firebase database
    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    //function to find and return all portfolios in database
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

    //function to find and return all favourites in database. not used in current code but kept for future.
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

    //function to find and return all portfolios belonging to a user
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

    //function to find and return all projects favourited by a user, include their and those of others
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

    //function to find and return all projects favourited by a user and belonging to them
    override fun findUserUserFavourites(userid: String, favouritesList: MutableLiveData<List<Favourite>>) {
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
                        if (favourite?.projectFavourite?.projectUserId == userid) {
                            localList.add(favourite!!)
                        }
                    }
                    database.child("user-favourites").child(userid)
                        .removeEventListener(this)
                    println("findUserAllFavourites localList $localList")

                    favouritesList.value = localList
                }
            })

    }

    // function to find and return all projects within a particular portfolio belonging to a user
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

    //function to find and return all projects from all portfolios belonging to all users
    override fun findAllProjects(projectsList: MutableLiveData<List<NewProject>>) {
        database.child("portfolios")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Project error : ${error.message}")
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
                }
            })

    }

    //function to find and return all projects from all portfolios belonging to a user. Not used in code but kept for future
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

    // function for finding and returning individual project belonging to user. Not used in code but kept for future.
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

    // function for finding and returning individual project belonging to any user.
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



    // function for finding and returning individual portfolio belonging to a user.
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

    // function for creating new portfolio in Firebase real-time database
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

    // function for creating new favourite in Firebase real-time database
    override fun createFavourite(firebaseUser: MutableLiveData<FirebaseUser>, favourite: Favourite) {
        Timber.i("Firebase DB Reference : $database")
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

    // function for updating existing portfolio in Firebase, using passed portfolio
    override fun update(userid: String, portfolioid: String, portfolio: PortfolioModel) {

        val portfolioValues = portfolio.toMap()

        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["portfolios/$portfolioid"] = portfolioValues
        childUpdate["user-portfolios/$userid/$portfolioid"] = portfolioValues

        database.updateChildren(childUpdate)
    }

    // function for updating project in existing favourite in Firebase, using passed project
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



    // function for deleting portfolio on Firebase, using passed portfolio and user IDs
    override fun delete(userid: String, portfolioId: String) {
        val childDelete : MutableMap<String, Any?> = HashMap()
        childDelete["/portfolios/$portfolioId"] = null
        childDelete["/user-portfolios/$userid/$portfolioId"] = null

        database.updateChildren(childDelete)
    }

    // function for deleting favourite on Firebase, using passed portfolio and user IDs
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

                    //in case there are more than one favourites listed for a particular project, a forEach loop is used
                    localList.forEach {
                        if (it.projectFavourite?.projectId == projectId) {
                            val favouriteId = it?.uid
                            val childDelete : MutableMap<String, Any?> = HashMap()
                            childDelete["/favourites/$favouriteId"] = null
                            childDelete["/user-favourites/$userid/$favouriteId"] = null

                            database.updateChildren(childDelete)
                        }
                    }
                }
            })


    }

    //function to update the references in a profile image in case it was already uploaded but Firebase reassigned a reference
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

}