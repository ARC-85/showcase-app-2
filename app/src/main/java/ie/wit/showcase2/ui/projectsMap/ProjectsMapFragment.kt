package ie.wit.showcase2.ui.projectsMap

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import ie.wit.showcase2.R
import ie.wit.showcase2.databinding.FragmentProjectsMapBinding
import ie.wit.showcase2.models.Location
import ie.wit.showcase2.models.NewProject
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.ui.auth.LoggedInViewModel

class ProjectsMapFragment : Fragment(), GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private lateinit var projectsMapViewModel: ProjectsMapViewModel
    var enabler: String = ""
    var enablerSwitch: Boolean = true
    val portfolioTypes = arrayOf("Show All", "New Builds", "Renovations", "Interiors", "Landscaping", "Commercial", "Other") // Creating array of different portfolio types
    var portfolioType = "Show All" // Selected portfolio type for filtering list
    var userProjects = ArrayList<NewProject>()
    var portfolioList = ArrayList<PortfolioModel>()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private var _fragBinding: FragmentProjectsMapBinding? = null
    private val fragBinding get() = _fragBinding!!
    var location = Location()
    var project = NewProject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentProjectsMapBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        println("testing testing")
        setupMenu()

        projectsMapViewModel = ViewModelProvider(this).get(ProjectsMapViewModel::class.java)
        //projectsMapViewModel.load()

        var test = projectsMapViewModel.load()
        println("this is test $test")

        projectsMapViewModel.observablePortfoliosList.observe(viewLifecycleOwner, Observer {
                portfolios ->
            portfolios?.let {
                render(portfolios as ArrayList<PortfolioModel>)
                println("this is the portfolios on the map $portfolios")
                configureEnabler(portfolios)
            }
        })

        val spinner = fragBinding.projectTypeSpinner
        val adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, portfolioTypes) } as SpinnerAdapter
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                portfolioType = portfolioTypes[position]
                println("this is portfolioType: $portfolioType")
                userProjects.clear()
                projectsMapViewModel.observablePortfoliosList.observe(viewLifecycleOwner, Observer {
                        portfolios ->
                    portfolios?.let {
                        render(portfolios as ArrayList<PortfolioModel>)
                        println("testing this is working")
                    }
                })
            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        return root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun configureEnabler(portfoliosList: ArrayList<PortfolioModel>) {
        if (portfoliosList.isNotEmpty() && enablerSwitch) {
            val userPortfolios = portfoliosList.filter { p -> p.email == loggedInViewModel.liveFirebaseUser.value!!.email }
            val firstPortfolio = userPortfolios[0]
            enabler = firstPortfolio.uid!!
        }
        println("this is enabler $enabler")
        enablerSwitch = false
    }

    private fun render(portfoliosList: ArrayList<PortfolioModel>) {
        if (portfolioType == "Show All") {
            portfolioList = portfoliosList
        } else {
            portfolioList = ArrayList(portfoliosList.filter { p -> p.type == portfolioType })
        }
        println("this is portfolioList $portfolioList")
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync {
            onMapReady(it) // Calling configure map function
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        projectsMapViewModel.map = googleMap
        println("test  portfolioList $portfolioList")
        portfolioList.forEach {
            val portfolioProjects = it.projects?.toMutableList()
            if (portfolioProjects != null) {
                userProjects += portfolioProjects.toMutableList()
            }
        }
        projectsMapViewModel.map.setOnMarkerClickListener(this)
        projectsMapViewModel.map.uiSettings.setZoomControlsEnabled(true)
        println("this is userProjects: $userProjects")
        projectsMapViewModel.map.clear()
        userProjects.forEach { // If show all selected, use function for finding all projects from JSON file
            val loc = LatLng(it.lat, it.lng)
            val options = MarkerOptions().title(it.projectTitle).position(loc)
            projectsMapViewModel.map.addMarker(options)?.tag = it.projectId
            projectsMapViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f))
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val tag =marker.tag as String
        val project = userProjects.find { p -> p.projectId == tag }
        println("this is project: $project")
        // Display information about a project upon clicking on tag, based on project ID
        if (project != null) {
            fragBinding.currentTitle.text = project.projectTitle
            fragBinding.currentDescription.text = project.projectDescription
            fragBinding.currentEmail.text = project.projectUserEmail
            if (project.projectImage.isNotEmpty()) {
                Picasso.get().load(project.projectImage).resize(200, 200)
                    .into(fragBinding.currentImage)
            }
            //user can only access other users' portfolios if they have their own portfolio to begin with
            if (enabler != "") {
                //enabler is related to portfolio id, if the project belongs to the user then the portfolio id passed is that of the project
                if (project.projectUserId == loggedInViewModel.liveFirebaseUser.value!!.uid) {
                    enabler = project.portfolioId
                }
                //otherwise, the enabler remains a random portfolio id related to the current user because if it related to another user's portfolio they wouldn't have authorisation
                fragBinding.cardView.setOnClickListener {
                    val action = ProjectsMapFragmentDirections.actionProjectsMapFragmentToProjectDetailFragment(
                        project,
                        enabler,
                        Location(lat = project.lat, lng = project.lng, zoom = 15f)
                    )
                    findNavController().navigate(action)
                }
            }

        }
        return false
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_projects_map, menu)

                val item = menu.findItem(R.id.toggleProjects) as MenuItem
                item.setActionView(R.layout.togglebutton_layout)
                val toggleProjects: SwitchCompat = item.actionView!!.findViewById(R.id.toggleButton)
                toggleProjects.isChecked = false

                toggleProjects.setOnCheckedChangeListener { _, isChecked ->
                    userProjects.clear()
                    if (isChecked) {
                        projectsMapViewModel.loadAll()
                        fragBinding.mapTitle.setText("All Projects")

                    }
                    else {
                        projectsMapViewModel.load()
                        fragBinding.mapTitle.setText("My Projects")

                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onResume() {
        super.onResume()
        //fragBinding.mapView.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                projectsMapViewModel.liveFirebaseUser.value = firebaseUser
                projectsMapViewModel.load()
            }
        })
    }
}



