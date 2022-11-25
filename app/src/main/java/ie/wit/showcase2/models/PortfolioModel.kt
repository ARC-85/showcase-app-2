package ie.wit.showcase2.models

import android.os.Parcelable
import android.net.Uri
import kotlinx.parcelize.Parcelize

@Parcelize
data class PortfolioModel(var id: Long = 0,
                          var title: String = "",
                          var description: String = "",
                          var type: String = "",
                          var image: Uri = Uri.EMPTY,
                          val email: String = "joe@bloggs.com",
                          var projects: Array<NewProject>? = null) : Parcelable

@Parcelize
data class NewProject(var projectId: Long = 0,
                      var portfolioId: Long = 0,
                      var projectPortfolioName: String = "",
                      var projectTitle: String = "",
                      var projectDescription: String = "",
                      var projectImage: Uri = Uri.EMPTY,
                      var projectImage2: Uri = Uri.EMPTY,
                      var projectImage3: Uri = Uri.EMPTY,
                      var lat : Double = 0.0,
                      var lng: Double = 0.0,
                      var zoom: Float = 0f,
                      var projectCompletionDay: Int = 1,
                      var projectCompletionMonth: Int = 1,
                      var projectCompletionYear: Int = 1900,
                      var projectBudget: String = "") : Parcelable

@Parcelize
data class Location(var lat: Double = 0.0,
                    var lng: Double = 0.0,
                    var zoom: Float = 0f) : Parcelable



