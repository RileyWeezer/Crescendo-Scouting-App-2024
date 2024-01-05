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
    var pose: Int = 0; // robot position of the team (1, 3 etc.)
    var teamNum = 0; // team number of the current app iteration
    lateinit var poseTXT: String // full position of current team (red1, blue2 etc.)
    lateinit var color: Drawable // Color value of current team for team display background
    lateinit var prevChange: Stack<View> //  Stack of changes for undo button (may not use)
    var dataBase: FirebaseDatabase = Firebase.database("https://sotabots-crescendo-scouting-default-rtdb.firebaseio.com/") // instance of the firebase database

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        start()
    }
    override fun onCreate(saveInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(saveInstanceState, persistentState)
        start()
    }

    private fun start() {
        setContentView(layout.start) // Sets view of app to be start layout
        findViewById<Button>(id.startButton).setOnClickListener() {x -> setPoseView()} // Creates
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) // Locks orientation of app to be portrait (i think)
        data = mutableMapOf() // Creates a data map for each collectable piece of data
        prevChange = Stack() // Creates a stack of changes used for an undo button (may not use)
        dataBase.setPersistenceEnabled(true)
        hideSystemUI() // Hides system UI
    }

    /**
     * Method that will hide the System UI of the app while running. Back, Home, and App Navigator should be hidden.
      */

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())

            // Show app navigator panel when bottom of screen is swiped up
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
        val poseList = listOf<View>( // create a list of buttons for each pose
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
        val initPose = findViewById<TextView>(id.initPose)
        initPose.background = color
        initPose.setText(poseTXT)

        findViewById<Button>(id.initNext).setOnClickListener() {initializeData()}


    }

    private fun initializeData () { // TODO: Document this method
        if (findViewById<TextView>(id.initTeam).text.toString() == "" ||
            findViewById<TextView>(id.initMatch).text.toString() == "") return
        data.clear()

        teamNum = findViewById<EditText>(id.initTeam).text.toString().toInt()
        data["pose"] = poseTXT
        data["team"] = teamNum
        data["match"] = findViewById<EditText>(id.initMatch).text.toString().toInt()

        setCounter()


    }

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
            findViewById(id.counter3up),
            findViewById(id.counter4up)
        )

        // list of each decrementing button
        var decrementers = listOf<Button>(
            findViewById(id.lowerCounter),
            findViewById(id.lowerCounter2),
            findViewById(id.counter3down),
            findViewById(id.counter4down)
        )

        // list of each counter textView
        var counters = listOf<TextView> (
            findViewById(id.count),
            findViewById(id.counter2),
            findViewById(id.counter3Label),
            findViewById(id.counter4)
        )
        // list of each counter's value. order is the same as declared in the button lists. All values start at 0
        var counterValue = mutableListOf<Int>(0, 0, 0, 0)


        counters.forEachIndexed { index, counterView -> // counterView is the element at the index of the counters list

            // update the counter's value in the data map
            setCounterData(counterView, counterValue[index])

            // Updates the number on text of the counter. The name is stored in the hint of the textView, which is not visible
            counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()
            incrementers[index].setOnClickListener{

                // increment the value of this counter when the + is pressed
                counterValue[index]++

                // update the counter's value in the data map
                setCounterData(counterView, counterValue[index])

                // Updates the number on the text of the counter when it is incremented
                counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()
            }

            decrementers[index].setOnClickListener {

                // decrement the value of this counter when the - is pressed. The if statement prevents negative numbers
                if (counterValue[index] > 0) counterValue[index]--

                // update the counter's value in the data map
                setCounterData(counterView, counterValue[index])

                // updates the number on the text of the counter when it is decremented.
                counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()
            }

        }
        findViewById<Button>(id.counterNext).setOnClickListener {setMalfunctionView()} // move to next page
        
    }

    private fun setMalfunctionView() {
        setContentView(layout.malfunction_type)
        hideSystemUI()

        var team = findViewById<TextView>(id.malfTeam)
        team.text = teamNum.toString()
        team.background = color

        // move to next page on button press
        findViewById<Button>(id.malfNext).setOnClickListener {setFinalView()}

        // create list of radio buttons
        var radioGroup = listOf<View> (
            findViewById(id.malfNothing),
            findViewById(id.malfBroken),
            findViewById(id.malfDisabled),
            findViewById(id.malfNoShow)
        )

        //  set data in map for selected radio button
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
        view as TextView // makes view a TextView element
        var result = view.hint.toString() // create variable for the selected result
        when (result) { // add result to the data map
            "win" -> data["result"] = "win"
            "tie" -> data["result"] = "tie"
            "lose" -> data["result"] = "lose"
        }

        publishData() // push data to firebase
        onNewStart() // start new app
    }

    private fun malfunctionData(view: View) { // TODO: Document this method
        view as TextView
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

    /**
     * Updates the values in the data map for each counter
     * @param currentCounter The counter to update as a TextView Element
     * @param value The current value of the counter
     */
    private fun setCounterData(currentCounter: TextView, value: Int) {
        data.putIfAbsent(currentCounter.hint.toString(), 0)
        data[currentCounter.hint.toString()] = value // Uses the hint of the counter TextView as the key, and its value to update the data map
    }

    private fun setPose (view: View) {
        var button = findViewById<Button>(view.id) // creates a variable for the pose button (Blue 1, red 3 etc.)
        this.pose = button.text.toString().toCharArray().lastIndex.toInt() // sets pose to the last char of the button text
        this.color = button.background // sets color to be the color of the button that was pressed
        this.poseTXT = button.text.toString() // sets poseTXT to be the entire button text
        initializeView()
    }

    // This method
    fun publishData () { // TODO: Document this method
        var allValues = listOf<String>(
            "team",
            "pose",
            "match",
            "Count",
            "Silliness",
            "Score",
            "malfunction",
            "result"
        )
        for (term in allValues) {
            data.putIfAbsent(term, 0)
        }

        var ref = dataBase.getReference(
            "Teams/" + data["team"].toString()
                    + "/" + "Matches/" + data["match"])
        ref.setValue(data)

    }


}