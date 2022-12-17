package ie.wit.showcase2.ui.projectsMap

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import ie.wit.showcase2.R
import ie.wit.showcase2.databinding.FragmentProjectMapBinding
import ie.wit.showcase2.databinding.FragmentProjectsMapBinding
import ie.wit.showcase2.models.Location
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.ui.projectDetail.ProjectDetailFragmentArgs
import ie.wit.showcase2.ui.projectMap.ProjectMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import ie.wit.showcase2.adapters.ProjectAdapter
import ie.wit.showcase2.databinding.ContentProjectsMapBinding
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.ui.auth.LoggedInViewModel
import ie.wit.showcase2.ui.portfolioNew.PortfoliolNewViewModel
import ie.wit.showcase2.ui.projectList.ProjectListFragmentArgs
import ie.wit.showcase2.ui.projectList.ProjectListViewModel
import ie.wit.showcase2.utils.hideLoader

class ProjectsMapFragment : Fragment(), GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private lateinit var projectsMapViewModel: ProjectsMapViewModel



    var userProjects = ArrayList<NewProject>()
    var portfolioList = ArrayList<PortfolioModel>()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()

    private var _fragBinding: FragmentProjectsMapBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val fragBinding get() = _fragBinding!!

    //lateinit var map : GoogleMap
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
                //configureMap(portfolios)
            }
        })
        return root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun render(portfoliosList: ArrayList<PortfolioModel>) {

        portfolioList = portfoliosList
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
            projectsMapViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, it.zoom))
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
            fragBinding.currentPortfolio.text = "Portfolio: ${project.projectPortfolioName}"
            if (project.projectImage.isNotEmpty()) {
                Picasso.get().load(project.projectImage).resize(200, 200)
                    .into(fragBinding.currentImage)
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

                    }
                    else {
                        projectsMapViewModel.load()

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

    override fun onLowMemory() {
        super.onLowMemory()
        //fragBinding.mapView.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        //fragBinding.mapView.onPause()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //fragBinding.mapView.onSaveInstanceState(outState)
    }
}



