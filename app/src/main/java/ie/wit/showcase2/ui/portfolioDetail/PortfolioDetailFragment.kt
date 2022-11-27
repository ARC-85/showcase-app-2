package ie.wit.showcase2.ui.portfolioDetail

import android.R
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import ie.wit.showcase2.databinding.FragmentPortfolioDetailBinding
import ie.wit.showcase2.databinding.FragmentPortfolioNewBinding
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.ui.auth.LoggedInViewModel
import ie.wit.showcase2.ui.portfolioList.PortfolioListFragmentDirections
import ie.wit.showcase2.ui.portfolioList.PortfolioListViewModel
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

    var portfolioType = "" // Current portfolio type
    var image: Uri = Uri.EMPTY
    val portfolioTypes = arrayOf("New Builds", "Renovations", "Interiors", "Landscaping", "Commercial", "Other") // Creating array of different portfolio types

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentPortfolioDetailBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        registerImagePickerCallback()

        detailViewModel = ViewModelProvider(this).get(PortfolioDetailViewModel::class.java)
        detailViewModel.observablePortfolio.observe(viewLifecycleOwner, Observer { render() })


        /*fragBinding.editPortfolioButton.setOnClickListener {
            detailViewModel.updatePortfolio(loggedInViewModel.liveFirebaseUser.value?.email!!,
                args.portfolioid, fragBinding.portfoliovm?.observablePortfolio!!.value!!)
            println(fragBinding.portfoliovm?.observablePortfolio!!.value!!)

            findNavController().navigateUp()
        }*/

        /*fragBinding.deletePortfolioButton.setOnClickListener {
            portfolioListViewModel.delete(loggedInViewModel.liveFirebaseUser.value?.email!!,
                detailViewModel.observablePortfolio.value!!)
            findNavController().navigateUp()
        }*/

        fragBinding.btnGoToProjects.setOnClickListener {
            val action = PortfolioDetailFragmentDirections.actionPortfolioDetailFragmentToProjectListFragment(
                args.portfolioid
            )
            findNavController().navigate(action)
        }

        var portfolio = detailViewModel.getPortfolio(loggedInViewModel.liveFirebaseUser.value?.email!!,
            args.portfolioid)



        println("this is portfolioid $portfolio")
        fragBinding.portfolioTitle.setText(portfolio?.title)
        fragBinding.description.setText(portfolio?.description)
        portfolioType = portfolio?.type!!
        image = portfolio?.image!!

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
                /*Toast.makeText(this@PortfolioActivity,
                    getString(R.string.selected_item) + " " +
                            "" + portfolioTypes[position], Toast.LENGTH_SHORT).show()*/
                println("this is portfolioType: $portfolioType")
            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        Picasso.get()
            .load(portfolio.image)
            .resize(450, 420)
            .centerCrop()
            .into(fragBinding.portfolioImage)

        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }

        setUpdateButtonListener(fragBinding)
        setDeleteButtonListener(fragBinding)

        return root
    }

    private fun render() {
        //fragBinding.portfolioTitle.setText("This is Title")
        //fragBinding.editUpvotes.setText("0")
        fragBinding.portfoliovm = detailViewModel
        //Timber.i("Retrofit fragBinding.donationvm == $fragBinding.donationvm")
    }

    fun setUpdateButtonListener(layout: FragmentPortfolioDetailBinding) {
        fragBinding.editPortfolioButton.setOnClickListener {
            if (layout.portfolioTitle.text.isEmpty()) {
                Toast.makeText(context, ie.wit.showcase2.R.string.enter_portfolio_title, Toast.LENGTH_LONG).show()
            } else {
                detailViewModel.updatePortfolio(loggedInViewModel.liveFirebaseUser.value?.email!!,
                    args.portfolioid, PortfolioModel(id = args.portfolioid, title = layout.portfolioTitle.text.toString(), description = layout.description.text.toString(), type = portfolioType, image = image,
                    email = loggedInViewModel.liveFirebaseUser.value?.email!!))
                println(portfolioType)
            }
            findNavController().navigate(ie.wit.showcase2.R.id.action_portfolioDetailFragment_to_portfolioListFragment)
        }
    }

    fun setDeleteButtonListener(layout: FragmentPortfolioDetailBinding) {
        fragBinding.deletePortfolioButton.setOnClickListener {
            detailViewModel.deletePortfolio(loggedInViewModel.liveFirebaseUser.value?.email!!, PortfolioModel(id = args.portfolioid, title = layout.portfolioTitle.text.toString(), description = layout.description.text.toString(), type = portfolioType, image = image,
                        email = loggedInViewModel.liveFirebaseUser.value?.email!!))
            findNavController().navigate(ie.wit.showcase2.R.id.action_portfolioDetailFragment_to_portfolioListFragment)
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
                            Timber.i("Got Result ${result.data!!.data}")
                            image = result.data!!.data!!
                            // Picasso used to get images, as well as standardising sizes and cropping as necessary
                            Picasso.get()
                                .load(image)
                                .centerCrop()
                                .resize(450, 420)
                                .into(fragBinding.portfolioImage)
                            fragBinding.chooseImage.setText(ie.wit.showcase2.R.string.button_changeImage)
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        detailViewModel.getPortfolio(loggedInViewModel.liveFirebaseUser.value?.email!!,
            args.portfolioid)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}