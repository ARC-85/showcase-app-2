package ie.wit.showcase2.ui.projectList

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.wit.showcase2.R
import ie.wit.showcase2.adapters.ProjectAdapter
import ie.wit.showcase2.adapters.ProjectListener


import ie.wit.showcase2.databinding.FragmentProjectListBinding
import ie.wit.showcase2.models.Favourite
import ie.wit.showcase2.models.Location
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.ui.auth.LoggedInViewModel

import ie.wit.showcase2.utils.*
import java.util.*
import kotlin.collections.ArrayList

class ProjectListFragment : Fragment(), ProjectListener {

    private var _fragBinding: FragmentProjectListBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader : AlertDialog
    private val projectListViewModel: ProjectListViewModel by activityViewModels()
    private val args by navArgs<ProjectListFragmentArgs>()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    var projectBudget = "" // Selected portfolio type for filtering list
    val projectBudgets = arrayOf("Show All", "€0-€50K", "€50K-€100K", "€100K-€250K", "€250K-€500K", "€500K-€1M", "€1M+") // Creating array of different project budgets
    var list = ArrayList<NewProject>()
    var currentPortfolio = PortfolioModel()
    val today = Calendar.getInstance()
    var dateDay = today.get(Calendar.DAY_OF_MONTH)
    var dateMonth = today.get(Calendar.MONTH)
    var dateYear = today.get(Calendar.YEAR)
    var initialLocation = Location(0.0, -7.139102, 15f)
    var favs = ArrayList<Favourite>()
    var check = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentProjectListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        loader = createLoader(requireActivity())

        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        projectListViewModel.load(args.portfolioid)

        projectListViewModel.observableProjectsList.observe(viewLifecycleOwner, Observer {
                projects ->
            projects?.let {
                render(ArrayList(projects))
                hideLoader(loader)
                checkSwipeRefresh()
            }
        })

        projectListViewModel.observablePortfolio.observe(viewLifecycleOwner, Observer {
                portfolio ->
            portfolio?.let {
                currentPortfolio = portfolio
                getCurrentPortfolio(portfolio)
            }
        })

        projectListViewModel.observableFavouritesList.observe(viewLifecycleOwner, Observer {
                favourites ->
            favourites?.let {
                getCurrentFavourites(ArrayList(favourites))
            }
        })

        var test = projectListViewModel.getPortfolio(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.portfolioid)
        println("this is test $test")

        //needed to initiate engagement with the model
        var test2 = projectListViewModel.loadAllFavourites()
        println("this is test2 $test2")

        fragBinding.fab.setOnClickListener {
            val action = ProjectListFragmentDirections.actionProjectListFragmentToProjectNewFragment(args.portfolioid, initialLocation, NewProject(projectCompletionDay = dateDay, projectCompletionMonth = dateMonth, projectCompletionYear = dateYear))
            findNavController().navigate(action)
        }

        setSwipeRefresh()

        val swipeDeleteHandler = object : SwipeToDeleteProjectCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                showLoader(loader,"Deleting Project")
                val adapter = fragBinding.recyclerView.adapter as ProjectAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                val removedProject = (viewHolder.itemView.tag as NewProject)
                if (loggedInViewModel.liveFirebaseUser.value?.uid!! == removedProject.projectUserId) {
                if (currentPortfolio.projects != null) { // If the portfolio has projects (as expected)
                    var projectIdList =
                        arrayListOf<String>() // Create a arrayList variable for storing project IDs
                    currentPortfolio.projects!!.forEach { // For each project in the relevant portfolio, add the project ID to the list of project IDs
                        projectIdList += it.projectId
                    }
                    println("this is projectIdList: $projectIdList")
                    var projectId = (viewHolder.itemView.tag as NewProject).projectId
                    println("this is projectId: $projectId")
                    val index =
                        projectIdList.indexOf(projectId) // Find the index position of the project ID that matches the ID of the project that was passed
                    println("this is index: $index")
                    var portfolioProjects1 =
                        currentPortfolio.projects!! // Create a list of the projects from the passed portfolio
                    var short =
                        portfolioProjects1.removeAt(index) // Remove the project at the previously found index position within the created project list
                    println("this is short: $short")

                    currentPortfolio.projects =
                        java.util.ArrayList(portfolioProjects1) // Assign the new list of projects to the found portfolio

                    println("this is updated portfolio projects ${currentPortfolio.projects}")
                }
                    projectListViewModel.removeFavourite(loggedInViewModel.liveFirebaseUser.value?.uid!!, removedProject.projectId)
                    projectListViewModel.updatePortfolio(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.portfolioid, currentPortfolio)
                }
                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)

        val swipeEditHandler = object : SwipeToEditProjectCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onProjectClick(viewHolder.itemView.tag as NewProject)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)

        val spinner = fragBinding.projectBudgetSpinner
        spinner.adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, projectBudgets) } as SpinnerAdapter

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                projectBudget = projectBudgets[position] // Index of array and spinner position used to select project budget

                println("this is projectBudget: $projectBudget")
                projectListViewModel.observableProjectsList.observe(viewLifecycleOwner, Observer {
                        projects ->
                    projects?.let {
                        render(ArrayList(projects))
                    }
                })
            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        return root
    }

    private fun getCurrentPortfolio(portfolio: PortfolioModel) {
        currentPortfolio = portfolio
        fragBinding.portfolioName.setText(portfolio.title)
        println("this is newCurrentPortfolio3 $currentPortfolio")
    }

    private fun getCurrentFavourites(favouritesList: ArrayList<Favourite>) {
        favs = favouritesList
        println("this is favs in getCurrentFavourites $favs")
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_project_list, menu)

                val item = menu.findItem(R.id.toggleProjects) as MenuItem
                item.setActionView(R.layout.togglebutton_layout)
                val toggleProjects: SwitchCompat = item.actionView!!.findViewById(R.id.toggleButton)
                toggleProjects.isChecked = false

                toggleProjects.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) projectListViewModel.loadAll()
                    else projectListViewModel.load(args.portfolioid)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    R.id.item_home -> {
                        val action = ProjectListFragmentDirections.actionProjectListFragmentToPortfolioListFragment()
                        findNavController().navigate(action)
                    }
                    R.id.item_cancel -> {
                        val action = ProjectListFragmentDirections.actionProjectListFragmentToPortfolioDetailFragment(args.portfolioid)
                        findNavController().navigate(action)
                    }
                }
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun render(projectsList: ArrayList<NewProject>) {
        //applying filter for project budget
        var favList = ArrayList<NewProject>()

        println("this is favs in render $favs")
        println("this is projectsList $projectsList")

        projectsList.forEach { project ->
            check = false
            val projID = project.projectId
            favs.forEach {
                if (projID == it.projectFavourite?.projectId)  {
                    val altProject = project.copy(projectFavourites = mutableListOf(loggedInViewModel.liveFirebaseUser.value?.uid!!))
                    favList.add(altProject)
                    check = true
                }
            }
            if (!check) {
                val altProject = project.copy(projectFavourites = null)
                favList.add(altProject)
            }
        }

        println("this is favList  $favList")
        if (projectBudget != "Show All") {
            list = ArrayList(favList.filter { p -> p.projectBudget == projectBudget })
            println("this is internal list $list")
            fragBinding.recyclerView.adapter = ProjectAdapter(list,this)
        } else {
            list = favList
        }
        println("this is portfoliosList $projectsList")
        println("this is list $list")
        fragBinding.recyclerView.adapter = ProjectAdapter(list,this)
        if (projectsList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.projectsNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.projectsNotFound.visibility = View.GONE
        }
    }

    override fun onProjectClick(project: NewProject) {
        val action = ProjectListFragmentDirections.actionProjectListFragmentToProjectDetailFragment(
            project,
            args.portfolioid,
            Location(lat = project.lat, lng = project.lng, zoom = project.zoom)
        )
        findNavController().navigate(action)
    }

    fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {

        if (currentPortfolio.projects != null) {
            fragBinding.swiperefresh.isRefreshing = true
            showLoader(loader,"Downloading Projects")
        } else {
            fragBinding.swiperefresh.isRefreshing = false
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.projectsNotFound.visibility = View.VISIBLE
        }
            projectListViewModel.load(args.portfolioid)
        }
    }

    fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                projectListViewModel.liveFirebaseUser.value = firebaseUser
                projectListViewModel.load(args.portfolioid)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}