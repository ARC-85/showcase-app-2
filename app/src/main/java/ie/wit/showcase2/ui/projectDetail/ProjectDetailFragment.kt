package ie.wit.showcase2.ui.projectDetail

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
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
import com.squareup.picasso.Picasso
import ie.wit.showcase2.R
import ie.wit.showcase2.databinding.FragmentPortfolioDetailBinding
import ie.wit.showcase2.databinding.FragmentProjectDetailBinding
import ie.wit.showcase2.databinding.FragmentProjectNewBinding
import ie.wit.showcase2.firebase.FirebaseImageManager
import ie.wit.showcase2.models.Location
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioManager
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.ui.auth.LoggedInViewModel
import ie.wit.showcase2.ui.map.MapProject
import ie.wit.showcase2.ui.projectList.ProjectListViewModel
import ie.wit.showcase2.ui.projectNew.ProjectNewFragmentArgs
import ie.wit.showcase2.ui.projectNew.ProjectNewFragmentDirections
import ie.wit.showcase2.ui.projectNew.ProjectNewViewModel
import ie.wit.showcase2.utils.readImageUri
import ie.wit.showcase2.utils.showImagePicker
import timber.log.Timber
import java.util.*

class ProjectDetailFragment : Fragment() {

    private var _fragBinding: FragmentProjectDetailBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val fragBinding get() = _fragBinding!!
    private lateinit var projectViewModel: ProjectDetailViewModel
    private val args by navArgs<ProjectDetailFragmentArgs>()
    private val projectListViewModel: ProjectListViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var image2IntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var image3IntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var mapIntentLauncher : ActivityResultLauncher<Intent>
    var projectBudget = "Show All" // Project budget initial selection
    var image: String = ""
    val projectBudgets = arrayOf("Show All", "€0-€50K", "€50K-€100K", "€100K-€250K", "€250K-€500K", "€500K-€1M", "€1M+") // Creating array of different project budgets
    val today = Calendar.getInstance()
    var dateDay = today.get(Calendar.DAY_OF_MONTH)
    var dateMonth = today.get(Calendar.MONTH)
    var dateYear = today.get(Calendar.YEAR)
    var project = NewProject()
    var currentPortfolio = PortfolioModel()
    var currentProject = NewProject()
    var projectImageUpdate: Boolean = false
    var projectImage2Update: Boolean = false
    var projectImage3Update: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _fragBinding = FragmentProjectDetailBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        registerImagePickerCallback()
        registerMapCallback()
        projectViewModel = ViewModelProvider(this).get(ProjectDetailViewModel::class.java)
        //projectViewModel.observableProject.observe(viewLifecycleOwner, Observer { render() })

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

        //project = projectViewModel.getProject(loggedInViewModel.liveFirebaseUser.value?.email!!, args.portfolioid, args.projectid)!!

        /*fragBinding.projectTitle.setText(project.projectTitle)
        fragBinding.projectDescription.setText(project.projectDescription)
        projectBudget = project.projectBudget
        image = project.projectImage
        var formattedLatitude = String.format("%.2f", project.lat); // Limit the decimal places to two
        fragBinding.projectLatitude.setText("Latitude: $formattedLatitude")
        var formattedLongitude = String.format("%.2f", project.lng); // Limit the decimal places to two
        fragBinding.projectLongitude.setText("Longitude: $formattedLongitude")


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
        }*/

        fragBinding.projectLocation.setOnClickListener {
            Timber.i("Set Location Pressed")
        }

        // Set the initial values for location if a new location is set, passing details of location and project to the map activity
        fragBinding.projectLocation.setOnClickListener {
            val location = Location(project.lat, project.lng, project.zoom)

            val launcherIntent = Intent(activity, MapProject::class.java)
                .putExtra("location", location)
            //.putExtra("project_edit", project)
            mapIntentLauncher.launch(launcherIntent)
        }

        /*

        // Set up DatePicker
        val datePicker = fragBinding.projectCompletionDatePicker
        // Set initial values if a completion date already exists
        dateDay = project.projectCompletionDay
        dateMonth = project.projectCompletionMonth - 1
        dateYear = project.projectCompletionYear
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

         */

        setUpdateButtonListener(fragBinding)
        setDeleteButtonListener(fragBinding)

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

        /*

        Picasso.get()
            .load(project.projectImage)
            .centerCrop()
            .resize(450, 420)
            .into(fragBinding.projectImage)
        if (project.projectImage != "") {
            fragBinding.chooseImage.setText(R.string.button_changeImage)
        }
        Picasso.get()
            .load(project.projectImage2)
            .centerCrop()
            .resize(450, 420)
            .into(fragBinding.projectImage2)
        if (project.projectImage2 != "") {
            fragBinding.chooseImage2.isVisible = true
            fragBinding.projectImage2.isVisible = true
            fragBinding.chooseImage2.setText(R.string.button_changeImage)
        }
        Picasso.get()
            .load(project.projectImage3)
            .centerCrop()
            .resize(450, 420)
            .into(fragBinding.projectImage3)
        if (project.projectImage3 != "") {
            fragBinding.chooseImage3.isVisible = true
            fragBinding.projectImage3.isVisible = true
            fragBinding.chooseImage3.setText(R.string.button_changeImage)
        }

         */

        return root;
    }

    private fun getCurrentPortfolio(portfolio: PortfolioModel) {
        currentPortfolio = portfolio
        println("this is newCurrentPortfolio3 $currentPortfolio")
    }

    private fun render(portfolio: PortfolioModel) {
        project = portfolio.projects?.find { p -> p.projectId == args.projectid }!!
        println("this is the currentProject $project")

        fragBinding.projectTitle.setText(project.projectTitle)
        fragBinding.projectDescription.setText(project.projectDescription)
        projectBudget = project.projectBudget
        image = project.projectImage
        var formattedLatitude = String.format("%.2f", project.lat); // Limit the decimal places to two
        fragBinding.projectLatitude.setText("Latitude: $formattedLatitude")
        var formattedLongitude = String.format("%.2f", project.lng); // Limit the decimal places to two
        fragBinding.projectLongitude.setText("Longitude: $formattedLongitude")


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
        dateMonth = project.projectCompletionMonth - 1
        dateYear = project.projectCompletionYear
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
        //fragBinding.portfolioTitle.setText("This is Title")
        //fragBinding.editUpvotes.setText("0")
        //fragBinding.portfoliovm = detailViewModel
        //Timber.i("Retrofit fragBinding.donationvm == $fragBinding.donationvm")
    }

    fun setUpdateButtonListener(layout: FragmentProjectDetailBinding) {
        layout.editProjectButton.setOnClickListener {
            if (layout.projectTitle.text.isEmpty()) {
                Toast.makeText(context,R.string.enter_project_title, Toast.LENGTH_LONG).show()
            } else {
                if (projectImageUpdate) {
                    project.projectImage = FirebaseImageManager.imageUriProject.value.toString()
                }
                if (projectImage2Update) {
                    project.projectImage2 = FirebaseImageManager.imageUriProject2.value.toString()
                }
                if (projectImage3Update) {
                    project.projectImage3 = FirebaseImageManager.imageUriProject3.value.toString()
                }
                var updatedProject = NewProject(projectId = args.projectid, projectTitle = layout.projectTitle.text.toString(), projectDescription = layout.projectDescription.text.toString(),
                    projectBudget = projectBudget, projectImage = project.projectImage, projectImage2 = project.projectImage2, projectImage3 = project.projectImage3,
                    projectPortfolioName = currentPortfolio!!.title, portfolioId = args.portfolioid, lat = project.lat, lng = project.lng,
                    projectCompletionDay = dateDay, projectCompletionMonth = dateMonth, projectCompletionYear = dateYear)

                if (currentPortfolio.projects != null) { // If the portfolio has projects (as expected)
                    var projectIdList =
                        arrayListOf<String>() // Create a arrayList variable for storing project IDs
                    currentPortfolio.projects!!.forEach { // For each project in the relevant portfolio, add the project ID to the list of project IDs
                        projectIdList += it.projectId
                    }
                    println("this is projectIdList: $projectIdList")
                    var projectId = updatedProject.projectId
                    println("this is projectId: $projectId")
                    val index =
                        projectIdList.indexOf(updatedProject.projectId) // Find the index position of the project ID that matches the ID of the project that was passed
                    println("this is index: $index")
                    var portfolioProjects1 =
                        currentPortfolio.projects!! // Create a list of the projects from the passed portfolio
                    var short =
                        portfolioProjects1.removeAt(index) // Remove the project at the previously found index position within the created project list
                    println("this is short: $short")
                    portfolioProjects1 =
                        portfolioProjects1.plus(updatedProject) as MutableList<NewProject> // Add the passed project to the shortened list of projects
                    currentPortfolio.projects =
                        ArrayList(portfolioProjects1) // Assign the new list of projects to the found portfolio

                    println("this is updated portfolio projects ${currentPortfolio.projects}")
                }


                projectViewModel.updatePortfolio(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.portfolioid, currentPortfolio)
            }
            val action = ProjectDetailFragmentDirections.actionProjectDetailFragmentToProjectListFragment(args.portfolioid)
            findNavController().navigate(action)
        }
    }



    fun setDeleteButtonListener(layout: FragmentProjectDetailBinding) {
        fragBinding.deleteProjectButton.setOnClickListener {

            if (currentPortfolio.projects != null) { // If the portfolio has projects (as expected)
                var projectIdList =
                    arrayListOf<String>() // Create a arrayList variable for storing project IDs
                currentPortfolio.projects!!.forEach { // For each project in the relevant portfolio, add the project ID to the list of project IDs
                    projectIdList += it.projectId
                }
                println("this is projectIdList: $projectIdList")
                var projectId = args.projectid
                println("this is projectId: $projectId")
                val index =
                    projectIdList.indexOf(args.projectid) // Find the index position of the project ID that matches the ID of the project that was passed
                println("this is index: $index")
                var portfolioProjects1 =
                    currentPortfolio.projects!! // Create a list of the projects from the passed portfolio
                var short =
                    portfolioProjects1.removeAt(index) // Remove the project at the previously found index position within the created project list
                println("this is short: $short")

                currentPortfolio.projects =
                    ArrayList(portfolioProjects1) // Assign the new list of projects to the found portfolio

                println("this is updated portfolio projects ${currentPortfolio.projects}")
            }

            projectViewModel.updatePortfolio(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.portfolioid, currentPortfolio)

        val action = ProjectDetailFragmentDirections.actionProjectDetailFragmentToProjectListFragment(args.portfolioid)
        findNavController().navigate(action)
        }
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
                            projectImageUpdate = true
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
                            fragBinding.chooseImage.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updateProjectImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.projectImage2,
                                    false, "projectImage2")
                            project.projectImage2 = result.data!!.data!!.toString()
                            projectImage2Update = true
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
                            fragBinding.chooseImage.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updateProjectImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.projectImage3,
                                    false, "projectImage3")
                            project.projectImage3 = result.data!!.data!!.toString()
                            projectImage3Update = true
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
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_project_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    R.id.item_home -> {
                        findNavController().navigate(R.id.action_projectDetailFragment_to_portfolioListFragment)
                    }
                    R.id.item_cancel -> {
                        val action = ProjectDetailFragmentDirections.actionProjectDetailFragmentToProjectListFragment(args.portfolioid)
                        findNavController().navigate(action)
                    }
                    /*R.id.item_project_save -> {
                        if (fragBinding.projectTitle.text.isEmpty()) {
                            Toast.makeText(context,R.string.enter_project_title, Toast.LENGTH_LONG).show()
                        } else {
                            val portfolio = projectViewModel.getPortfolio(loggedInViewModel.liveFirebaseUser.value?.email!!,
                                args.portfolioid)
                            projectViewModel.updateProject(loggedInViewModel.liveFirebaseUser.value?.email!!,
                                NewProject(projectId = args.projectid, projectTitle = fragBinding.projectTitle.text.toString(), projectDescription = fragBinding.projectDescription.text.toString(),
                                    projectBudget = projectBudget, projectImage = image, projectImage2 = project.projectImage2, projectImage3 = project.projectImage3,
                                    projectPortfolioName = portfolio!!.title, portfolioId = args.portfolioid, lat = project.lat, lng = project.lng,
                                    projectCompletionDay = dateDay, projectCompletionMonth = dateMonth, projectCompletionYear = dateYear),
                                args.portfolioid)
                        }
                        val action = ProjectDetailFragmentDirections.actionProjectDetailFragmentToProjectListFragment(args.portfolioid)
                        findNavController().navigate(action)
                    }
                    R.id.item_project_delete -> {
                            projectViewModel.deleteProject(loggedInViewModel.liveFirebaseUser.value?.email!!, args.projectid, args.portfolioid)
                            val action = ProjectDetailFragmentDirections.actionProjectDetailFragmentToProjectListFragment(args.portfolioid)
                            findNavController().navigate(action)
                    }*/
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

}