package ru.surf.firebasesample

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import ru.surf.firebasesample.domain.Message
import ru.surf.firebasesample.domain.MessageUI
import ru.surf.firebasesample.domain.User
import ru.surf.firebasesample.list.LeftMessageController
import ru.surf.firebasesample.list.RightMessageController
import ru.surfstudio.android.easyadapter.EasyAdapter
import ru.surfstudio.android.easyadapter.ItemList


class ChatActivity : BaseActivity() {
    companion object {
        private const val TAG = "ChatActivity"
        const val EXTRA_UID = "USER_UID"
        const val MESSAGES_NODE = "messages"
    }

    private lateinit var dbReference: DatabaseReference
    private lateinit var newMessageListener: ChildEventListener

    private val adapter = EasyAdapter()

    private val messagesList = ItemList()
    private var isInitialLoadCompleted = false

    private val rightMessageController = RightMessageController {
        openProfileActivity(it)
    }

    private val leftMessageController = LeftMessageController {
        openProfileActivity(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initViews()
        initFirebaseInstances()
        loadMessages()
        initNewMessageListener()
    }

    override fun onDestroy() {
        dbReference.removeEventListener(newMessageListener)
        super.onDestroy()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        messages_rv.layoutManager = LinearLayoutManager(this)
        messages_rv.adapter = adapter

        send_btn.setOnClickListener {
            if (!TextUtils.isEmpty(message_et.text)) {
                sendMessageToFirebase(message_et.text.toString())
                message_et.setText("")
            }
        }
    }

    private fun initFirebaseInstances() {
        dbReference = FirebaseDatabase.getInstance().reference
    }

    private fun loadMessages() {
        progress.visibility = View.VISIBLE

        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = getUserById(dataSnapshot)
                dataSnapshot.child(MESSAGES_NODE).children.forEach {
                    val message = it.getValue<Message>(Message::class.java)!!
                    addMessage(MessageUI(it.key!!, users[message.uid]!!, message.message, message.timestamp))
                }
                adapter.setItems(messagesList)
                scrollToLastPosition()

                isInitialLoadCompleted = true

                progress.visibility = GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progress.visibility = GONE
                Toast.makeText(this@ChatActivity, "Load failed!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initNewMessageListener() {
        newMessageListener = object : ChildEventListener {

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                if (isInitialLoadCompleted) {
                    val message = dataSnapshot.getValue<Message>(Message::class.java) ?: Message()
                    addNewMessageToList(message, dataSnapshot.key!!)
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        }

        dbReference.child(MESSAGES_NODE).addChildEventListener(newMessageListener)
    }

    private fun sendMessageToFirebase(messageText: String) {
        dbReference.child(MESSAGES_NODE).push().setValue(createMessage(messageText)).addOnCompleteListener{ task ->
            if (!task.isSuccessful) {
                Toast.makeText(this@ChatActivity, "Message sent failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createMessage(messageText: String): Message {
        return Message(getUid(), messageText, System.currentTimeMillis())
    }

    private fun getUid(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private fun scrollToLastPosition() {
        messages_rv.layoutManager.scrollToPosition(messagesList.size)
    }

    private fun addNewMessageToList(message: Message, messageId: String) {
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                addMessage(MessageUI(messageId, getUserById(dataSnapshot)[message.uid] ?: User(), message.message, message.timestamp))
                adapter.setItems(messagesList)
                scrollToLastPosition()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Load failed!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserById(dataSnapshot: DataSnapshot): Map<String, User> {
        val users = mutableMapOf<String, User>()
        dataSnapshot.child("users").children.forEach {
            users[it.key!!] = it.getValue(User::class.java)!!
        }
        return users
    }

    private fun addMessage(message: MessageUI) {
        if (message.user.id == getUid()) {
            messagesList.add(message, rightMessageController)
        } else {
            messagesList.add(message, leftMessageController)
        }
    }

    private fun openProfileActivity(uid: String) {
        startActivity(Intent(this, ProfileActivity::class.java).apply { putExtra(EXTRA_UID, uid) })
    }
}
