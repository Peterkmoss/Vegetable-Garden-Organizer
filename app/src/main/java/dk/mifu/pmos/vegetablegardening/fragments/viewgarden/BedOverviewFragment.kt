package dk.mifu.pmos.vegetablegardening.fragments.viewgarden

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dk.mifu.pmos.vegetablegardening.R
import dk.mifu.pmos.vegetablegardening.database.AppDatabase
import dk.mifu.pmos.vegetablegardening.database.GardenRepository
import dk.mifu.pmos.vegetablegardening.databinding.FragmentBedOverviewBinding
import dk.mifu.pmos.vegetablegardening.databinding.ListItemTileBinding
import dk.mifu.pmos.vegetablegardening.helpers.GridHelper
import dk.mifu.pmos.vegetablegardening.helpers.callbacks.BedCallback
import dk.mifu.pmos.vegetablegardening.helpers.callbacks.IconCallback
import dk.mifu.pmos.vegetablegardening.helpers.predicates.LocationPredicate
import dk.mifu.pmos.vegetablegardening.helpers.predicates.PlantablePredicate
import dk.mifu.pmos.vegetablegardening.models.Bed
import dk.mifu.pmos.vegetablegardening.models.Coordinate
import dk.mifu.pmos.vegetablegardening.models.MyPlant
import dk.mifu.pmos.vegetablegardening.viewmodels.BedViewModel
import dk.mifu.pmos.vegetablegardening.viewmodels.PlantViewModel
import dk.mifu.pmos.vegetablegardening.views.Tooltip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BedOverviewFragment: Fragment() {
    private lateinit var binding: FragmentBedOverviewBinding
    private var existsPlantablePlants = false
    private val bedViewModel: BedViewModel by activityViewModels()
    private val plantViewModel: PlantViewModel by activityViewModels()
    private var plantableTileSlots: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_bed_overview, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.tooltip -> {
                Tooltip.newTooltip(requireContext(), getString(R.string.tooltip_bed_overview), requireView().rootView.findViewById(R.id.tooltip))
                true
            }
            R.id.delete -> {
                findNavController().navigate(BedOverviewFragmentDirections.deleteBedAction())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentBedOverviewBinding.inflate(inflater, container, false)

        existsPlantablePlants = !plantViewModel.plants.value
                ?.filter(PlantablePredicate())
                ?.filter(LocationPredicate(bedViewModel.bedLocation))
                .isNullOrEmpty()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gridlayout.columnCount = bedViewModel.columns
        binding.gridlayout.rowCount = bedViewModel.rows

        val orderedArrayList = getTilesInOrder()

        insertTilesInView(orderedArrayList)
        addOnMapChangedCallbacks()
        setExplanationTextViews()
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar?.title = bedViewModel.name
    }

    private fun getTilesInOrder(): List<Pair<Coordinate, MyPlant?>> {
        val orderedArrayList: MutableList<Pair<Coordinate, MyPlant?>> = mutableListOf()
        for(i in 0 until bedViewModel.rows){
            for(j in 0 until bedViewModel.columns){
                val coordinate = Coordinate(j,i)
                orderedArrayList.add(Pair(coordinate, bedViewModel.plants?.get(coordinate)))
            }
        }
        return orderedArrayList
    }

    private fun insertTilesInView(list: List<Pair<Coordinate, MyPlant?>>){
        list.forEach {
            val coordinate = it.first
            val plant = it.second
            val tileBinding = ListItemTileBinding.inflate(layoutInflater, binding.gridlayout, true)
            initializeTile(coordinate, plant, tileBinding)
            initializeIcons(coordinate, plant, tileBinding)
        }
    }

    private fun initializeTile(coordinate: Coordinate, plant: MyPlant?, tileBinding: ListItemTileBinding) {
        val tileSideLength = GridHelper.getTileSideLength()

        if(plant != null || existsPlantablePlants) //Only create listeners for tiles with plants or plantables
            tileBinding.plantButton.setOnClickListener { _ -> navigate(coordinate, plant) }

        tileBinding.plantButton.text = plant?.name ?: ""
        tileBinding.plantButton.width = tileSideLength
        tileBinding.plantButton.height = tileSideLength
        tileBinding.plantButton.id = View.generateViewId()

        bedViewModel.tileIds?.put(coordinate, tileBinding.plantButton.id)
    }

    private fun initializeIcons(coordinate: Coordinate, plant: MyPlant?, tileBinding: ListItemTileBinding){
        if(plant == null && existsPlantablePlants) {
            tileBinding.iconView.setImageResource(R.drawable.ic_flower)
            tileBinding.iconView.visibility = View.VISIBLE
            plantableTileSlots = true
        }

        bedViewModel.plantsToWater.observe(viewLifecycleOwner, {
            if(plant != null && it != null && it[coordinate] != null){
                tileBinding.iconView.setImageResource(R.drawable.water)
                tileBinding.iconView.visibility = View.VISIBLE
            }
        })
    }

    private fun addOnMapChangedCallbacks(){
        bedViewModel.plants?.addOnMapChangedCallback(BedCallback(requireView(), bedViewModel))
        bedViewModel.plants?.addOnMapChangedCallback(IconCallback(requireView(), bedViewModel))
    }

    private fun setExplanationTextViews(){
        if(existsPlantablePlants && plantableTileSlots){
            binding.plantableExplanationTextView.visibility = View.VISIBLE
            binding.plantableExplanationTextView.text = getString(R.string.guide_plantable_plants)
            binding.plantableExplanationImageView.setImageResource(R.drawable.ic_flower)
        }

        bedViewModel.plantsToWater.observe(viewLifecycleOwner, {
            if(!it.isNullOrEmpty()){
                binding.waterExplanationTextView.visibility = View.VISIBLE
                binding.waterExplanationTextView.text = getString(R.string.guide_check_water)
                binding.waterExplanationImageView.setImageResource(R.drawable.water)
            }
        })
    }

    private fun navigate(coordinate: Coordinate, plant: MyPlant?) {
        if(plant == null) {
            findNavController().navigate(BedOverviewFragmentDirections.showPlantingOptions(coordinate, PlantablePredicate()))
        } else {
            findNavController().navigate(BedOverviewFragmentDirections.showPlantInfo(coordinate, plant))
        }
    }
}