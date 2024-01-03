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

    private fun onNewStart () {
        setContentView(layout.start)
        hideSystemUI()
        data.clear()
        findViewById<Button>(id.startButton).setOnClickListener { setPoseView() }
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
        counterTeam.setText(teamNum.toString())
        
        // list of each incrementing button
        var incrementers = listOf<Button>(
            findViewById(id.increaseCounter),
            findViewById(id.increaseCounter2),
            findViewById(id.counter4up)
        )

        // list of each decrementing button
        var decrementers = listOf<Button>(
            findViewById(id.lowerCounter),
            findViewById(id.lowerCounter2),
            findViewById(id.counter4down)
        )

        // list of each counter textView
        var counters = listOf<TextView> (
            findViewById(id.count),
            findViewById(id.counter2),
            findViewById(id.counter4)
        )
        // list of each counter's value. order is the same as declared in the button lists. All values start at 0
        var counterValue = mutableListOf<Int>(0, 0, 0)


        counters.forEachIndexed { index, counterView -> // counterView is the element at the index of the counters list
            var test = if (index != 3) {counterView.hint.toString() + ": " + counterValue[index].toString()}  else {counterValue[index].toString()}

//            setCounterData(counterView, counterValue[index]) // TODO: this brokey fix :)

            // Updates the number on text of the counter. The name is stored in the hint of the textView, which is not visible
            counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()
            incrementers[index].setOnClickListener{

                // increment the value of this counter when the + is pressed
                counterValue[index]++
//                setCounterData(counterView, counterValue[index])
                // Updates the number on the text of the counter when it is incremented
                counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()
            }

            decrementers[index].setOnClickListener {

                // decrement the value of this counter when the - is pressed. The if statement prevents negative numbers
                if (counterValue[index] > 0) counterValue[index]--
//                setCounterData(counterView, counterValue[index])
                // updates the number on the text of the counter when it is decremented.
                counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()
            }

        }
        findViewById<Button>(id.counterNext).setOnClickListener {setMalfunctionView()}
        
    }

    private fun setMalfunctionView() {
        setContentView(layout.malfunction_type)
        hideSystemUI()

        var team = findViewById<TextView>(id.malfTeam)
        team.text = teamNum.toString()
        team.background = color

        findViewById<Button>(id.malfNext).setOnClickListener {setFinalView()}

        var radioGroup = listOf<View> (
            findViewById(id.malfNothing),
            findViewById(id.malfBroken),
            findViewById(id.malfDisabled),
            findViewById(id.malfNoShow)
        )
        radioGroup.forEach() {
            it.setOnClickListener {
                val x = findViewById<View>(it.id)
                malfunctionData(x)
            }
        }

    }

    private fun setFinalView () {
        setContentView(layout.result)
        hideSystemUI()

        var team = findViewById<TextView>(id.resultTeam)
        team.text = teamNum.toString()
        team.background = color

        findViewById<Button>(id.resultWin).setOnClickListener { finish(it) }
        findViewById<Button>(id.resultTie).setOnClickListener { finish(it) }
        findViewById<Button>(id.resultLose).setOnClickListener { finish(it) }
    }

    private fun finish(view: View) {
//        var result = view.tag.toString() // TODO: this brokey :(
//        when (result) {
//            "win" -> data["result"] = "win"
//            "tie" -> data["result"] = "tie"
//            "lose" -> data["result"] = "lose"
//        }

//        publishData()
//        onNewStart()
    }

    private fun malfunctionData(view: View) { //TODO: Test to see if having a single data call works (data[ind] = view.text or whatever)
        val ind = "malfunction"
        when (view.id) {
            id.malfNothing -> {
                data.putIfAbsent(ind, 0)
                data[ind] = "Nothing Wrong"
            }
            id.malfBroken -> {
                data.putIfAbsent(ind, 1)
                data[ind] = "Broken Mechanism"
            }
            id.malfDisabled -> {
                data.putIfAbsent(ind, 2)
                data[ind] = "Disabled"
            }
            id.malfNoShow -> {
                data.putIfAbsent(ind, 3)
                data[ind] = "No Show"
            }
        }
    }

    private fun setCounterData(view: View, value: Int) {
        data.putIfAbsent(view.tag.toString(), 0)
        data[view.tag.toString()] = value
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