package com.rahul.rickandmorty.ui.characters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rahul.rickandmorty.data.entities.Character
import com.rahul.rickandmorty.databinding.ItemCharacterGridBinding

class CharactersGridAdapter(private val listener: CharacterItemListener) :
    RecyclerView.Adapter<CharacterGridViewHolder>() {

    interface CharacterItemListener {
        fun onClickedCharacter(characterId: Int)
        fun onClickedCharacterIcon(character: Character)
    }

    private val items = ArrayList<Character>()

    fun setItems(items: ArrayList<Character>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterGridViewHolder {
        val binding: ItemCharacterGridBinding =
            ItemCharacterGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterGridViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: CharacterGridViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size
}

class CharacterGridViewHolder(
    private val itemBinding: ItemCharacterGridBinding,
    private val listener: CharactersGridAdapter.CharacterItemListener
) : RecyclerView.ViewHolder(itemBinding.root),
    View.OnClickListener {
    private lateinit var character: Character

    init {
        itemBinding.root.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    fun bind(item: Character) {
        this.character = item
        itemBinding.name.text = item.name
        itemBinding.speciesAndStatus.text = """${item.species} - ${item.status}"""
        Glide.with(itemBinding.root)
            .load(item.image)
            .into(itemBinding.imageGrid)
        itemBinding.imageGrid.setOnClickListener {
            /*Toast.makeText(itemBinding.root.context, item.location.locationName, Toast.LENGTH_LONG)
                .show()*/
            listener.onClickedCharacterIcon(item)
        }
    }

    override fun onClick(v: View?) {
        listener.onClickedCharacter(character.id)
    }
}
