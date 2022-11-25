package ie.wit.showcase2.ui.projectNew

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ie.wit.showcase2.R

class ProjectNewFragment : Fragment() {

    companion object {
        fun newInstance() = ProjectNewFragment()
    }

    private lateinit var viewModel: ProjectNewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_project_new, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProjectNewViewModel::class.java)
        // TODO: Use the ViewModel
    }

}