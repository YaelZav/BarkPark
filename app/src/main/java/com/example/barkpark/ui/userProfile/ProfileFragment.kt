package com.example.barkpark.ui.userProfile


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barkpark.R
import com.example.barkpark.databinding.ProfileLayoutBinding
import com.example.barkpark.model.DogItem
import com.example.barkpark.model.ItemManager
import com.example.barkpark.repository.firebasempl.AuthRepositoryFirebase
import com.example.barkpark.repository.firebasempl.FireStoreRepositoryFirebase
import com.example.barkpark.repository.firebasempl.FirestorageRepositoryFirebase
import com.example.barkpark.util.Resource
import com.example.barkpark.util.autoCleared
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var binding : ProfileLayoutBinding by autoCleared()

    private val viewModel : ProfileViewModel by viewModels {
        ProfileViewModel.ProfileViewModelFactory(AuthRepositoryFirebase(), FireStoreRepositoryFirebase(),FirestorageRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = ProfileLayoutBinding.inflate(inflater, container, false)

        binding.profileEditBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding.addDogBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_addDogItemFragment)
        }

        binding.groupByGenderCheckbox.setOnClickListener {
            updateDogRecyclerView()
        }

        return binding.root
    }

    fun updateDogRecyclerView() {
        val groupByGender = binding.groupByGenderCheckbox.isChecked

        val task = if (groupByGender)
            FirebaseFirestore.getInstance().collection("dogs").
            orderBy("gender").get()
        else FirebaseFirestore.getInstance().collection("dogs").get()

        task.addOnCompleteListener() {
                if(it.isSuccessful) {
                    ItemManager.items.clear()
                    val breedMap = mutableMapOf<String, Int>()


                    for(query in it.result) {
                        val dogItem =  query.toObject(DogItem::class.java)
                        ItemManager.items.add(dogItem)

                        if(!breedMap.containsKey(dogItem.breed))
                            breedMap[dogItem.breed] = 0
                        breedMap[dogItem.breed] = breedMap[dogItem.breed]!! + 1
                    }

                    val breedCountList = breedMap.map()
                        { "${it.key}: ${it.value} dogs" }.toList()
                    val breedCountString = breedCountList.joinToString("\n")

                    binding.dogBreedReductionTextView.text = breedCountString

                    binding.dogsRecycler.adapter = ProfileAdapter(ItemManager.items,
                        object : ProfileAdapter.DogListener {
                        fun deleteDog(index: Int) {
                            if(ItemManager.items[index].photo != ""){
                                viewModel.deleteDogpic(ItemManager.items[index].Id)
                            }
                            else{
                                viewModel.deleteDog(ItemManager.items[index].Id)
                            }
                            ItemManager.remove(index)
                            binding.dogsRecycler.adapter!!.notifyItemRemoved(index)
                            viewModel.getDogs(viewModel.currentUser.value!!.data?.id!!)
                        }
                        override fun onDogClicked(index: Int) {
                            val dog = ItemManager.items[index]
                            val bundle = bundleOf("edit" to true, "name" to dog.name, "breed" to dog.breed, "age" to dog.age, "gender" to dog.gender)
                            deleteDog(index)
                            Thread.sleep(2000)
                            findNavController().navigate(R.id.action_profileFragment_to_addDogItemFragment, bundle)
                        }
                        override fun onDogLongClicked(index: Int) {
                            deleteDog(index)
                        }
                    })
                }
            }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateDogRecyclerView()

        binding.dogsRecycler.layoutManager = LinearLayoutManager(requireContext())

        ItemTouchHelper(object : ItemTouchHelper.Callback() {

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) = makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.UP or ItemTouchHelper.DOWN)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                ItemManager.remove(viewHolder.adapterPosition)
                binding.dogsRecycler.adapter!!.notifyDataSetChanged()
            }
        }).attachToRecyclerView(binding.dogsRecycler)

        viewModel.currentUser.observe(viewLifecycleOwner) {

            when(it){
                is Resource.Loading -> {

                }
                is Resource.Success -> {
                    val newName = viewModel.currentUser.value?.data?.name
                    binding.usernameProfile.text = newName
                    lifecycleScope.launch {
                        if (viewModel.curUserMap.value != null && viewModel.curUserMap.value?.data != null) {
                            binding.imgProfile.setImageBitmap(byteArrayToBitmap(viewModel.curUserMap.value?.data!!))
                            if (ItemManager.items.isEmpty())
                                for (dog in viewModel.dogStatus.value?.data!!) {
                                    ItemManager.add(dog)
                                }
                        }
                    }

                }
                is Resource.Error -> {

                }
            }
        }

    }

    fun byteArrayToBitmap(data: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_sign_out){
            viewModel.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        return super.onOptionsItemSelected(item)
    }
}