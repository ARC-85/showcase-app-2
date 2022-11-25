package ie.wit.showcase2.models

import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

// Function for generating random ID numbers
internal fun generateRandomId(): Long {
    return Random().nextLong()
}

object PortfolioManager : PortfolioStore {

    // Creation of portfolios list
    var portfolios = mutableListOf<PortfolioModel>()

    // Function for finding all portfolios on portfolio JSON file
    override fun findAll(): MutableList<PortfolioModel> {
        //logAll()
        return portfolios
    }

    // Function for finding all projects on portfolio JSON file
    override fun findProjects(): MutableList<NewProject> {
        logProjects()
        return projects
    }

    // Function for finding individual project on portfolio JSON file, using passed project ID
    override fun findProject(id: Long): NewProject? {
        logProjects()
        return projects.find { p -> p.projectId == id }
    }

    // Function for finding individual portfolio on portfolio JSON file, using passed portfolio
    override fun findPortfolio(portfolio: PortfolioModel): PortfolioModel? {
        logAll()
        return portfolios.find { p -> p.id == portfolio.id }
    }

    // Function for finding individual portfolio on portfolio JSON file, using passed portfolio
    override fun findPortfolioById(id: Long): PortfolioModel? {
        logAll()
        return portfolios.find { p -> p.id == id }
    }

    // Function for creating new portfolio on portfolio JSON file
    override fun create(portfolio: PortfolioModel) {
        portfolio.id = generateRandomId() // Generation of random id for portfolio
        portfolios.add(portfolio)
        //serialize()
        logAll()
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
            //serialize()
            logAll()
        }
    }

    // Function for deleting portfolio on portfolio JSON file, using passed portfolio
    override fun delete(portfolio: PortfolioModel) {
        println("this is the removed portfolio: $portfolio")
        portfolios.remove(portfolio)
        //serialize()
        logAll()
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
            //serialize() // Add project to portfolio JSON file
            logAll()
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
            //serialize() // Update the portfolio JSON file
            logAll()
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
            //serialize() // Update the portfolio JSON file
            logAll()
        }
    }

}

