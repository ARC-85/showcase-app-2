package ie.wit.showcase2.ui.projectNew

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import ie.wit.showcase2.R

import ie.wit.showcase2.databinding.FragmentProjectNewBinding
import ie.wit.showcase2.firebase.FirebaseImageManager
import ie.wit.showcase2.models.Location
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioModel

import ie.wit.showcase2.ui.auth.LoggedInViewModel
import ie.wit.showcase2.ui.map.MapProject
import ie.wit.showcase2.ui.projectList.ProjectListFragmentDirections

import ie.wit.showcase2.ui.projectList.ProjectListViewModel
import ie.wit.showcase2.utils.checkLocationPermissions
import ie.wit.showcase2.utils.readImageUri
import ie.wit.showcase2.utils.showImagePicker
import timber.log.Timber
import java.util.*

class ProjectNewFragment : Fragment(), OnMapReadyCallback {

    private var _fragBinding: FragmentProjectNewBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val fragBinding get() = _fragBinding!!
    private lateinit var projectViewModel: ProjectNewViewModel
    private val args by navArgs<ProjectNewFragmentArgs>()
    private val projectListViewModel: ProjectListViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var image2IntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var image3IntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var mapIntentLauncher : ActivityResultLauncher<Intent>
    var projectBudget = "€0-€50K" // Project budget initial selection
    var image: String = ""
    val projectBudgets = arrayOf("€0-€50K", "€50K-€100K", "€100K-€250K", "€250K-€500K", "€500K-€1M", "€1M+") // Creating array of different project budgets
    val today = Calendar.getInstance()
    var dateDay = today.get(Calendar.DAY_OF_MONTH)
    var dateMonth = today.get(Calendar.MONTH)
    var dateYear = today.get(Calendar.YEAR)
    var project = NewProject()
    var currentPortfolio = PortfolioModel()
    var initialLocation = Location(52.245696, -7.139102, 15f)
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient //from https://www.tutorialspoint.com/how-to-show-current-location-on-a-google-map-on-android-using-kotlin
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _fragBinding = FragmentProjectNewBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        registerImagePickerCallback()
        //registerMapCallback()
        projectViewModel = ViewModelProvider(this).get(ProjectNewViewModel::class.java)
        fusedLocationProviderClient = requireActivity().let{LocationServices.getFusedLocationProviderClient(it)}


        projectViewModel.observablePortfolio.observe(viewLifecycleOwner, Observer {
                portfolio ->
            portfolio?.let {
                currentPortfolio = portfolio
                getCurrentPortfolio(portfolio)
                render(portfolio)
            }
        })

        var test = projectViewModel.getPortfolio(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.portfolioid)
        println("this is test $test")

        println("this is newnewcurrentPortfolio $currentPortfolio")

        val spinner = fragBinding.projectBudgetSpinner
        spinner.adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, projectBudgets) } as SpinnerAdapter

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                projectBudget = projectBudgets[position] // Index of array and spinner position used to select project budget

                println("this is projectBudget: $projectBudget")
            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        fragBinding.projectLocation.setOnClickListener {
            Timber.i("Set Location Pressed")
        }

        // Set the initial values for location if a new location is set, passing details of location and project to the map activity
        fragBinding.projectLocation.setOnClickListener {
            var location = args.location
            if (args.location.lat.equals(0.0)) {
                val task = fusedLocationProviderClient.lastLocation
                task.addOnSuccessListener { myLocation ->
                    location.lat = myLocation.latitude
                    location.lng = myLocation.longitude
                }
            } else {
                location = args.location
            }

            val action = ProjectNewFragmentDirections.actionProjectNewFragmentToProjectMapFragment(location, args.portfolioid,NewProject(projectTitle = fragBinding.projectTitle.text.toString(), projectDescription = fragBinding.projectDescription.text.toString(),
                projectBudget = projectBudget, projectImage = project.projectImage, projectImage2 = project.projectImage2, projectImage3 = project.projectImage3,
                portfolioId = args.portfolioid, lat = location.lat, lng = location.lng, zoom = 15f, projectUserId = loggedInViewModel.liveFirebaseUser.value?.uid!!, projectUserEmail = loggedInViewModel.liveFirebaseUser.value?.email!!,
                projectCompletionDay = dateDay, projectCompletionMonth = dateMonth, projectCompletionYear = dateYear, projectPortfolioName = currentPortfolio.title, projectPortfolioType = currentPortfolio.type))
            findNavController().navigate(action)
        }

        // Set up DatePicker
        val datePicker = fragBinding.projectCompletionDatePicker
        // Set initial values if a completion date already exists

        datePicker.init(dateYear, dateMonth, dateDay) { view, year, month, day ->
            val month = month + 1
            val msg = "You Selected: $day/$month/$year"
            var dateProjectCompletion = "$day/$month/$year"
            dateDay = day
            dateMonth = month
            dateYear = year
            // Toast is turned off, but can be turned back on
            //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            println ("this is dateDay: $dateDay")
            println ("this is dateMonth: $dateMonth")
            println ("this is dateYear: $dateYear")
            println("this is datePicker: $datePicker")
            println("this is dateProjectCompletion: $dateProjectCompletion")
        }

        setButtonListener(fragBinding)

        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
            fragBinding.chooseImage2.isVisible = true
            fragBinding.projectImage2.isVisible = true
        }

        fragBinding.chooseImage2.setOnClickListener {
            showImagePicker(image2IntentLauncher)
            fragBinding.chooseImage3.isVisible = true
            fragBinding.projectImage3.isVisible = true
        }

        fragBinding.chooseImage3.setOnClickListener {
            showImagePicker(image3IntentLauncher)
        }

        fragBinding.chooseImage2.isVisible = false // Initially hidden until previous image set
        fragBinding.projectImage2.isVisible = false // Initially hidden until previous image set
        fragBinding.chooseImage3.isVisible = false // Initially hidden until previous image set
        fragBinding.projectImage3.isVisible = false // Initially hidden until previous image set

        return root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView3) as SupportMapFragment
        mapFragment.getMapAsync {
            onMapReady(it) // Calling configure map function
        }
    }
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        projectViewModel.map = googleMap
        if (args.location.lat.equals(0.0) ) {
            if (activity?.let { checkLocationPermissions(it) } == true) {
                val task = fusedLocationProviderClient.lastLocation
                task.addOnSuccessListener { myLocation ->
                    locationUpdate(myLocation.latitude, myLocation.longitude)
                    println("this is myLocation $myLocation")
                }

                println("permission is true")
            } else {
                doPermissionLauncher()
                println("permission is false")
            }
        } else {
            locationUpdate(args.location.lat, args.location.lng)
        }
        //locationUpdate(args.location.lat, args.location.lng)
    }

    fun locationUpdate(lat: Double, lng: Double) {
        println("this is lat $lat and lng $lng")
        project.lat = lat
        project.lng = lng
        project.zoom = 15f
        projectViewModel.map.clear()
        projectViewModel.map.uiSettings?.setZoomControlsEnabled(true)
        val options = MarkerOptions().title(project.projectTitle).position(LatLng(project.lat, project.lng))
        projectViewModel.map.addMarker(options)
        projectViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(project.lat, project.lng), project.zoom))
        //showProject(project)
    }

    @SuppressLint("MissingPermission")
    private fun render(portfolio: PortfolioModel) {

        project = args.project
        println("this is the currentProject $project")

        fragBinding.projectTitle.setText(project.projectTitle)
        fragBinding.projectDescription.setText(project.projectDescription)
        projectBudget = project.projectBudget
        image = project.projectImage
        if (args.location.lat.equals(0.0)) {
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener { myLocation ->
                locationUpdate(myLocation.latitude, myLocation.longitude)
                println("this is myLocation $myLocation")
                var formattedLatitude = String.format("%.2f", myLocation.latitude); // Limit the decimal places to two
                fragBinding.projectLatitude.setText("Latitude: $formattedLatitude")
                var formattedLongitude = String.format("%.2f", myLocation.longitude); // Limit the decimal places to two
                fragBinding.projectLongitude.setText("Longitude: $formattedLongitude")
            }
        } else {
            var formattedLatitude = String.format("%.2f", args.location.lat); // Limit the decimal places to two
            fragBinding.projectLatitude.setText("Latitude: $formattedLatitude")
            var formattedLongitude = String.format("%.2f", args.location.lng); // Limit the decimal places to two
            fragBinding.projectLongitude.setText("Longitude: $formattedLongitude")
        }

        val spinner = fragBinding.projectBudgetSpinner
        spinner.adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, projectBudgets) } as SpinnerAdapter
        val spinnerPosition = projectBudgets.indexOf(projectBudget)
        spinner.setSelection(spinnerPosition)

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                projectBudget = projectBudgets[position] // Index of array and spinner position used to select project budget

                println("this is projectBudget: $projectBudget")
            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        // Set up DatePicker
        val datePicker = fragBinding.projectCompletionDatePicker
        // Set initial values if a completion date already exists

        dateDay = project.projectCompletionDay
        dateMonth = project.projectCompletionMonth
        dateYear = project.projectCompletionYear

        datePicker.init(dateYear, dateMonth, dateDay) { view, year, month, day ->
            val month = month
            val msg = "You Selected: $day/$month/$year"
            var dateProjectCompletion = "$day/${month+1}/$year"
            dateDay = day
            dateMonth = month
            dateYear = year
            // Toast is turned off, but can be turned back on
            //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            println ("this is dateDay: $dateDay")
            println ("this is dateMonth: $dateMonth")
            println ("this is dateYear: $dateYear")
            println("this is datePicker: $datePicker")
            println("this is dateProjectCompletion: $dateProjectCompletion")
        }

        if (project.projectImage.isNotEmpty()) {
            Picasso.get()
                .load(project.projectImage)
                .centerCrop()
                .resize(450, 420)
                .into(fragBinding.projectImage) }
        if (project.projectImage != "") {
            fragBinding.chooseImage.setText(R.string.button_changeImage)
        }

        if (project.projectImage2.isNotEmpty()) {
            Picasso.get()
                .load(project.projectImage2)
                .centerCrop()
                .resize(450, 420)
                .into(fragBinding.projectImage2)}

        if (project.projectImage2 != "") {
            fragBinding.chooseImage2.isVisible = true
            fragBinding.projectImage2.isVisible = true
            fragBinding.chooseImage2.setText(R.string.button_changeImage)
        }
        if (project.projectImage3.isNotEmpty()) {
            Picasso.get()
                .load(project.projectImage3)
                .centerCrop()
                .resize(450, 420)
                .into(fragBinding.projectImage3)}

        if (project.projectImage3 != "") {
            fragBinding.chooseImage3.isVisible = true
            fragBinding.projectImage3.isVisible = true
            fragBinding.chooseImage3.setText(R.string.button_changeImage)
        }
    }

    private fun getCurrentPortfolio(portfolio: PortfolioModel) {
        currentPortfolio = portfolio

        println("this is newCurrentPortfolio3 $currentPortfolio")
    }
    @SuppressLint("MissingPermission")
    fun setButtonListener(layout: FragmentProjectNewBinding) {
        if (args.location.lat.equals(0.0)) {
            println("this was the path")
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener { myLocation ->
                initialLocation.lat = myLocation.latitude
                initialLocation.lng = myLocation.longitude
            }
        } else {
            println("that was the path")
            initialLocation = args.location
        }
        println("the updated location saved $initialLocation")
    }

    // Image picker is setup for choosing project image
    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${readImageUri(result.resultCode, result.data).toString()}")
                            image = result.data!!.data!!.toString()
                            fragBinding.chooseImage.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updateProjectImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.projectImage,
                                    false, "projectImage")
                            project.projectImage = result.data!!.data!!.toString()
                            println("this is project.projectImage ${project.projectImage}")
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
        image2IntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${readImageUri(result.resultCode, result.data).toString()}")
                            fragBinding.chooseImage2.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updateProjectImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.projectImage2,
                                    false, "projectImage2")
                            project.projectImage2 = result.data!!.data!!.toString()
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
        // Image launcher for 3rd project image
        image3IntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${readImageUri(result.resultCode, result.data).toString()}")
                            fragBinding.chooseImage3.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updateProjectImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.projectImage3,
                                    false, "projectImage3")
                            project.projectImage3 = result.data!!.data!!.toString()
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
    }

    // Map is setup for selecting a location of the project
    private fun registerMapCallback() {
        mapIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Location ${result.data.toString()}")
                            val location = result.data!!.extras?.getParcelable<Location>("location")!!
                            Timber.i("Location == $location")
                            // Setting project co-ordinates based on location passed from map
                            project.lat = location.lat
                            project.lng = location.lng
                            project.zoom = location.zoom
                            // Set shown co-ordinates based on location passed from map
                            var formattedLatitude = String.format("%.2f", location.lat); // Limit the decimal places to two
                            fragBinding.projectLatitude.setText("Latitude: $formattedLatitude")
                            var formattedLongitude = String.format("%.2f", location.lng); // Limit the decimal places to two
                            fragBinding.projectLongitude.setText("Longitude: $formattedLongitude")
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_project_new, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    R.id.item_home -> {
                        findNavController().navigate(R.id.action_projectNewFragment_to_portfolioListFragment)
                    }
                    R.id.item_cancel -> {
                        val action = ProjectNewFragmentDirections.actionProjectNewFragmentToProjectListFragment(args.portfolioid)
                        findNavController().navigate(action)
                    }
                    R.id.item_project_save -> {
                        if (fragBinding.projectTitle.text.isEmpty()) {
                            Toast.makeText(context,R.string.enter_project_title, Toast.LENGTH_LONG).show()
                        } else {
                            if (project.projectImage.isNotEmpty()) {
                                project.projectImage = FirebaseImageManager.imageUriProject.value.toString()
                            }
                            if (project.projectImage2.isNotEmpty()) {
                                project.projectImage2 = FirebaseImageManager.imageUriProject2.value.toString()
                            }
                            if (project.projectImage3.isNotEmpty()) {
                                project.projectImage3 = FirebaseImageManager.imageUriProject3.value.toString()
                            }


                            val updatedProject = NewProject(projectId = generateRandomId().toString(), projectTitle = fragBinding.projectTitle.text.toString(), projectDescription = fragBinding.projectDescription.text.toString(),
                                projectBudget = projectBudget, projectImage = project.projectImage, projectImage2 = project.projectImage2, projectImage3 = project.projectImage3,
                                portfolioId = args.portfolioid, lat = initialLocation.lat, lng = initialLocation.lng, zoom = 15f, projectPortfolioType = currentPortfolio.type,
                                projectCompletionDay = dateDay, projectCompletionMonth = dateMonth, projectCompletionYear = dateYear, projectPortfolioName = currentPortfolio.title, projectUserId = loggedInViewModel.liveFirebaseUser.value?.uid!!.toString(), projectUserEmail = loggedInViewModel.liveFirebaseUser.value?.email!!)
                            if (currentPortfolio.projects == null) {
                                currentPortfolio.projects = listOf(updatedProject).toMutableList()
                            } else {
                                currentPortfolio.projects = currentPortfolio.projects?.plus(updatedProject)?.toMutableList()
                            }

                            println("this is updatedProject $updatedProject")
                            println("this is updated currentprojects ${currentPortfolio.projects}")

                            println("this is updated currentPortfolio $currentPortfolio")
                            projectViewModel.updatePortfolio(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.portfolioid, currentPortfolio)
                        }
                        val action = ProjectNewFragmentDirections.actionProjectNewFragmentToProjectListFragment(args.portfolioid)
                        findNavController().navigate(action)
                    }
                }
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

    }

    internal fun generateRandomId(): Long {
        return Random().nextLong()
    }

    @SuppressLint("MissingPermission")
    private fun doPermissionLauncher() {
        Timber.i("permission check called")
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) {
                    /*locationService?.lastLocation?.addOnSuccessListener {
                        locationUpdate(it.latitude, it.longitude)
                    }*/
                    println("permission granted")
                } else {
                    initialLocation = Location(52.245696, -7.139102, 15f)
                    println("permission not granted")
                }
            }
    }



}