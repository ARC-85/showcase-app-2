package ie.wit.showcase2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit.showcase2.R
import ie.wit.showcase2.databinding.CardPortfolioBinding
import ie.wit.showcase2.models.PortfolioModel

interface PortfolioClickListener {
    fun onPortfolioClick(portfolio: PortfolioModel)
}

class PortfolioAdapter constructor(private var portfolios: ArrayList<PortfolioModel>,
                                  private val listener: PortfolioClickListener)
    : RecyclerView.Adapter<PortfolioAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardPortfolioBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val portfolio = portfolios[holder.adapterPosition]
        holder.bind(portfolio,listener)
    }

    fun removeAt(position: Int) {
        portfolios.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = portfolios.size

    inner class MainHolder(val binding : CardPortfolioBinding) :
                            RecyclerView.ViewHolder(binding.root) {

        fun bind(portfolio: PortfolioModel, listener: PortfolioClickListener) {
            binding.root.tag = portfolio
            binding.portfolio = portfolio
            Picasso.get().load(portfolio.image).resize(200,200).into(binding.imageIcon)
            binding.root.setOnClickListener { listener.onPortfolioClick(portfolio) }
            binding.executePendingBindings()
        }
    }
}