package ie.wit.showcase2.ui.projectMap

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ie.wit.showcase2.R
import ie.wit.showcase2.databinding.FragmentPortfolioNewBinding
import ie.wit.showcase2.databinding.FragmentProjectMapBinding

import ie.wit.showcase2.firebase.FirebaseImageManager
import ie.wit.showcase2.models.Location
import ie.wit.showcase2.models.NewProject
import ie.wit.showcase2.models.PortfolioModel
import ie.wit.showcase2.ui.portfolioDetail.PortfolioDetailFragmentDirections
import ie.wit.showcase2.ui.projectDetail.ProjectDetailFragmentArgs
import ie.wit.showcase2.ui.projectNew.ProjectNewViewModel


class ProjectMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener,
    GoogleMap.OnMarkerClickListener {

    private lateinit var projectMapViewModel: ProjectMapViewModel
    private val args by navArgs<ProjectDetailFragmentArgs>()
    private var _fragBinding: FragmentProjectMapBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val fragBinding get() = _fragBinding!!

    //lateinit var map : GoogleMap
    var location = Location()
    var project = NewProject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                          savedInstanceState: Bundle?): View? {

        location = args.location
        _fragBinding = FragmentProjectMapBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        projectMapViewModel = ViewModelProvider(this).get(ProjectMapViewModel::class.java)


        setButtonListener(fragBinding)
        return root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView2) as SupportMapFragment
        mapFragment.getMapAsync{

            onMapReady(it)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        projectMapViewModel.map = googleMap
        val loc = LatLng(location.lat, location.lng)
        // Setting up the map marker to allow dragging, start in the right position, and show the co-ordinates
        val options = MarkerOptions()
            .title("Project")
            .snippet("GPS : $loc")
            .draggable(true)
            .position(loc)
        projectMapViewModel.map.addMarker(options)
        projectMapViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, location.zoom))
        projectMapViewModel.map.setOnMarkerDragListener(this)
        projectMapViewModel.map.setOnMarkerClickListener(this)
    }



    override fun onMarkerDragStart(marker: Marker) {

    }

    override fun onMarkerDrag(marker: Marker)  {
        fragBinding.lat.setText("Lat: " + "%.6f".format(marker.position.latitude))
        fragBinding.lng.setText("Lng: " + "%.6f".format(marker.position.longitude))

    }

    override fun onMarkerDragEnd(marker: Marker) {
        // Updating co-ordinates after dragging
        location.lat = marker.position.latitude
        location.lng = marker.position.longitude
        location.zoom = projectMapViewModel.map.cameraPosition.zoom
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        // Showing co-ordinates on marker click
        val loc = LatLng(location.lat, location.lng)
        println(loc)
        marker.snippet = "GPS : $loc"
        return false
    }


    fun setButtonListener(layout: FragmentProjectMapBinding) {
        layout.fab.setOnClickListener {
            val resultIntent = Intent()
            // Passing the new location upon back click
            resultIntent.putExtra("location", location)
            if (args.project.projectId.isEmpty()) {
                val action = ProjectMapFragmentDirections.actionProjectMapFragmentToProjectNewFragment(
                    args.portfolioid,
                    location,
                    args.project
                )
                findNavController().navigate(action)
            } else {
                val action = ProjectMapFragmentDirections.actionProjectMapFragmentToProjectDetailFragment(
                    args.project,
                    args.portfolioid,
                    location
                )
                findNavController().navigate(action)

            }

        }
    }


}

