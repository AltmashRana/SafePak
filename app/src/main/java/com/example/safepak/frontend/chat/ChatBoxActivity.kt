package com.example.safepak.frontend.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safepak.R
import com.example.safepak.frontend.chatAdapters.ChatboxAdapter
import com.example.safepak.databinding.ActivityChatBoxBinding
import com.example.safepak.logic.models.Message

class ChatBoxActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBoxBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBoxBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.sendtextBt.setOnClickListener{
            val temp = binding.chatboxBox.text.toString()
            Toast.makeText(this, "${temp}", Toast.LENGTH_SHORT).show()
        }



        val texts = arrayOf("Hi","Hey!", "How are you?", "I'm fine what about you?",
            "You know about tomorrow's test right? We have a test tomorrow!!!",
            "I think I should go and prepare my test, bye","Unfortunately yeah! it's programming fundamentals, the subject i hate the most because it's my first semester btw best of luck. bye","Ok Bye")
        val times = arrayOf("9:30pm","9:31pm", "9:37pm", "9:42pm", "9:56pm","10:01pm","10:30pm","10:35")
        val ids = arrayOf(1,2, 1, 2, 1, 1, 2,1)

        val messages: ArrayList<Message> = ArrayList()

        for (i in texts.indices)
        {
            messages.add(Message(0,ids[i],ids[i],ids[i],texts[i], times[i],null))
        }
        val recyclerView: RecyclerView = binding.chatboxRecyclerView
        val adapter = ChatboxAdapter(messages,1)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(view.context)


        if (messages.size == 0)
            binding.emptychatboxText.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_dropdown, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.close_item -> {
                Toast.makeText(this, "Added to close", Toast.LENGTH_SHORT).show()
            }
            R.id.block_item -> {
                Toast.makeText(this, "Blocked", Toast.LENGTH_SHORT).show()
            }
            R.id.unfriend_item -> {
                Toast.makeText(this, "Unfriended", Toast.LENGTH_SHORT).show()
            }
            R.id.about_menu -> {
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}