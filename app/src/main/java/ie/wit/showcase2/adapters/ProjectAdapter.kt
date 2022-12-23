package ie.wit.showcase2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit.showcase2.R
import ie.wit.showcase2.databinding.CardPortfolioBinding
import ie.wit.showcase2.databinding.CardProjectBinding
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.ui.auth.LoggedInViewModel


interface ProjectListener {
    fun onProjectClick(project: NewProject)
}

class ProjectAdapter constructor(private var projects: ArrayList<NewProject>,
                                 private val listener: ProjectListener) :
    RecyclerView.Adapter<ProjectAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        //binding project card
        val binding = CardProjectBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        //project related to specific adapter position selected
        val project = projects[holder.adapterPosition]
        holder.bind(project, listener)
    }

    fun removeAt(position: Int) {
        projects.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = projects.size

    inner class MainHolder(val binding : CardProjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(project: NewProject, listener: ProjectListener) {
            // Function to bind different values to the project adapter card
            binding.root.tag = project
            binding.project = project
            binding.projectTitle.text = project.projectTitle
            binding.projectBudget.text = project.projectBudget
            binding.projectDescription.text = project.projectDescription
            if (project.projectImage.isNotEmpty()) {
                Picasso.get().load(project.projectImage).resize(200,200).into(binding.projectImageIcon)
            }
            //favourite star only shown if favourite user ID matching with project user ID
            val projectFavouriteId = project.projectFavourites?.find { p -> p == project.projectUserId }
            if (projectFavouriteId == null) {
                binding.imageFavourite.visibility = View.GONE
            } else {
                binding.imageFavourite.visibility = View.VISIBLE
            }
            binding.root.setOnClickListener { listener.onProjectClick(project) }

        }
    }
}