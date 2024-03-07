package com.example.sotabotsscouting2024crescendo


import android.R
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.sotabotsscouting2024crescendo.R.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Stack

class Main : Activity() {

    // Initial declarations of variables to be used by the app.

    lateinit var data: MutableMap<String, Any> // map of each data piece, and its name (ex: "team", 2557)
    var pose: Int = 0 // robot position of the team (1, 3 etc.)
    var teamNum = 0 // team number of the current app iteration
    lateinit var poseTXT: String // full position of current team (red1, blue2 etc.)
    lateinit var color: Drawable // Color value of current team for team display background
    lateinit var prevChange: Stack<View> //  Stack of changes for undo button (may not use)
    var dataBase: FirebaseDatabase = Firebase.database(
        "https://sotabots-crescendo-scouting-default-rtdb.firebaseio.com/") // instance of the firebase database

    // The onCreate methods execute upon opening the app
    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        start() // starts app activity
    }
    override fun onCreate(saveInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(saveInstanceState, persistentState)
        start() // starts app activity
    }

    /**
     * Starts the app activity and creates initial values of variables
     */
    private fun start() {
        setContentView(layout.start) // Sets view of app to be start layout
        findViewById<Button>(id.startButton).setOnClickListener {x -> setPoseView()} // Move to next page on button click
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) // Locks orientation of app to be portrait
        data = mutableMapOf() // Creates a data map for each collectable piece of data
        prevChange = Stack() // Creates a stack of changes used for an undo button (may not use)
        dataBase.setPersistenceEnabled(true)
        hideSystemUI() // Hides system UI
    }

    /**
     * Upon finishing a full match scout, restart app
     */
    private fun onNewStart () {
        setContentView(layout.start) // sets content view to start page
        hideSystemUI() // hides system ui
        data.clear() // clears the data map
        findViewById<Button>(id.startButton).setOnClickListener { setPoseView() } // move to next page
    }

    /**
     * Sets the content view to the position selection page and sets the robot position
     */
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

        // set the pose for the selected position button
        poseList.forEach {z -> z.setOnClickListener {x -> setPose(x)}}
    }

    /**
     * Sets the position of the current team
     * @param view The position button that was pressed to run this code (ex Red 3)
     */
    private fun setPose (view: View) {
        val button = findViewById<Button>(view.id) // creates a variable for the pose button (Blue 1, red 3 etc.)
        this.pose = button.text.toString().toCharArray().lastIndex // sets pose to the last char of the button text
        this.color = button.background // sets color to be the color of the button that was pressed
        this.poseTXT = button.text.toString() // sets poseTXT to be the entire button text
        initializeView()
    }

    /**
     * Sets the content view to the team and match selection page
     */
    private fun initializeView () {
        setContentView(layout.initialize) // set view to initialize layout
        hideSystemUI() // hide system UI
        val initPose = findViewById<TextView>(id.initPose) // create a variable for the position TextView element
        initPose.background = color // sets the color of the position element to correspond with the current robot position
        initPose.text = poseTXT // sets the text of the position element to be the full robot position

        findViewById<Button>(id.initNext).setOnClickListener {initializeData()} // update data map values


    }

    /**
     * Updates the pose, team, and match in the data map
     */

    private fun initializeData () {
        // return to initializeView if the team number or match number are left blank. app will not move to the next page
        if (findViewById<TextView>(id.initTeam).text.toString() == "" ||
            findViewById<TextView>(id.initMatch).text.toString() == "") return
        data.clear()

        teamNum = findViewById<EditText>(id.initTeam).text.toString().toInt() // create variable for
        data["pose"] = poseTXT // set full robot position
        data["team"] = teamNum // set team number
        data["match"] = findViewById<EditText>(id.initMatch).text.toString().toInt() // set match number

        setAuto() // move to the next page


    }

    /**
     * Switches to the autonomous page and keep track of each piece of data in the data map
     * Uses + and - buttons separate from the counters themselves
     */
    private fun setAuto () { // TODO: document again
        setContentView(layout.auto) // sets the content view to the auto page
        hideSystemUI() // hides system ui

        // set the attributes of the team display
        val team = findViewById<TextView>(id.autoTeam)
        team.text = teamNum.toString()
        team.background = color

        // create a list of textview elements for the counter labels. they will display the value of the counter
        val counters = listOf<TextView> (
            findViewById(id.autoSpeakerCount),
            findViewById(id.autoAmpCount)
        )
        // create a list of button elements for the counter incrementers. they will increase the value of their associated counter
        val incrementers = listOf<Button>(
            findViewById(id.autoSpeakerUp),
            findViewById(id.autoAmpUp)
        )
        // create a list of button elements for the counter decrementers. they will decrease the value of their associated counter
        val decrementors = listOf<Button>(
            findViewById(id.autoSpeakerDown),
            findViewById(id.autoAmpDown)
        )
        // create a list of integers for the counter values. they are updated when incremented or decremented and displayed on the labels
        val counterValues = mutableListOf(
            if (data["auto" + counters[0].hint.toString()] == null) 0 // if the value has not yet been initially displayed and/or edited, set them to 0
            else data["auto" + counters[0].hint.toString()].toString().toInt(), // if the value has been displayed and/or edited, set them to their value in the data map
            if (data["auto" + counters[1].hint.toString()] == null) 0
            else data["auto" + counters[1].hint.toString()].toString().toInt()
        )

        counters.forEachIndexed { index, counterLabel -> // counter label is the textview at the index of the counters list

            // update the data map for the current counter
            setCounterData(counterLabel, counterValues[index], "auto")

            // update the counter label for the current counter
            counterLabel.text = createNewLabel(counterLabel, counterValues[index])

            // on click of the incrementer associated with the current counter
            incrementers[index].setOnClickListener {

                // increase value of the current counter
                counterValues[index]++

                // update data in the data map for the current counter
                setCounterData(counterLabel, counterValues[index], "auto")

                // update the label of the current counter
                counterLabel.text = createNewLabel(counterLabel, counterValues[index])


            }

            // on click of the decrementer associated with the current counter
            decrementors[index].setOnClickListener {

                // if the counter's value is greater than 0, decrease it. this will eliminate the possibility of negative numbers
                if (counterValues[index] > 0) counterValues[index]--

                // update the counter's data in the data map
                setCounterData(counterLabel, counterValues[index], "auto")

                // update the counter's label with the new value
                counterLabel.text = createNewLabel(counterLabel, counterValues[index])

            }


        }

        // when the checkbox is pressed
        findViewById<CheckBox>(id.autoLeave).setOnClickListener {

            data.putIfAbsent("autoLeave", 0) // put value to data map if none is present
            data.compute("autoLeave") { k, v -> return@compute if (v == 1) 0 else 1 } // if value is 1, set to 0. if not, set to 1
        }
        findViewById<Button>(id.autoSwitchTele).setOnClickListener{ setTele() } // move to teleop page


    }

    /**
     * Switches to the teleop page and keep track of each piece of data in the data map
     * Uses + and - buttons separate from the counters themselves
     */
    private fun setTele() {
        setContentView(layout.teleop) // switch page to teleop
        hideSystemUI() // hide system ui

        // set attributes of team display
        val teamID = findViewById<TextView>(id.teleTeam)
        teamID.text = teamNum.toString()
        teamID.background = color

        // create list of textview elements for each counter label
        val counters = listOf<TextView> (
            findViewById(id.teleSpeaker),
            findViewById(id.teleAmp),
            findViewById(id.teleTrap)
        )

        // create list of buttons for each incrementer
        val incrementers = listOf<Button> (
            findViewById(id.teleSpeakerUp),
            findViewById(id.teleAmpUp),
            findViewById(id.teleTrapUp)
        )

        // create list of buttons for each decrementer
        val decrementers = listOf<Button> (
            findViewById(id.teleSpeakerDown),
            findViewById(id.teleAmpDown),
            findViewById(id.teleTrapDown)
        )

        // create list of integers for each counter value
        val counterValues = mutableListOf (
            if (data["tele" + counters[0].hint.toString()] == null) 0 // if this counter value does not exist yet in the data map, set to 0
            else data["tele" + counters[0].hint.toString()].toString().toInt(), // if this counter value does exist in the data map, keep it at that value
            if (data["tele" + counters[1].hint.toString()] == null) 0 // for each counter
            else data["tele" + counters[1].hint.toString()].toString().toInt(),
            if (data["tele" + counters[2].hint.toString()] == null) 0
            else data["tele" + counters[2].hint.toString()].toString().toInt()
        )


        counters.forEachIndexed { index, counterLabel -> // counter label is the textView element at index of the counters list

            // updates the data map for the current counter
            setCounterData(counterLabel, counterValues[index], "tele")

            // updates the label for the current counter
            counterLabel.text = createNewLabel(counterLabel, counterValues[index])

            // on click of the incrementer associated with the current counter
            incrementers[index].setOnClickListener {
                counterValues[index]++ // increase value of the current counter
                setCounterData(counterLabel, counterValues[index], "tele") // update the data map
                counterLabel.text = createNewLabel(counterLabel, counterValues[index]) // update the counter label
            }
            // on click of the decrementer associated with the current counter
            decrementers[index].setOnClickListener {
                if (counterValues[index] > 0) counterValues[index]-- // if value is above zero, decrease (gets rid of negative numbers)
                setCounterData(counterLabel, counterValues[index], "tele") // update the data map
                counterLabel.text = createNewLabel(counterLabel, counterValues[index]) // update the counter label
            }

        }

        // when the checkbox is pressed
        findViewById<CheckBox>(id.teleClimb).setOnClickListener {
            data.putIfAbsent("teleClimb", 0) // put value to data map if none is present
            data.compute("teleClimb") { k, v -> return@compute if (v == 1) 0 else 1 } // if value is 1, set to 0. if not, set to 0
        }

        // when the checkbox is pressed
        findViewById<CheckBox>(id.teleCoop).setOnClickListener{
            data.putIfAbsent("teleCoop", 0) // put value to data map if none is present
            data.compute("teleCoop") { k, v -> return@compute if (v == 1) 0 else 1 } // if value is 1, set to 0. if not, set to 0
        }

        findViewById<Button>(id.teleSwitchAuto).setOnClickListener {setAuto()} // switch back to auto page
        findViewById<Button>(id.teleFinished).setOnClickListener { setMalfunctionView() } // move on to malfunction page

    }

    /**
     * Updates the values in the data map for a given counter. Used for the auto and teleop pages
     * @param currentCounter The counter to update as a TextView element
     * @param value The current integer value of the counter
     * @param opMode The current opmode of the app as a string (auto / tele)
     */
    private fun setCounterData(currentCounter: TextView, value: Int, opMode: String) {
        data.putIfAbsent(opMode + currentCounter.hint.toString(), 0) // put value to the data map if none is present
        data[opMode + currentCounter.hint.toString()] = value // Uses the hint of the counter TextView as the key, and its value to update the data map
    }

    /**
     * Creates a new string value for an updated counter value
     * @param currentCounter TextView element of the counter to update
     * @param value new value of the counter
     * @return the updated label of the counter as a string
     */
    private fun createNewLabel(currentCounter: TextView, value: Int): String {
        return currentCounter.hint.toString() + ": " + value
    }

    /**
     * Sets content view to malfunction selection page
     */
    private fun setMalfunctionView() {
        setContentView(layout.malfunction_type) // set content view for the malfunction selection page
        hideSystemUI() // hides the system ui

        // set attributes of team display
        val team = findViewById<TextView>(id.malfTeam)
        team.text = teamNum.toString()
        team.background = color

        // move to next page on button press
        findViewById<Button>(id.malfNext).setOnClickListener {setFinalView()}

        // create list of radio buttons
        val radioGroup = listOf<RadioButton> (
            findViewById(id.malfNothing),
            findViewById(id.malfBroken),
            findViewById(id.malfDisabled),
            findViewById(id.malfNoShow)
        )



        //  set data in map for selected radio button
        radioGroup.forEach {
            it.setOnClickListener {
                val x = findViewById<View>(it.id) as TextView // create textview element variable for the selected radio button
                data.putIfAbsent("malfunction", 0) // put value to data map if none is present
                data["malfunction"] = x.text.toString() // sets malfunction value to the text of the pressed radio button
            }
        }

        // when the checkbox is pressed
        findViewById<CheckBox>(id.malfYellow).setOnClickListener {
            data.putIfAbsent("yellowCard", 0) // put value to data map if none is present
            data.compute("yellowCard") { k, v -> return@compute if (v == 1) 0 else 1 } // if value is 1 make zero, otherwise make one
        }

        // when the checkbox is pressed
        findViewById<CheckBox>(id.malfRed).setOnClickListener {
            data.putIfAbsent("redCard", 0) // put value to data map if none is present
            data.compute("redCard") { k, v -> return@compute if (v == 1) 0 else 1 } // if value is 1 make zero, otherwise make one
        }


    }


    /**
     * Sets the content view to the game result selector and keeps track of necessary data
     */
    private fun setFinalView () {
        setContentView(layout.result) // sets content view to result page
        hideSystemUI() // hide system ui

        // set attributes of the team display
        val team = findViewById<TextView>(id.resultTeam)
        team.text = teamNum.toString()
        team.background = color

        // check for ranking point data, set point data to map
        findViewById<Button>(id.resultWin).setOnClickListener { setResultData(it) }
        findViewById<Button>(id.resultTie).setOnClickListener { setResultData(it) }
        findViewById<Button>(id.resultLose).setOnClickListener { setResultData(it) }

    }

    /**
     * Checks to see if user has entered a ranking point value for this match. Only move on if there is one
     * Sets rank point result data in the map
     * @param view Result element for the specified result (win/lose/tie)
     */
    private fun setResultData (view: View) {
        val rankPoints = findViewById<EditText>(id.resultRankPtsNum)

        // if user has not entered rank point data, go back to view page
        if (rankPoints.text.toString() == "") {
            return
        }
        // put rank point data to map
        data.putIfAbsent("rankingPoints", 0)
        data["rankingPoints"] = rankPoints.text.toString().toInt()

        // finish app
        finish(view)
    }

    /**
     * Updates result data in the map, ends this instance of the app and starts a new one
     * @param view Result element for the specified result (win/lose/tie)
     */
    private fun finish(view: View) {
        view as TextView // make view a TextView element
        when (view.hint.toString()) { // create variable for the selected result
            // add result to the data map
            "win" -> data["result"] = "win"
            "tie" -> data["result"] = "tie"
            "lose" -> data["result"] = "lose"
        }

        publishData() // push data map to firebase
        onNewStart() // start new app instance
    }

    /**
     * Publishes the data map to a Firebase realtime database
     */
    private fun publishData () {

        // create a list of strings as the keys for each piece of data to be collected and uploaded
        val allValues = listOf(
            "team",
            "pose",
            "match",
            "autoSpeaker",
            "autoAmp",
            "autoLeave",
            "teleSpeaker",
            "teleAmp",
            "teleClimb",
            "teleTrap",
            "teleCoop",
            "rankingPoints",
            "yellowCard",
            "redCard",
            "malfunction",
            "result"
        )

        // for every key check if it has value, set to 0 if none
        for (term in allValues) {
            data.putIfAbsent(term, 0)
        }

        // create a reference in the Firebase database
        val ref = dataBase.getReference(
            "Teams/" + data["team"].toString()
                    + "/" + "Matches/" + data["match"])

        // upload the data map to the created database reference
        ref.setValue(data)

    }

    /**
     * Hides the System UI of the app while running. Back, Home, and App Navigator should be hidden
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


}