package ie.wit.showcase2.ui.projectMap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ie.wit.showcase2.models.Location
import ie.wit.showcase2.models.PortfolioModel

class ProjectMapViewModel : ViewModel() {
    var location = MutableLiveData<Location>()
    lateinit var map : GoogleMap

    var observableLocation: LiveData<Location>
        get() = location
        set(value) {
            location.value = value.value
        }


}