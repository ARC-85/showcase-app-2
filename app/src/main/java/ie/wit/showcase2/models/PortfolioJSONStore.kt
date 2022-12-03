package ie.wit.showcase2.models
/*
import android.content.Context
import android.net.Uri
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import ie.wit.showcase2.utils.*
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

const val PORTFOLIO_JSON_FILE = "portfolios.json" // Portfolio JSON file is main file used

// Create single GSON builder, related to portfolio JSON
val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting()
    .registerTypeAdapter(Uri::class.java, UriParser())
    .create()

val listType: Type = object : TypeToken<ArrayList<PortfolioModel>>() {}.type // Main list created, based on Portfolio model

// Function for generating random ID numbers
fun generateRandomId(): Long {
    return Random().nextLong()
}

class PortfolioJSONStore(private val context: Context) : PortfolioStore {

    // Creation of portfolios list
    var portfolios = mutableListOf<PortfolioModel>()

    // Initialisation of portfolio JSON file requires deserialisation of data
    init {
        if (exists(context, PORTFOLIO_JSON_FILE)) {
            deserialize()
        }
    }

    // Function for finding all portfolios on portfolio JSON file
    override fun findAll(): MutableList<PortfolioModel> {
        logAll()
        return portfolios
    }

    // Function for finding all projects on portfolio JSON file
    override fun findProjects(): MutableList<NewProject> {
        logProjects()
        return projects
    }

    // Function for finding individual project on portfolio JSON file, using passed project ID
    override fun findProject(id: String): NewProject? {
        logProjects()
        return projects.find { p -> p.projectId == id }
    }

    // Function for finding individual portfolio on portfolio JSON file, using passed portfolio
    override fun findPortfolio(portfolio: PortfolioModel): PortfolioModel? {
        logAll()
        return portfolios.find { p -> p.id == portfolio.id }
    }

    // Function for finding individual portfolio on portfolio JSON file, using passed portfolio
    override fun findPortfolioById(id: String): PortfolioModel? {
        logAll()
        return portfolios.find { p -> p.id == id }
    }

    // Function for creating new portfolio on portfolio JSON file
    override fun create(portfolio: PortfolioModel) {
        portfolio.id = generateRandomId() // Generation of random id for portfolio
        portfolios.add(portfolio)
        serialize()
    }

    // Function for updating existing portfolio on portfolio JSON file, using passed portfolio
    override fun update(portfolio: PortfolioModel) {
        // Find portfolio based on ID
        var foundPortfolio: PortfolioModel? = portfolios.find { p -> p.id == portfolio.id }
        // Update values and store
        if (foundPortfolio != null) {
            foundPortfolio.title = portfolio.title
            foundPortfolio.description = portfolio.description
            foundPortfolio.image = portfolio.image
            foundPortfolio.projects = portfolio.projects
            foundPortfolio.type = portfolio.type
            serialize()
        }
    }

    // Function for deleting portfolio on portfolio JSON file, using passed portfolio
    override fun delete(portfolio: PortfolioModel) {
        println("this is the removed portfolio: $portfolio")
        portfolios.remove(portfolio)
        serialize()
    }

    // Function creating jsonString using GSON builder and portfolios list, as well as write function in file helper
    private fun serialize() {
        val jsonString = gsonBuilder.toJson(portfolios, listType)
        write(context, PORTFOLIO_JSON_FILE, jsonString)
    }

    // Function reading jsonString using read function in file helper and creating portfolios list using GSON builder
    private fun deserialize() {
        val jsonString = read(context, PORTFOLIO_JSON_FILE)
        portfolios = gsonBuilder.fromJson(jsonString, listType)
    }

    // Function for using Timber to log each portfolio in portfolio list
    private fun logAll() {
        portfolios.forEach { Timber.i("$it") }
    }

    // Function for using Timber to log each project associated to portfolios in portfolio list
    private fun logProjects() {
        portfolios.forEach {
            var portfolioProjects = it.projects?.toMutableList()
            if (portfolioProjects != null) {
                projects += portfolioProjects.toMutableList()
            }
        }
    }

    // Creation of projects list
    var projects = mutableListOf<NewProject>()

    // Function not used, but left in case needed in future (see findProjects() instead)
    override fun findAllProjects(): MutableList<NewProject> {
        logAllProjects()
        return projects
    }

    // Function not used, but left in case needed in future (see findSpecificTypeProjects() instead)
    override fun findSpecificProjects(portfolio: PortfolioModel): MutableList<NewProject> {
        var foundPortfolio: PortfolioModel? = portfolios.find { p -> p.id == portfolio.id }
        if (foundPortfolio != null) {
            var list = projects.filter { p -> p.portfolioId == foundPortfolio.id }
            return list.toMutableList()
        }
        logAllProjects()
        return projects
    }

    // Function to find specific portfolios based on passed portfolio type
    override fun findSpecificPortfolios(portfolioType: String): MutableList<PortfolioModel> {
        var list = portfolios.filter { p -> p.type == portfolioType } // Create a list based on matching/filtering portfolio types
        return list.toMutableList() // Return mutable list and log
        println("this is list: $list")
        logAll()
        return portfolios
    }

    // Function to find projects that come from portfolios of specific type, using data on passed type
    override fun findSpecificTypeProjects(portfolioType: String): MutableList<NewProject> {
        var list = portfolios.filter { p -> p.type == portfolioType } // Create a list based on matching/filtering portfolio types
        println("this is list: $list")
        var portfolioTypeProjectsOverall: MutableList<NewProject> = arrayListOf() // Create a mutable list for following
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
        project.projectId = generateRandomId()
        var foundPortfolio: PortfolioModel? = portfolios.find { p -> p.id == portfolio.id } // Finding matching portfolio
        if (foundPortfolio != null) {
            if (foundPortfolio.projects != null) { // If there are already projects in the portfolio, add this project to the list
                var portfolioProjects = foundPortfolio.projects
                portfolioProjects = portfolioProjects?.plus(project)
                foundPortfolio.projects = portfolioProjects
            } else {
                foundPortfolio.projects = arrayOf(project) // Otherwise initiate a new array of projects
            }
            serialize() // Add project to portfolio JSON file
        }
    }

    // Function for updating a project using passed data for project and related portfolio
    override fun updateProject(project: NewProject, portfolio: PortfolioModel) {

        // Process for updating portfolio JSON file
        var foundPortfolio: PortfolioModel? = portfolios.find { p -> p.id == portfolio.id } // Find the relevant portfolio from the portfolios list based on matching id of passed portfolio
        if (foundPortfolio != null) { // If the portfolio is found...
            if (foundPortfolio.projects != null) { // And the portfolio has projects (as expected)
                var projectIdList = arrayListOf<Long>() // Create a arrayList variable for storing project IDs
                foundPortfolio.projects!!.forEach { // For each project in the relevant portfolio, add the project ID to the list of project IDs
                    projectIdList += it.projectId
                }
                println("this is projectIdList: $projectIdList")
                val index = projectIdList.indexOf(project.projectId) // Find the index position of the project ID that matches the ID of the project that was passed
                println("this is index: $index")
                var portfolioProjects1 = foundPortfolio.projects!!.toMutableList() // Create a list of the projects from the passed portfolio
                var short = portfolioProjects1.removeAt(index) // Remove the project at the previously found index position within the created project list
                println("this is short: $short")
                portfolioProjects1 = portfolioProjects1.plus(project) as MutableList<NewProject> // Add the passed project to the shortened list of projects
                foundPortfolio.projects = ArrayList(portfolioProjects1).toTypedArray() // Assign the new list of projects to the found portfolio
            }
            serialize() // Update the portfolio JSON file
        }
    }

    // Function to delete a project based on passed data for project and portfolio
    override fun deleteProject(project: NewProject, portfolio: PortfolioModel) {
        var foundPortfolio: PortfolioModel? = portfolios.find { p -> p.id == portfolio.id }
        if (foundPortfolio != null) { // If the portfolio is found...
            if (foundPortfolio.projects != null) { // And the portfolio has projects (as expected)
                var projectIdList = arrayListOf<Long>() // Create a arrayList variable for storing project IDs
                foundPortfolio.projects!!.forEach { // For each project in the relevant portfolio, add the project ID to the list of project IDs
                    projectIdList += it.projectId
                }
                println("this is projectIdList: $projectIdList")
                val index = projectIdList.indexOf(project.projectId) // Find the index position of the project ID that matches the ID of the project that was passed
                println("this is index: $index")
                var portfolioProjects1 = foundPortfolio.projects!!.toMutableList() // Create a list of the projects from the passed portfolio
                var short = portfolioProjects1.removeAt(index) // Remove the project at the previously found index position within the created project list
                println("this is short: $short")
                foundPortfolio.projects = ArrayList(portfolioProjects1).toTypedArray() // Assign the new list of projects to the found portfolio
            }
            serialize() // Update the portfolio JSON file
        }
    }

    private fun logAllProjects() {
        projects.forEach { Timber.i("$it") }
    }
}

class UriParser : JsonDeserializer<Uri>,JsonSerializer<Uri> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Uri {
        return Uri.parse(json?.asString)
    }

    override fun serialize(
        src: Uri?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }
}
*/
