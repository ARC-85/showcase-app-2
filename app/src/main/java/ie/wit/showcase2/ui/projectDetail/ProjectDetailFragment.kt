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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.squareup.picasso.Picasso
import ie.wit.showcase2.R
import ie.wit.showcase2.databinding.FragmentProjectDetailBinding
import ie.wit.showcase2.databinding.FragmentProjectNewBinding
import ie.wit.showcase2.models.Location
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.ui.auth.LoggedInViewModel
import ie.wit.showcase2.ui.map.MapProject
import ie.wit.showcase2.ui.projectList.ProjectListViewModel
import ie.wit.showcase2.ui.projectNew.ProjectNewFragmentArgs
import ie.wit.showcase2.ui.projectNew.ProjectNewFragmentDirections
import ie.wit.showcase2.ui.projectNew.ProjectNewViewModel
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
    var image: Uri = Uri.EMPTY
    val projectBudgets = arrayOf("Show All", "€0-€50K", "€50K-€100K", "€100K-€250K", "€250K-€500K", "€500K-€1M", "€1M+") // Creating array of different project budgets
    val today = Calendar.getInstance()
    var dateDay = today.get(Calendar.DAY_OF_MONTH)
    var dateMonth = today.get(Calendar.MONTH)
    var dateYear = today.get(Calendar.YEAR)
    var project = NewProject()


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
        project = projectViewModel.getProject(loggedInViewModel.liveFirebaseUser.value?.email!!, args.portfolioid, args.projectid)!!

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

        Picasso.get()
            .load(project.projectImage)
            .centerCrop()
            .resize(450, 420)
            .into(fragBinding.projectImage)
        if (project.projectImage != Uri.EMPTY) {
            fragBinding.chooseImage.setText(R.string.button_changeImage)
        }
        Picasso.get()
            .load(project.projectImage2)
            .centerCrop()
            .resize(450, 420)
            .into(fragBinding.projectImage2)
        if (project.projectImage2 != Uri.EMPTY) {
            fragBinding.chooseImage2.isVisible = true
            fragBinding.projectImage2.isVisible = true
            fragBinding.chooseImage2.setText(R.string.button_changeImage)
        }
        Picasso.get()
            .load(project.projectImage3)
            .centerCrop()
            .resize(450, 420)
            .into(fragBinding.projectImage3)
        if (project.projectImage3 != Uri.EMPTY) {
            fragBinding.chooseImage3.isVisible = true
            fragBinding.projectImage3.isVisible = true
            fragBinding.chooseImage3.setText(R.string.button_changeImage)
        }

        return root;
    }

    private fun render() {
        //fragBinding.portfolioTitle.setText("This is Title")
        //fragBinding.editUpvotes.setText("0")
        //fragBinding.portfoliovm = detailViewModel
        //Timber.i("Retrofit fragBinding.donationvm == $fragBinding.donationvm")
    }

    fun setButtonListener(layout: FragmentProjectDetailBinding) {
        layout.editProjectButton.setOnClickListener {
            if (layout.projectTitle.text.isEmpty()) {
                Toast.makeText(context,R.string.enter_project_title, Toast.LENGTH_LONG).show()
            } else {
                val portfolio = projectViewModel.getPortfolio(loggedInViewModel.liveFirebaseUser.value?.email!!,
                    args.portfolioid)
                projectViewModel.updateProject(loggedInViewModel.liveFirebaseUser.value?.email!!,
                    NewProject(projectId = args.projectid, projectTitle = layout.projectTitle.text.toString(), projectDescription = layout.projectDescription.text.toString(),
                        projectBudget = projectBudget, projectImage = image, projectImage2 = project.projectImage2, projectImage3 = project.projectImage3,
                        projectPortfolioName = portfolio!!.title, portfolioId = args.portfolioid, lat = project.lat, lng = project.lng,
                        projectCompletionDay = dateDay, projectCompletionMonth = dateMonth, projectCompletionYear = dateYear),
                    args.portfolioid)
            }
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
                            Timber.i("Got Result ${result.data!!.data}")
                            image = result.data!!.data!!
                            // Picasso used to get images, as well as standardising sizes and cropping as necessary
                            Picasso.get()
                                .load(image)
                                .centerCrop()
                                .resize(450, 420)
                                .into(fragBinding.projectImage)
                            fragBinding.chooseImage.setText(R.string.button_changeImage)
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
                            Timber.i("Got Result ${result.data!!.data}")
                            project.projectImage2 = result.data!!.data!!
                            Picasso.get()
                                .load(project.projectImage2)
                                .centerCrop()
                                .resize(450, 420)
                                .into(fragBinding.projectImage2)
                            fragBinding.chooseImage2.setText(R.string.button_changeImage)
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
                            Timber.i("Got Result ${result.data!!.data}")
                            project.projectImage3 = result.data!!.data!!
                            Picasso.get()
                                .load(project.projectImage3)
                                .centerCrop()
                                .resize(450, 420)
                                .into(fragBinding.projectImage3)
                            fragBinding.chooseImage3.setText(R.string.button_changeImage)
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
                menuInflater.inflate(R.menu.menu_project_new, menu)
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

    }

}