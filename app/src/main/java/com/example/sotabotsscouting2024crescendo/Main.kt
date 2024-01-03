package com.example.sotabotsscouting2024crescendo


import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.sotabotsscouting2024crescendo.R.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import java.util.Stack

class Main : Activity() {
    /*
    Initial declarations of variables to be used by the app.

     */
    lateinit var data: MutableMap<String, Any> // map of each data piece, and its name (ex: "team", 2557)
    var pose: Int = 0; // position of the team (red or blue)
    var teamNum = 0; // team number of the current app iteration
    lateinit var poseTXT: String
    lateinit var color: Drawable
    lateinit var prevChange: Stack<View>
    var dataBase: FirebaseDatabase = Firebase.database("https://sotabots-crescendo-scouting-default-rtdb.firebaseio.com/") // instance of the firebase database

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        start()
    }
    override fun onCreate(saveInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(saveInstanceState, persistentState)
        start()
    }

    fun start() {
        setContentView(layout.start) // Sets view of app to be start layout
        findViewById<Button>(id.startButton).setOnClickListener() {x -> setPoseView()} // Creates
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) // Locks orientation of app to be portrait (i think)
        data = mutableMapOf()
        prevChange = Stack()
        dataBase.setPersistenceEnabled(true)
        hideSystemUI() // Hides system UI
    }

    // Method that will hide the System UI of the app while running. Back, Home, and App Navigator should be hidden.
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())

            // When the screen is swiped up at the bottom
            // of the application, the navigationBar shall
            // appear for some time
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // Method to set the app layout to the set_pose page
    private fun setPoseView () {
        setContentView(layout.set_pose) // set view to set_pose layout
        hideSystemUI() // hides system UI
        var poseList = listOf<View>( // create a list of buttons for each pose
            findViewById<Button>(id.red1),
            findViewById<Button>(id.red2),
            findViewById<Button>(id.red3),
            findViewById<Button>(id.blue1),
            findViewById<Button>(id.blue2),
            findViewById<Button>(id.blue3))

        poseList.forEach() {z -> z.setOnClickListener() {x -> setPose(x)}}
    }

    private fun initializeView () {
        setContentView(layout.initialize) // set view to initialize layout
        hideSystemUI()
        var initPose = findViewById<TextView>(id.initPose)
        initPose.background = color
        initPose.setText(poseTXT)

        findViewById<Button>(id.initNext).setOnClickListener() {initializeData()}


    }

    private fun initializeData () {
        if (findViewById<TextView>(id.initTeam).text.toString() == "" ||
            findViewById<TextView>(id.initMatch).text.toString() == "") return
        data.clear()

        teamNum = findViewById<EditText>(id.initTeam).text.toString().toInt()
        data["pose"] = poseTXT
        data["team"] = teamNum
        data["match"] = findViewById<EditText>(id.initMatch).text.toString().toInt()

        setCounter()

//        setContentView(layout.counter)
    }
//    private fun incrementData (view: View) {
//        var button: Button = findViewById(view.id)
//        val id = button.hint.toString();
//    }


    // TODO: Change this to real game layout
    private fun setCounter () {
        setContentView(layout.counter)
        hideSystemUI()
        var counterTeam = findViewById<TextView>(id.countTeam)
        counterTeam.background = color
//        counterTeam.setText(teamNum) // TODO: problematic for some reason. fix
//        var counter = 0
        var incrementors = listOf<View>(
            findViewById<Button>(id.increaseCounter)
        )

        var decrementors = listOf<View>(
            findViewById<Button>(id.lowerCounter)
        )

        var counters = listOf<View> (
            findViewById(id.counter)
        )

        var counterValue = mutableListOf<Int>(0) // if i end up using this method, label order of counters

        counters.forEachIndexed { index, counter ->
            val counterView = counter as TextView
            setData(counter) // TODO: setData method. maybe talk with jon about this one
            counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()

            incrementors[index].setOnClickListener{
                counterValue[index]++
                counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()
            }

            decrementors[index].setOnClickListener {
                counterValue[index]--
                counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()
            }

        }

//        incrementors.forEach {
//            val x = findViewById<Button>(it.id)
//            val count = findViewById<TextView>(x.hint.get)
//            x.setOnClickListener() {
////                counter++
////                count.setText("Count: " + counter)
//                incrementData(x)
//
//            }
//
//        }







    }

    private fun setData(view: View) {

    }

    private fun setPose (view: View) {
        var button = findViewById<Button>(view.id) // creates a variable for the pose button (Blue 1, red 3 etc.)
        this.pose = button.text.toString().toCharArray().lastIndex.toInt() // sets pose to the last char of the button text
        this.color = button.background // sets color to be the color of the button that was pressed
        this.poseTXT = button.text.toString() // sets poseTXT to be the entire button text
        initializeView()
    }

    // This method
    fun publishData () {
        var allValues = listOf<String>(
            "team",
            "pose",
            "match",
            "count",
            "malfunction",
            "result"
        )
        for (term in allValues) {
            data.putIfAbsent(term, 0)
        }

        var ref = dataBase.getReference(
            "Teams/" + data["team"].toString()
                    + "Matches/" + data["match"])
        ref.setValue(data)

    }


}