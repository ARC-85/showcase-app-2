package ie.wit.showcase2.ui.portfolioList

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.wit.showcase2.R
import ie.wit.showcase2.adapters.PortfolioAdapter
import ie.wit.showcase2.adapters.PortfolioClickListener
import ie.wit.showcase2.databinding.FragmentPortfolioListBinding
import ie.wit.showcase2.main.Showcase2App
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.ui.auth.LoggedInViewModel
import ie.wit.showcase2.utils.*

class PortfolioListFragment : Fragment(), PortfolioClickListener {

    lateinit var app: Showcase2App
    private var _fragBinding: FragmentPortfolioListBinding? = null
    private val fragBinding get() = _fragBinding!!
    val portfolioTypes = arrayOf("Show All", "New Builds", "Renovations", "Interiors", "Landscaping", "Commercial", "Other") // Creating array of different portfolio types
    lateinit var loader : AlertDialog
    private val portfolioListViewModel: PortfolioListViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    var portfolioType = "" // Selected portfolio type for filtering list
    var list = ArrayList<PortfolioModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentPortfolioListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
	    setupMenu()
        loader = createLoader(requireActivity())

        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        //portfolioListViewModel = ViewModelProvider(this).get(PortfolioListViewModel::class.java)
        showLoader(loader,"Downloading Portfolios")
        portfolioListViewModel.observablePortfoliosList.observe(viewLifecycleOwner, Observer {
                portfolios ->
            portfolios?.let {
                render(portfolios as ArrayList<PortfolioModel>)
                hideLoader(loader)
                checkSwipeRefresh()
            }
        })

        fragBinding.fab.setOnClickListener {
            val action = PortfolioListFragmentDirections.actionPortfolioListFragmentToPortfolioNewFragment()
            findNavController().navigate(action)
        }

        setSwipeRefresh()

        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showLoader(loader,"Deleting Portfolio")
                val adapter = fragBinding.recyclerView.adapter as PortfolioAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                portfolioListViewModel.delete(portfolioListViewModel.liveFirebaseUser.value?.uid!!,
                    (viewHolder.itemView.tag as PortfolioModel).uid!!)
                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)

        val swipeEditHandler = object : SwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onPortfolioClick(viewHolder.itemView.tag as PortfolioModel)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)

        val spinner = fragBinding.portfolioTypeSpinner
        val adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, portfolioTypes) } as SpinnerAdapter
        spinner.adapter = adapter
        //val spinnerPosition = portfolioTypes.indexOf(portfolioType)
        //spinner.setSelection(spinnerPosition)
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
                portfolioListViewModel.observablePortfoliosList.observe(viewLifecycleOwner, Observer {
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

        return root
    }

  private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_portfolio_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun render(portfoliosList: ArrayList<PortfolioModel>) {
        if (portfolioType != "Show All") {
            list = ArrayList(portfoliosList.filter { p -> p.type == portfolioType })
            println("this is internal list $list")
            fragBinding.recyclerView.adapter = PortfolioAdapter(list,this)
        } else {
            list = portfoliosList
        }
        println("this is portfoliosList $portfoliosList")
        println("this is list $list")
        fragBinding.recyclerView.adapter = PortfolioAdapter(list,this)
        if (portfoliosList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.portfoliosNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.portfoliosNotFound.visibility = View.GONE
        }
    }

    override fun onPortfolioClick(portfolio: PortfolioModel) {
        val action = PortfolioListFragmentDirections.actionPortfolioListFragmentToPortfolioDetailFragment(
            portfolio.uid!!
        )
        findNavController().navigate(action)
    }

    fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {
            fragBinding.swiperefresh.isRefreshing = true
            showLoader(loader,"Downloading Portfolios")
            portfolioListViewModel.load()
        }
    }

    fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader,"Downloading Portfolios")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                portfolioListViewModel.liveFirebaseUser.value = firebaseUser
                portfolioListViewModel.load()
            }
        })
        //hideLoader(loader)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}