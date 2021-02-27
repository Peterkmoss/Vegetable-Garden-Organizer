package dk.mifu.pmos.vegetablegardening.viewgarden

import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dk.mifu.pmos.vegetablegardening.models.Coordinate
import dk.mifu.pmos.vegetablegardening.models.Plant

abstract class BedOverviewNavigation: Fragment(){
    protected fun navigateToPlantInfoDialog(coordinate: Coordinate, plant: Plant?) {
        if (plant != null) {
            requireView().findNavController().navigate(BedOverviewFragmentDirections.showPlantInfo(coordinate, plant))
        }
    }
}