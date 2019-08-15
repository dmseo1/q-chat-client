package com.dongmin.qchat.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dongmin.qchat.R
import com.dongmin.qchat.elements.ChattingRankingElement

class ChattingRankingAdapter(context : Context) : RecyclerView.Adapter<ChattingRankingAdapter.ViewHolder>() {

    var rankingList = ArrayList<ChattingRankingElement>()
    private var context : Context? = context

    //하나의 뷰에는 어떤 정보들이 들어갈까요?
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // each data item is just a string in this case
        var ranking : TextView = itemView.findViewById(R.id.ranking) as TextView
        var rankingImage : ImageView = itemView.findViewById(R.id.ranking_image) as ImageView
        var nickname : TextView = itemView.findViewById<View>(R.id.nickname) as TextView
        var record : TextView = itemView.findViewById<View>(R.id.record) as TextView
        var points : TextView = itemView.findViewById<View>(R.id.points) as TextView
    }

    override fun getItemViewType(position: Int): Int {
        return when(rankingList[position].ranking) {
            "1" -> 1
            "2" -> 2
            "3" -> 3
            else -> 4
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChattingRankingAdapter.ViewHolder {
        // create a new view
        val v : View = when(viewType) {
            1 -> LayoutInflater.from(parent.context)
                .inflate(R.layout.element_chatting_ranking, parent, false)
            2 ->  LayoutInflater.from(parent.context)
                .inflate(R.layout.element_chatting_ranking, parent, false)
            3 -> LayoutInflater.from(parent.context)
                .inflate(R.layout.element_chatting_ranking, parent, false)
            else -> LayoutInflater.from(parent.context)
                .inflate(R.layout.element_chatting_ranking, parent, false)
        }

        return ChattingRankingAdapter.ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ChattingRankingAdapter.ViewHolder, position: Int) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element
        try {
            when(rankingList[position].ranking) {
                "1" -> {    //1등
                    holder.ranking.visibility = View.GONE
                    holder.rankingImage.visibility = View.VISIBLE
                    holder.rankingImage.setImageResource(R.drawable.ico_ranking_1)
                }
                "2" -> {    //2등
                    holder.ranking.visibility = View.GONE
                    holder.rankingImage.visibility = View.VISIBLE
                    holder.rankingImage.setImageResource(R.drawable.ico_ranking_2)
                }
                "3" -> {    //3등
                    holder.ranking.visibility = View.GONE
                    holder.rankingImage.visibility = View.VISIBLE
                    holder.rankingImage.setImageResource(R.drawable.ico_ranking_3)
                }
                else -> {    //기타
                    holder.ranking.visibility = View.VISIBLE
                    holder.rankingImage.visibility = View.GONE
                    holder.ranking.text = rankingList[position].ranking
                }
            }

            holder.nickname.text = rankingList[position].nickname
            holder.record.text = context!!.resources.getString(R.string.element_chatting_ranking_record, rankingList[position].record)
            holder.points.text = context!!.resources.getString(R.string.element_chatting_ranking_points, rankingList[position].points)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return rankingList.size
    }

    fun addItem(item : ChattingRankingElement) {
        rankingList.add(item)
    }
}
