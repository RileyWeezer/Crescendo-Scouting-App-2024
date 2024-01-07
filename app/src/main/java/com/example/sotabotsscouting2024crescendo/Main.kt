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

    /**
     * Starts the app activity and creates initial values of variables
     */
    private fun start() {
        setContentView(layout.start) // Sets view of app to be start layout
        findViewById<Button>(id.startButton).setOnClickListener() {x -> setPoseView()} // Move to next page on button click
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
        poseList.forEach() {z -> z.setOnClickListener() {x -> setPose(x)}}
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
        initPose.setText(poseTXT) // sets the text of the position element to be the full robot position

        findViewById<Button>(id.initNext).setOnClickListener() {initializeData()} // update data map values


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

    // TODO: Change this to real game layout
    /**
     * Temporary (now deprecated) page to test different styles of counters
     * Uses + and - buttons to raise and lower the value rather than just tapping the button and undoing
     */
//    private fun setCounter () {
//        setContentView(layout.counter) // sets content view to the counter page
//        hideSystemUI() // hides the system ui
//
//        // set attributes of team display
//        val counterTeam = findViewById<TextView>(id.countTeam)
//        counterTeam.background = color
//        counterTeam.text = teamNum.toString()
//
//        // list of each incrementing button
//        val incrementers = listOf<Button>(
//            findViewById(id.increaseCounter),
//            findViewById(id.increaseCounter2),
//            findViewById(id.counter3up),
//            findViewById(id.counter4up)
//        )
//
//        // list of each decrementing button
//        val decrementers = listOf<Button>(
//            findViewById(id.lowerCounter),
//            findViewById(id.lowerCounter2),
//            findViewById(id.counter3down),
//            findViewById(id.counter4down)
//        )
//
//        // list of each counter textView
//        val counters = listOf<TextView> (
//            findViewById(id.count),
//            findViewById(id.counter2),
//            findViewById(id.counter3Label),
//            findViewById(id.counter4)
//        )
//
//        // list of each counter's value. order is the same as declared in the button lists. All values start at 0
//        val counterValue = mutableListOf<Int>(0, 0, 0, 0)
//
//
//        counters.forEachIndexed { index, counterView -> // counterView is the element at the index of the counters list
//
//            // update the counter's value in the data map
//            setCounterData(counterView, counterValue[index])
//
//            // Updates the number on text of the counter. The name is stored in the hint of the textView, which is not visible
//            counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()
//
//            incrementers[index].setOnClickListener{
//
//                // increment the value of this counter when the + is pressed
//                counterValue[index]++
//
//                // update the counter's value in the data map
//                setCounterData(counterView, counterValue[index])
//
//                // Updates the number on the text of the counter when it is incremented
//                counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()
//            }
//
//            decrementers[index].setOnClickListener {
//
//                // decrement the value of this counter when the - is pressed. The if statement prevents negative numbers
//                if (counterValue[index] > 0) counterValue[index]--
//
//                // update the counter's value in the data map
//                setCounterData(counterView, counterValue[index])
//
//                // updates the number on the text of the counter when it is decremented.
//                counterView.text = counterView.hint.toString() + ": " + counterValue[index].toString()
//            }
//
//        }
//        findViewById<Button>(id.counterNext).setOnClickListener {setMalfunctionView()} // move to next page
//
//    }

    private fun setAuto () { // TODO: document again
        setContentView(layout.auto) // sets the content view to the auto page
        hideSystemUI() // hides system ui

        // set the attributes of the team display
        val team = findViewById<TextView>(id.autoTeam)
        team.text = teamNum.toString()
        team.background = color

        val counters = listOf<TextView> (
            findViewById(id.autoSpeakerCount),
            findViewById(id.autoAmpCount),
            findViewById(id.autoFoulCount)
        )
        val incrementers = listOf<Button>(
            findViewById(id.autoSpeakerUp),
            findViewById(id.autoAmpUp),
            findViewById(id.autoFoulUp)
        )
        val decrementors = listOf<Button>(
            findViewById(id.autoSpeakerDown),
            findViewById(id.autoAmpDown),
            findViewById(id.autoFoulDown)
        )
        val counterValues = mutableListOf<Int>(0,0,0)

        counters.forEachIndexed { index, counterLabel ->

//            setCounterData(counterLabel, counterValues[index], "auto")

            counterLabel.text = createNewLabel(counterLabel, counterValues[index])

            incrementers[index].setOnClickListener {

                counterValues[index]++

                setCounterData(counterLabel, counterValues[index], "auto")

                counterLabel.text = createNewLabel(counterLabel, counterValues[index])


            }

            decrementors[index].setOnClickListener {

                counterValues[index]--

                setCounterData(counterLabel, counterValues[index], "auto")

                counterLabel.text = createNewLabel(counterLabel, counterValues[index])

            }


        }

        findViewById<CheckBox>(id.autoLeave).setOnClickListener {

            data.putIfAbsent("leave", 0)
            data.compute("leave") { k, v -> return@compute if (v == 1) 0 else 1 }
        }
        findViewById<Button>(id.autoSwitchTele).setOnClickListener{ setTele() }


    }

    private fun setTele() {
        setContentView(layout.teleop)
        hideSystemUI()

        val teamID = findViewById<TextView>(id.teleTeam)
        teamID.text = teamNum.toString()
        teamID.background = color

        val counters = listOf<TextView> (

        )

        val incrementers = listOf<Button> (

        )

        val decrementers = listOf<Button> (

        )

        val counterValues = mutableListOf<Int> ()

        counters.forEachIndexed { index, counterLabel ->

            setCounterData(counterLabel, counterValues[index], "tele")

            counterLabel.text = createNewLabel(counterLabel, counterValues[index])

            incrementers[index].setOnClickListener {
                counterValues[index]++
                setCounterData(counterLabel, counterValues[index], "tele")
                counterLabel.text = createNewLabel(counterLabel, counterValues[index])
            }
            decrementers[index].setOnClickListener {
                if (counterValues[index] > 0) counterValues[index]--
                setCounterData(counterLabel, counterValues[index], "tele")
                counterLabel.text = createNewLabel(counterLabel, counterValues[index])
            }

        }

        findViewById<Button>(id.teleSwitchAuto).setOnClickListener {setAuto()}

    }

    /**
     * Updates the values in the data map for a given counter. Used for the auto and teleop pages
     * @param currentCounter The counter to update as a TextView element
     * @param value The current integer value of the counter
     * @param opMode The current opmode of the app as an integer (0 is auto, 1 is teleop)
     */
    private fun setCounterData(currentCounter: TextView, value: Int, opMode: String) {
//        var suffix: String
//        suffix = if (mode == 0) "Auto" else "Tele"
        data.putIfAbsent(opMode + currentCounter.hint.toString(), 0)
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
        radioGroup.forEach() {
            it.setOnClickListener {
                val x = findViewById<View>(it.id)
                malfunctionData(x)
            }
        }

    }

    /**
     * Updates the malfunction value in the data map. Should be executed upon any change to the malfunction radio.
     * @param view The radio button that was most recently pressed
     */
    private fun malfunctionData(view: View) {
        view as TextView // set view to be a TextView element
        val ind = "malfunction" // create variable for the malfunction key in the data map
        when (view.id) { // condition for each malfunction type
            id.malfNothing -> { // when nothing wrong is selected
                data.putIfAbsent(ind, 0)
                data[ind] = "Nothing Wrong"
            }
            id.malfBroken -> { // when broken mechanism is selected
                data.putIfAbsent(ind, 1)
                data[ind] = "Broken Mechanism"
            }
            id.malfDisabled -> { // when disabled is selected
                data.putIfAbsent(ind, 2)
                data[ind] = "Disabled"
            }
            id.malfNoShow -> { // when no show is selected
                data.putIfAbsent(ind, 3)
                data[ind] = "No Show"
            }
        }
    }

    /**
     * Sets the content view to the game result selector
     */
    private fun setFinalView () {
        setContentView(layout.result) // sets content view to result page
        hideSystemUI() // hide system ui

        // set attributes of the team display
        val team = findViewById<TextView>(id.resultTeam)
        team.text = teamNum.toString()
        team.background = color

        // end app with specific result
        findViewById<Button>(id.resultWin).setOnClickListener { finish(it) }
        findViewById<Button>(id.resultTie).setOnClickListener { finish(it) }
        findViewById<Button>(id.resultLose).setOnClickListener { finish(it) }
    }

    /**
     * Updates result data in the map, ends this instance of the app and starts a new one
     * @param view Result element for the specified result (win, lose)
     */
    private fun finish(view: View) {
        view as TextView // make view a TextView element
        val result = view.hint.toString() // create variable for the selected result
        when (result) { // add result to the data map
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
        val allValues = listOf<String>(
            "team",
            "pose",
            "match",
            "autoSpeaker",
            "autoAmp",
            "autoFouls",
            "teleSpeaker",
            "teleAmp",
            "teleFouls",
            "teleClimb",
            "teleSpotlights",
            "leave",
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


}