package ie.wit.showcase2.ui.projectDetail

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ie.wit.showcase2.R

class projectDetailFragment : Fragment() {

    companion object {
        fun newInstance() = projectDetailFragment()
    }

    private lateinit var viewModel: ProjectDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_project_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProjectDetailViewModel::class.java)
        // TODO: Use the ViewModel
    }

}