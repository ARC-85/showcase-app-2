package ie.wit.showcase2.ui.portfolioDetail

import android.R
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.squareup.picasso.Picasso
import ie.wit.showcase2.databinding.FragmentPortfolioDetailBinding
import ie.wit.showcase2.databinding.FragmentPortfolioNewBinding
import ie.wit.showcase2.firebase.FirebaseImageManager
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.ui.auth.LoggedInViewModel
import ie.wit.showcase2.ui.portfolioList.PortfolioListFragmentDirections
import ie.wit.showcase2.ui.portfolioList.PortfolioListViewModel
import ie.wit.showcase2.ui.projectList.ProjectListFragmentDirections
import ie.wit.showcase2.utils.hideLoader
import ie.wit.showcase2.utils.readImageUri
import ie.wit.showcase2.utils.showImagePicker
import timber.log.Timber



class PortfolioDetailFragment : Fragment() {

    private lateinit var detailViewModel: PortfolioDetailViewModel
    private val args by navArgs<PortfolioDetailFragmentArgs>()
    private var _fragBinding: FragmentPortfolioDetailBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val portfolioListViewModel : PortfolioListViewModel by activityViewModels()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    var currentPortfolio = PortfolioModel()
    var imageLoad: Boolean = false
    var projects: MutableList<NewProject>? = null
    var testing: Boolean = false

    var portfolioType = "" // Current portfolio type
    var image: String = ""
    val portfolioTypes = arrayOf("New Builds", "Renovations", "Interiors", "Landscaping", "Commercial", "Other") // Creating array of different portfolio types

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentPortfolioDetailBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        registerImagePickerCallback()

        detailViewModel = ViewModelProvider(this).get(PortfolioDetailViewModel::class.java)
        //detailViewModel.observablePortfolio.observe(viewLifecycleOwner, Observer { render() })


        detailViewModel.observablePortfolio.observe(viewLifecycleOwner, Observer {
                portfolio ->
            portfolio?.let {
                render(portfolio)
                getCurrentPortfolio(portfolio)
                currentPortfolio = portfolio
                println("this is currentPortfolio $currentPortfolio")
            }
        })

        println("this is currentPortfolio2 $currentPortfolio")

        var test = detailViewModel.getPortfolio(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.portfolioid)
        println("this is test $test")

        setupMenu()


        fragBinding.btnGoToProjects.setOnClickListener {
            val action = PortfolioDetailFragmentDirections.actionPortfolioDetailFragmentToProjectListFragment(
                args.portfolioid
            )
            findNavController().navigate(action)
        }


        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }


        return root
    }

    private fun render(portfolio: PortfolioModel) {

        fragBinding.portfoliovm = detailViewModel
        fragBinding.portfolioName.setText(portfolio.title)

        projects = portfolio.projects
        portfolioType = portfolio?.type.toString()
        if (!imageLoad) {
            image = portfolio?.image.toString()
        }
        val spinner = fragBinding.portfolioTypeSpinner
        val adapter = activity?.applicationContext?.let { ArrayAdapter(it, R.layout.simple_spinner_item, portfolioTypes) } as SpinnerAdapter
        spinner.adapter = adapter
        val spinnerPosition = portfolioTypes.indexOf(portfolioType)
        spinner.setSelection(spinnerPosition)
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                portfolioType = portfolioTypes[position] // Index of array and spinner position used to select portfolio type
                // The toast message was taken out because it was annoying, but can be reinstated if wanted

                println("this is portfolioType: $portfolioType")
            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        println("portfolio.image in render ${portfolio?.image}")
        println("image in render $image")
        if (image.isNotEmpty()) {
            fragBinding.chooseImage.setText(ie.wit.showcase2.R.string.button_changeImage)
            Picasso.get()
                .load(image)
                .resize(450, 420)
                .centerCrop()
                .into(fragBinding.portfolioImage) }



    }

    private fun getCurrentPortfolio(portfolio: PortfolioModel) {
        currentPortfolio = portfolio
        println("this is currentPortfolio3 $currentPortfolio")
    }



    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(ie.wit.showcase2.R.menu.menu_portfolio_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    ie.wit.showcase2.R.id.item_cancel -> {
                        findNavController().navigate(ie.wit.showcase2.R.id.action_portfolioDetailFragment_to_portfolioListFragment)

                    }
                    ie.wit.showcase2.R.id.item_portfolio_save -> {
                        if (fragBinding.portfolioTitle.text.isEmpty()) {
                            Toast.makeText(context, ie.wit.showcase2.R.string.enter_portfolio_title, Toast.LENGTH_LONG).show()
                        } else {
                            if(imageLoad) {
                                image = FirebaseImageManager.imageUriPortfolio.value.toString()
                                println("tell me imageUriValue ${FirebaseImageManager.imageUriPortfolio.value.toString()}")
                            }
                            detailViewModel.updatePortfolio(loggedInViewModel.liveFirebaseUser.value?.uid!!,
                                args.portfolioid, PortfolioModel(uid = args.portfolioid, title = fragBinding.portfolioTitle.text.toString(), description = fragBinding.description.text.toString(), type = portfolioType,
                                    email = loggedInViewModel.liveFirebaseUser.value?.email!!, projects = projects, profilePic = FirebaseImageManager.imageUri.value.toString(), image = image))
                            println("tell me image $image")
                        }
                        findNavController().navigate(ie.wit.showcase2.R.id.action_portfolioDetailFragment_to_portfolioListFragment)
                    }

                    ie.wit.showcase2.R.id.item_portfolio_delete -> {
                        projects?.forEach {
                            detailViewModel.removeFavourite(loggedInViewModel.liveFirebaseUser.value?.uid!!, it.projectId)
                        }
                        detailViewModel.deletePortfolio(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.portfolioid)
                        findNavController().navigate(ie.wit.showcase2.R.id.action_portfolioDetailFragment_to_portfolioListFragment)

                    }

                    ie.wit.showcase2.R.id.item_goToProjects -> {
                        val action = PortfolioDetailFragmentDirections.actionPortfolioDetailFragmentToProjectListFragment(
                            args.portfolioid
                        )
                        findNavController().navigate(action)
                    }

                }
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    // Image picker is setup for choosing portfolio image
    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${readImageUri(result.resultCode, result.data).toString()}")
                            image = result.data!!.data!!.toString()
                            println("image in imageLauncher $image")
                            // Picasso used to get images, as well as standardising sizes and cropping as necessary
                            /*Picasso.get()
                                .load(image)
                                .centerCrop()
                                .resize(450, 420)
                                .into(fragBinding.portfolioImage)*/
                            fragBinding.chooseImage.setText(ie.wit.showcase2.R.string.button_changeImage)
                            FirebaseImageManager
                                .updatePortfolioImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.portfolioImage,
                                    true)
                            imageLoad = true
                            testing = true

                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        detailViewModel.getPortfolio(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.portfolioid)
        println("onResume is used")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}