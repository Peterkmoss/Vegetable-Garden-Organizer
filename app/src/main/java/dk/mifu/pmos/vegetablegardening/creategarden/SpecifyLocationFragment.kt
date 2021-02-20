package dk.mifu.pmos.vegetablegardening.creategarden

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import dk.mifu.pmos.vegetablegardening.viewmodels.CurrentGardenViewModel
import dk.mifu.pmos.vegetablegardening.models.Garden
import dk.mifu.pmos.vegetablegardening.enums.Location
import dk.mifu.pmos.vegetablegardening.databinding.FragmentSpecifyLocationBinding

class SpecifyLocationFragment: Fragment() {
    private lateinit var binding: FragmentSpecifyLocationBinding

    private val currentGardenViewModel: CurrentGardenViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpecifyLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.outdoorsButton.setOnClickListener { if (validate()) startCreateGridFragment(Location.Outdoors) }
        binding.greenhouseButton.setOnClickListener{ if (validate()) startCreateGridFragment(Location.Greenhouse) }
    }

    private fun validate() : Boolean {
        val name = binding.gardenNameEditText.text.toString()
        return if (name.isBlank()) {
            Toast.makeText(context, "Please give your garden a name", Toast.LENGTH_SHORT).show()
            binding.gardenNameEditText.requestFocus()
            false
        } else
            true
    }

    private fun startCreateGridFragment(location: Location){
        val name = binding.gardenNameEditText.text.toString()
        currentGardenViewModel.name = name
        currentGardenViewModel.location = location
        requireView().findNavController().navigate(SpecifyLocationFragmentDirections.nextAction())
    }
}
