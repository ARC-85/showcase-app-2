package ie.wit.showcase2.ui.projectList

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.wit.showcase2.R
import ie.wit.showcase2.adapters.ProjectAdapter
import ie.wit.showcase2.adapters.ProjectListener

import ie.wit.showcase2.databinding.FragmentPortfolioListBinding
import ie.wit.showcase2.databinding.FragmentProjectListBinding
import ie.wit.showcase2.main.Showcase2App
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.ui.auth.LoggedInViewModel
import ie.wit.showcase2.ui.portfolioDetail.PortfolioDetailFragmentArgs

import ie.wit.showcase2.ui.portfolioList.PortfolioListViewModel
import ie.wit.showcase2.utils.*

class ProjectListFragment : Fragment(), ProjectListener {

    lateinit var app: Showcase2App
    private var _fragBinding: FragmentProjectListBinding? = null
    private val fragBinding get() = _fragBinding!!

    lateinit var loader : AlertDialog
    private val projectListViewModel: ProjectListViewModel by activityViewModels()
    private val args by navArgs<ProjectListFragmentArgs>()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentProjectListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        loader = createLoader(requireActivity())


        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        projectListViewModel.load(args.portfolioid)

        //showLoader(loader,"Downloading Projects")
        projectListViewModel.observableProjectsList.observe(viewLifecycleOwner, Observer {
                projects ->
            projects?.let {
                render(projects as ArrayList<NewProject>)
                hideLoader(loader)
                checkSwipeRefresh()
            }
        })

        fragBinding.fab.setOnClickListener {
            val action = ProjectListFragmentDirections.actionProjectListFragmentToProjectNewFragment(args.portfolioid)
            findNavController().navigate(action)
        }

        setSwipeRefresh()

        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showLoader(loader,"Deleting Project")
                val adapter = fragBinding.recyclerView.adapter as ProjectAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                projectListViewModel.delete(projectListViewModel.liveFirebaseUser.value?.email!!,
                    (viewHolder.itemView.tag as NewProject), args.portfolioid)
                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)

        val swipeEditHandler = object : SwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onProjectClick(viewHolder.itemView.tag as NewProject)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)

        return root
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                //(requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                //getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
                //getActivity().getActionBar().setHomeButtonEnabled(false);
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_project_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun render(projectsList: ArrayList<NewProject>) {
        fragBinding.recyclerView.adapter = ProjectAdapter(projectsList,this)
        if (projectsList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.projectsNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.projectsNotFound.visibility = View.GONE
        }
    }

    override fun onProjectClick(project: NewProject) {
        val action = ProjectListFragmentDirections.actionProjectListFragmentToProjectDetailFragment(
            project.projectId,
            args.portfolioid
        )
        findNavController().navigate(action)
    }

    fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {
            fragBinding.swiperefresh.isRefreshing = true
            showLoader(loader,"Downloading Projects")
            projectListViewModel.load(args.portfolioid)
        }
    }

    fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        //showLoader(loader,"Downloading Projects")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                projectListViewModel.liveFirebaseUser.value = firebaseUser
                projectListViewModel.load(args.portfolioid)
            }
        })
        //hideLoader(loader)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}