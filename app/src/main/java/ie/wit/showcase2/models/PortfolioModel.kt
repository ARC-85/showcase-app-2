package ie.wit.showcase2.models

import android.os.Parcelable
import android.net.Uri
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class PortfolioModel(var uid: String? = "",
                          var title: String = "",
                          var description: String = "",
                          var type: String = "",
                          var image: String = "",
                          val email: String? = "joe@bloggs.com",
                          var projects: Array<NewProject>? = null) : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "title" to title,
            "description" to description,
            "type" to type,
            "image" to image,
            "email" to email,
            "projects" to projects
        )
    }
}

@IgnoreExtraProperties
@Parcelize
data class NewProject(var projectId: String = "",
                      var portfolioId: String = "",
                      var projectPortfolioName: String = "",
                      var projectTitle: String = "",
                      var projectDescription: String = "",
                      var projectImage: String = "",
                      var projectImage2: String = "",
                      var projectImage3: String = "",
                      var lat : Double = 0.0,
                      var lng: Double = 0.0,
                      var zoom: Float = 0f,
                      var projectCompletionDay: Int = 1,
                      var projectCompletionMonth: Int = 1,
                      var projectCompletionYear: Int = 1900,
                      var projectBudget: String = "") : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "projectId" to projectId,
            "portfoloId" to portfolioId,
            "projectPortfolioName" to projectPortfolioName,
            "projectTitle" to projectTitle,
            "projectDescription" to projectDescription,
            "projectImage" to projectImage,
            "projectImage2" to projectImage2,
            "projectImage3" to projectImage3,
            "lat" to lat,
            "lng" to lng,
            "zoom" to zoom,
            "projectCompletionDay" to projectCompletionDay,
            "projectCompletionMonth" to projectCompletionMonth,
            "projectCompletionYear" to projectCompletionYear,
            "projectBudget" to projectBudget
        )
    }
}

@IgnoreExtraProperties
@Parcelize
data class Location(var lat: Double = 0.0,
                    var lng: Double = 0.0,
                    var zoom: Float = 0f) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        "lat" to lat
        "lng" to lng
        "zoom" to zoom
        return mapOf()
    }
}



