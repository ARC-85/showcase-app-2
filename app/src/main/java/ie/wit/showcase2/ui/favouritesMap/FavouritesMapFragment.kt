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

        _fragBinding = FragmentFavouritesMapBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        println("testing testing")
        setupMenu()

        favouritesMapViewModel = ViewModelProvider(this).get(FavouritesMapViewModel::class.java)
        //projectsMapViewModel.load()

        var test = favouritesMapViewModel.load()
        println("this is test $test")

        var test2 = favouritesMapViewModel.load()
        println("this is test $test2")

        favouritesMapViewModel.observablePortfoliosList.observe(viewLifecycleOwner, Observer {
                portfolios ->
            portfolios?.let {
                configureEnabler(portfolios as ArrayList<PortfolioModel>)
            }
        })


        favouritesMapViewModel.observableFavouritesList.observe(viewLifecycleOwner, Observer {
                favourites ->
            favourites?.let {
                render(favourites as ArrayList<Favourite>)
            }
        })

        val spinner = fragBinding.projectTypeSpinner
        val adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, portfolioTypes) } as SpinnerAdapter
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                portfolioType = portfolioTypes[position] // Index of array and spinner position used to select portfolio type
                // The toast message was taken out because it was annoying, but can be reinstated if wanted
                /*Toast.makeText(this@PortfolioActivity,
                    getString(R.string.selected_item) + " " +
                            "" + portfolioTypes[position], Toast.LENGTH_SHORT).show()*/
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

    private fun configureEnabler(portfoliosList: ArrayList<PortfolioModel>) {
        if (portfoliosList.isNotEmpty() && enablerSwitch) {
            val userPortfolios = portfoliosList.filter { p -> p.email == loggedInViewModel.liveFirebaseUser.value!!.email }

            val firstPortfolio = userPortfolios[0]
            enabler = firstPortfolio.uid!!
        }
        println("this is enabler $enabler")
        enablerSwitch = false
    }


    private fun render(favouritesList: ArrayList<Favourite>) {

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
        favouritesMapViewModel.map = googleMap
        println("test  favouriteList $favouriteList")
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
        favouriteProjects.forEach { // If show all selected, use function for finding all projects from JSON file
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
        // Display information about a project upon clicking on tag, based on project ID
        if (project != null) {
            fragBinding.currentTitle.text = project.projectTitle
            fragBinding.currentDescription.text = project.projectDescription
            fragBinding.currentEmail.text = project.projectUserEmail
            if (project.projectImage.isNotEmpty()) {
                Picasso.get().load(project.projectImage).resize(200, 200)
                    .into(fragBinding.currentImage)
            }
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
                favouritesMapViewModel.liveFirebaseUser.value = firebaseUser
                favouritesMapViewModel.load()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //fragBinding.mapView.onSaveInstanceState(outState)
    }
}