package ru.surf.firebasesample.list

import android.view.ViewGroup
import kotlinx.android.synthetic.main.left_chat_item.view.*
import ru.surf.firebasesample.R
import ru.surf.firebasesample.domain.MessageUI
import ru.surfstudio.android.easyadapter.controller.BindableItemController
import ru.surfstudio.android.easyadapter.holder.BindableViewHolder
import ru.surfstudio.android.imageloader.ImageLoader


class RightMessageController(val profileClickAction: (uid: String) -> Unit) : BindableItemController<MessageUI, RightMessageController.Holder>() {

    override fun getItemId(message: MessageUI) = message.id.hashCode().toLong()

    override fun createViewHolder(parent: ViewGroup?) = Holder(parent)

    inner class Holder(parent: ViewGroup?) : BindableViewHolder<MessageUI>(parent, R.layout.right_chat_item) {

        override fun bind(message: MessageUI) {
            ImageLoader.with(itemView.context)
                    .circle(true)
                    .url(message.user.photoUrl)
                    .into(itemView.member_photo_iv)
            itemView.member_name_tv.text = message.user.name
            itemView.message_tv.text = message.message

            itemView.member_photo_iv.setOnClickListener {
                profileClickAction.invoke(message.user.id)
            }
        }
    }
}