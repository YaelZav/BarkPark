package com.example.barkpark.ui.userProfile


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barkpark.R
import com.example.barkpark.databinding.ProfileLayoutBinding
import com.example.barkpark.model.ItemManager
import com.example.barkpark.repository.firebasempl.AuthRepositoryFirebase
import com.example.barkpark.repository.firebasempl.FireStoreRepositoryFirebase
import com.example.barkpark.repository.firebasempl.FirestorageRepositoryFirebase
import com.example.barkpark.util.Resource
import com.example.barkpark.util.autoCleared
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

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dogsRecycler.adapter = ProfileAdapter(ItemManager.items, object : ProfileAdapter.DogListener {
            override fun onDogClicked(index: Int) {

            }
            override fun onDogLongClicked(index: Int) {
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
        })

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
                        delay(2000)
                        if(viewModel.curUserMap.value!!.data!! != null) {
                            binding.imgProfile.setImageBitmap(byteArrayToBitmap(viewModel.curUserMap.value!!.data!!))
                        }
                        if(ItemManager.items.isEmpty())
                        for(dog in viewModel.dogStatus.value?.data!!){
                            ItemManager.add(dog)


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