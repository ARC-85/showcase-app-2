package ie.wit.showcase2.ui.favouritesMap

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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import ie.wit.showcase2.R
import ie.wit.showcase2.databinding.FragmentFavouritesMapBinding
import ie.wit.showcase2.databinding.FragmentProjectsMapBinding
import ie.wit.showcase2.models.Favourite
import ie.wit.showcase2.models.Location
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.ui.auth.LoggedInViewModel
import ie.wit.showcase2.ui.projectsMap.ProjectsMapFragmentDirections
import ie.wit.showcase2.ui.projectsMap.ProjectsMapViewModel

class FavouritesMapFragment : Fragment(), GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private lateinit var favouritesMapViewModel: FavouritesMapViewModel
    var enabler: String = ""
    var enablerSwitch: Boolean = true
    val portfolioTypes = arrayOf("Show All", "New Builds", "Renovations", "Interiors", "Landscaping", "Commercial", "Other") // Creating array of different portfolio types
    var portfolioType = "Show All" // Selected portfolio type for filtering list
    var favouriteProjects = ArrayList<NewProject>()
    var favouriteList = ArrayList<Favourite>()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private var _fragBinding: FragmentFavouritesMapBinding? = null
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
        //setting bindings with xml
        _fragBinding = FragmentFavouritesMapBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        println("testing testing")
        setupMenu()

        //connecting to view model
        favouritesMapViewModel = ViewModelProvider(this).get(FavouritesMapViewModel::class.java)

        //needed to initiate engagement with the model
        var test = favouritesMapViewModel.load()
        println("this is test $test")

        //call portfolios from model
        favouritesMapViewModel.observablePortfoliosList.observe(viewLifecycleOwner, Observer {
                portfolios ->
            portfolios?.let {
                configureEnabler(portfolios as ArrayList<PortfolioModel>)
            }
        })

        //call favourites from model
        favouritesMapViewModel.observableFavouritesList.observe(viewLifecycleOwner, Observer {
                favourites ->
            favourites?.let {
                render(favourites as ArrayList<Favourite>)
            }
        })

        //setting up spinner for filtering type
        val spinner = fragBinding.projectTypeSpinner
        val adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, portfolioTypes) } as SpinnerAdapter
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                //setting type variable
                portfolioType = portfolioTypes[position]
                println("this is portfolioType: $portfolioType")
                favouriteProjects.clear()
                favouritesMapViewModel.observableFavouritesList.observe(viewLifecycleOwner, Observer {
                        favourites ->
                    favourites?.let {
                        render(favourites as ArrayList<Favourite>)
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

    //enabler variable is the portfolio number of a user to allow for them to access other users' projects, i.e. required for accessing ProjectDetailsFragment (in addition to their user ID)
    private fun configureEnabler(portfoliosList: ArrayList<PortfolioModel>) {
        //only an option to use if the user has their own portfolio already, otherwise they can't access other users' portfolios
        if (portfoliosList.isNotEmpty() && enablerSwitch) {
            val userPortfolios = portfoliosList.filter { p -> p.email == loggedInViewModel.liveFirebaseUser.value!!.email }
            val firstPortfolio = userPortfolios[0]
            enabler = firstPortfolio.uid!!
        }
        println("this is enabler $enabler")
        enablerSwitch = false
    }

    private fun render(favouritesList: ArrayList<Favourite>) {
        //adjust portfolio list based on spinner selection
        if (portfolioType == "Show All") {
            favouriteList = favouritesList
        } else {
            favouriteList = ArrayList(favouritesList.filter { p -> p.projectFavourite?.projectPortfolioType == portfolioType })
        }
        println("this is favouriteList $favouriteList")
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync {
            onMapReady(it) // Calling configure map function
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        //calling map from view model
        favouritesMapViewModel.map = googleMap
        println("test  favouriteList $favouriteList")
        //rendered favourite list
        favouriteList.forEach {
            val projects = it.projectFavourite
            if (projects != null) {
                favouriteProjects += projects
            }
        }
        favouritesMapViewModel.map.setOnMarkerClickListener(this)
        favouritesMapViewModel.map.uiSettings.setZoomControlsEnabled(true)
        println("this is favouriteProjects: $favouriteProjects")
        favouritesMapViewModel.map.clear()
        //process markers for each favourited project
        favouriteProjects.forEach {
            val loc = LatLng(it.lat, it.lng)
            val options = MarkerOptions().title(it.projectTitle).position(loc)
            favouritesMapViewModel.map.addMarker(options)?.tag = it.projectId
            favouritesMapViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f))
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val tag =marker.tag as String
        val project = favouriteProjects.find { p -> p.projectId == tag }
        println("this is project: $project")
        // display information about a project upon clicking on tag, based on project ID
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
                fragBinding.cardView.setOnClickListener {
                    val action = FavouritesMapFragmentDirections.actionFavouritesMapFragmentToProjectDetailFragment(
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
                //use of toggle button to switch between favourites belonging to user or all users
                val item = menu.findItem(R.id.toggleProjects) as MenuItem
                item.setActionView(R.layout.togglebutton_layout)
                val toggleProjects: SwitchCompat = item.actionView!!.findViewById(R.id.toggleButton)
                toggleProjects.isChecked = false

                toggleProjects.setOnCheckedChangeListener { _, isChecked ->
                    favouriteProjects.clear()
                    if (isChecked) {
                        favouritesMapViewModel.loadAll()
                        fragBinding.mapTitle.setText("All Favourites")
                    }
                    else {
                        favouritesMapViewModel.load()
                        fragBinding.mapTitle.setText("My Favourites")

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
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                favouritesMapViewModel.liveFirebaseUser.value = firebaseUser
                favouritesMapViewModel.load()
            }
        })
    }

}