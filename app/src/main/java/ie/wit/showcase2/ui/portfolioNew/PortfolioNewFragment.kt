package ie.wit.showcase2.ui.portfolioNew

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import ie.wit.showcase2.R
import ie.wit.showcase2.databinding.FragmentPortfolioNewBinding
import ie.wit.showcase2.firebase.FirebaseImageManager
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.ui.auth.LoggedInViewModel
import ie.wit.showcase2.ui.portfolioDetail.PortfolioDetailFragmentDirections
import ie.wit.showcase2.ui.portfolioList.PortfolioListFragment
import ie.wit.showcase2.ui.portfolioList.PortfolioListViewModel
import ie.wit.showcase2.utils.readImageUri
import ie.wit.showcase2.utils.showImagePicker
import timber.log.Timber

class PortfolioNewFragment : Fragment() {

    //var totalDonated = 0
    private var _fragBinding: FragmentPortfolioNewBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val fragBinding get() = _fragBinding!!
    private lateinit var portfolioViewModel: PortfoliolNewViewModel
    private val portfolioListViewModel: PortfolioListViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    var portfolioType = "" // Current portfolio type
    var image: String = ""
    val portfolioTypes = arrayOf("New Builds", "Renovations", "Interiors", "Landscaping", "Commercial", "Other") // Creating array of different portfolio types

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _fragBinding = FragmentPortfolioNewBinding.inflate(inflater, container, false)
        val root = fragBinding.root
	    setupMenu()
        registerImagePickerCallback()
        portfolioViewModel = ViewModelProvider(this).get(PortfoliolNewViewModel::class.java)
        portfolioViewModel.observableStatus.observe(viewLifecycleOwner, Observer {
                status -> status?.let { render(status) }
        })

        val spinner = fragBinding.portfolioTypeSpinner
        spinner.adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, portfolioTypes) } as SpinnerAdapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View?, position: Int, id: Long) {
                    portfolioType = portfolioTypes[position] // Index of array and spinner position used to select portfolio type

                    println("this is portfolioType: $portfolioType")
                }
                // No problem if nothing selected
                override fun onNothingSelected(parent: AdapterView<*>) {
                }
                }

        //fragBinding.amountPicker.setOnValueChangedListener { _, _, newVal ->
            //Display the newly selected number to paymentAmount
        //    fragBinding.paymentAmount.setText("$newVal")
        //}
        setButtonListener(fragBinding)

        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }

        return root;
    }

    private fun render(status: Boolean) {
        when (status) {
            true -> {
                view?.let {
                    //Uncomment this if you want to immediately return to Report
                    //findNavController().navigate(R.id.action_portfolioNewFragment_to_portfolioListFragment)
                }
            }
            false -> Toast.makeText(context,getString(R.string.donationError),Toast.LENGTH_LONG).show()
        }
    }

    fun setButtonListener(layout: FragmentPortfolioNewBinding) {
        layout.btnAdd.setOnClickListener {
            if (layout.portfolioTitle.text.isEmpty()) {
                Toast.makeText(context,R.string.enter_portfolio_title, Toast.LENGTH_LONG).show()
            } else {
                println(loggedInViewModel.liveFirebaseUser)
                portfolioViewModel.addPortfolio(loggedInViewModel.liveFirebaseUser, PortfolioModel(title = layout.portfolioTitle.text.toString(), description = layout.description.text.toString(), type = portfolioType,
                    email = loggedInViewModel.liveFirebaseUser.value?.email!!, profilePic = FirebaseImageManager.imageUri.value.toString()))
                println(portfolioType)
            }
            findNavController().navigate(R.id.action_portfolioNewFragment_to_portfolioListFragment)
        }
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
                            // Picasso used to get images, as well as standardising sizes and cropping as necessary
                            /*Picasso.get()
                                .load(image)
                                .centerCrop()
                                .resize(450, 420)
                                .into(fragBinding.portfolioImage)*/
                            fragBinding.chooseImage.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updatePortfolioImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.portfolioImage,
                                    false)
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
                menuInflater.inflate(R.menu.menu_portfolio_new, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    R.id.item_home -> {
                        findNavController().navigate(R.id.action_portfolioNewFragment_to_portfolioListFragment)
                    }
                    R.id.item_portfolio_save -> {
                        if (fragBinding.portfolioTitle.text.isEmpty()) {
                            Toast.makeText(context,R.string.enter_portfolio_title, Toast.LENGTH_LONG).show()
                        } else {
                            portfolioViewModel.addPortfolio(loggedInViewModel.liveFirebaseUser, PortfolioModel(title = fragBinding.portfolioTitle.text.toString(), description = fragBinding.description.text.toString(), type = portfolioType, image = image,
                                email = loggedInViewModel.liveFirebaseUser.value?.email!!))
                            println(portfolioType)
                        }
                        findNavController().navigate(R.id.action_portfolioNewFragment_to_portfolioListFragment)
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
}