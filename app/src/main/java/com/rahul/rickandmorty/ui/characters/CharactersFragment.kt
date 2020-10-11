package com.rahul.rickandmorty.ui.characters

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.rahul.rickandmorty.R
import com.rahul.rickandmorty.app.Constants
import com.rahul.rickandmorty.app.MainApplication
import com.rahul.rickandmorty.data.entities.Character
import com.rahul.rickandmorty.databinding.CharactersFragmentBinding
import com.rahul.rickandmorty.utils.AppPreferences
import com.rahul.rickandmorty.utils.GridSpacingItemDecoration
import com.rahul.rickandmorty.utils.Resource
import com.rahul.rickandmorty.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CharactersFragment : Fragment(), CharactersAdapter.CharacterItemListener,
    CharactersGridAdapter.CharacterItemListener {

    private var binding: CharactersFragmentBinding by autoCleared()
    private val viewModel: CharactersViewModel by viewModels()
    private lateinit var charactersAdapter: CharactersAdapter
    private lateinit var adapterGrid: CharactersGridAdapter

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var gridLayoutManager: GridLayoutManager

    // initialise loading state
    private var mIsLoading: Boolean = false
    private var mIsLastPage: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CharactersFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentOrientation = resources.configuration.orientation
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setupRecyclerViewGrid()
        } else {
            setupRecyclerView()
        }
        setupRvScrollListeners(currentOrientation)
        var pageNum =
            AppPreferences.get(MainApplication.getContext(), Constants.CURRENT_PAGE, 0) as Int
        if (pageNum == null)
            pageNum = 1
        viewModel.start(pageNum)
        setupObservers(currentOrientation)
    }

    private fun setupRecyclerView() {
        charactersAdapter = CharactersAdapter(this)
        layoutManager = LinearLayoutManager(requireContext())
        binding.charactersRv.layoutManager = layoutManager
        binding.charactersRv.adapter = charactersAdapter
    }

    private fun setupRecyclerViewGrid() {
        adapterGrid = CharactersGridAdapter(this)
        gridLayoutManager =
            GridLayoutManager(requireContext(), 4, GridLayoutManager.VERTICAL, false)
        binding.charactersRv.layoutManager = gridLayoutManager

        val spanCount = 4
        val spacing = 50 // 50px
        val includeEdge = false
        binding.charactersRv.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount,
                spacing,
                includeEdge
            )
        )

        binding.charactersRv.adapter = adapterGrid
    }

    private fun setupRvScrollListeners(currentOrientation: Int) {
        // initialise loading state
        mIsLoading = false
        mIsLastPage = false

        // amount of items you want to load per page
        val pageSize = 20
        val scrollListener = object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                var visibleItemCount: Int = 0
                var totalItemCount: Int = 0
                var firstVisibleItemPosition: Int = 0

                if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    // number of visible items
                    visibleItemCount = gridLayoutManager.childCount
                    // number of items in layout
                    totalItemCount = gridLayoutManager.itemCount
                    // the position of first visible item
                    firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()

                } else {
                    // number of visible items
                    visibleItemCount = layoutManager.childCount
                    // number of items in layout
                    totalItemCount = layoutManager.itemCount
                    // the position of first visible item
                    firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                }

                val isNotLoadingAndNotLastPage = !mIsLoading && !mIsLastPage

                // flag if number of visible items is at the last
                val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount

                // validate non negative values
                val isValidFirstItem = firstVisibleItemPosition >= 0

                // validate total items are more than possible visible items
                val totalIsMoreThanVisible = totalItemCount >= pageSize

                // flag to know whether to load more
                val shouldLoadMore =
                    isValidFirstItem && isAtLastItem && totalIsMoreThanVisible && isNotLoadingAndNotLastPage

                if (shouldLoadMore) loadNextPage()
            }
        }
        binding.charactersRv.addOnScrollListener(scrollListener)

    }

    private fun loadNextPage() {
        var pageNum =
            AppPreferences.get(MainApplication.getContext(), Constants.CURRENT_PAGE, 0) as Int

        pageNum += 1

        viewModel.start(pageNum)

    }

    private fun setupObservers(currentOrientation: Int) {
        viewModel.characters.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    var pageNum =
                        AppPreferences.get(
                            MainApplication.getContext(),
                            Constants.CURRENT_PAGE,
                            0
                        ) as Int
                    if (!it.data.isNullOrEmpty())
                        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                            adapterGrid.setItems(ArrayList(it.data))
                            Handler().postDelayed({
                                if (pageNum > 1) {
                                    val pos = gridLayoutManager.findLastVisibleItemPosition() + 2
                                    if (pos < gridLayoutManager.itemCount)
                                        binding.charactersRv.smoothScrollToPosition(pos)
                                }
                            }, 1000)
                        } else {
                            charactersAdapter.setItems(ArrayList(it.data))
                            Handler().postDelayed({
                                if (pageNum > 1) {
                                    val pos = layoutManager.findLastVisibleItemPosition() + 2
                                    if (pos < layoutManager.itemCount)
                                        binding.charactersRv.smoothScrollToPosition(pos)
                                }
                            }, 1000)
                        }
                }
                Resource.Status.ERROR ->
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

                Resource.Status.LOADING ->
                    binding.progressBar.visibility = View.VISIBLE
            }
        })
    }

    override fun onClickedCharacter(characterId: Int) {
        findNavController().navigate(
            R.id.action_charactersFragment_to_characterDetailFragment,
            bundleOf("id" to characterId)
        )
    }

    override fun onClickedCharacterIcon(character: Character) {
        showLocationAlert(character)
    }

    private fun showLocationAlert(character: Character) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(character.name)
        builder.setMessage("Located at ${character.location.locationName}")
        //builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Okay") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        /*builder.setNeutralButton("Cancel") { dialogInterface, which ->
            Toast.makeText(
                requireActivity(),
                "clicked cancel\n operation cancel",
                Toast.LENGTH_LONG
            ).show()
        }*/
        /*builder.setNegativeButton("No") { dialogInterface, which ->
            Toast.makeText(requireActivity(), "clicked No", Toast.LENGTH_LONG).show()
        }*/
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
