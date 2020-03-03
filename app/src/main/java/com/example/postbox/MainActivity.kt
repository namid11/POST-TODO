package com.example.postbox

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.postbox.Adapter.PostboxListAdapter
import com.example.postbox.Adapter.TodoListAdapter
import com.example.postbox.Functions.*
import com.example.postbox.Helper.DB_DEFAULT_STATE_TABLE
import com.example.postbox.Helper.TodoDataBaseOpenHelper
import com.example.postbox.MyClass.PostboxView
import com.example.postbox.Service.operateFirstJobService
import com.example.postbox.Widget.AddButtonClickListener
import com.example.postbox.Widget.PostboxItemDecoration
import com.example.postbox.Widget.PostboxViewDragListener
import java.lang.Exception
import java.lang.NullPointerException
import kotlin.math.log

class MainActivity : AppCompatActivity(), PostboxListAdapter.ItemDragListener, PostboxViewDragListener.AnimateListener {

    private lateinit var mainConstraintLayout: ConstraintLayout
    private lateinit var todoBoardConstraintLayout: ConstraintLayout
    private lateinit var doneConstraintLayout: ConstraintLayout
    private lateinit var postboxRecyclerView: RecyclerView
    private lateinit var todoBoardRecyclerView: RecyclerView
    private lateinit var addButton: ImageButton
    private lateinit var doneButton: ImageButton
    private lateinit var boardTitleTextView: TextView

    private val dbHelper: TodoDataBaseOpenHelper = TodoDataBaseOpenHelper(this)

    private lateinit var postboxListAdapter: PostboxListAdapter
    private lateinit var todoListAdapter: TodoListAdapter

    private var previewStateId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Windowシステム設定
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // channel作成
        makeNotificationChannel(this)
        // 通知用初期設定Job開始
        operateFirstJobService(this)

        val todoDataPref = getSharedPreferences("todo_database", Context.MODE_PRIVATE)
        if (!todoDataPref.getBoolean("default_setup", false)) {
            dbHelper.defaultSetup()
        }

        mainConstraintLayout = findViewById(R.id.MainConstraintLayout)

        todoBoardConstraintLayout = findViewById(R.id.todo_board)

        boardTitleTextView = findViewById(R.id.todo_board_title_textview)

        previewStateId = DB_DEFAULT_STATE_TABLE["TODO"] ?: throw NullPointerException()

        // POSTBox Adapter
        postboxRecyclerView = findViewById(R.id.postbox_recyclerView)
        postboxRecyclerView.addItemDecoration(PostboxItemDecoration(
            convertDp2Px(
                0f,
                this
            ).toInt()))
        postboxListAdapter = PostboxListAdapter(this, dbHelper, targetState = previewStateId)
        postboxListAdapter.listener = this
        postboxRecyclerView.adapter = postboxListAdapter
        postboxRecyclerView.layoutManager = GridLayoutManager(this, 1).apply {
            orientation = GridLayoutManager.HORIZONTAL
        }

        todoBoardRecyclerView = findViewById(R.id.todo_board_recyclerView)
        todoListAdapter = TodoListAdapter(this, dbHelper, targetState = previewStateId)
        todoBoardRecyclerView.adapter = todoListAdapter
        todoBoardRecyclerView.layoutManager = GridLayoutManager(this, 2).apply {

        }

        doneConstraintLayout = findViewById(R.id.done_post_box_constraint_layout)
        doneConstraintLayout.visibility = View.INVISIBLE
        doneConstraintLayout.tag = PostboxView.PostboxData(DB_DEFAULT_STATE_TABLE["DONE"] ?: throw NullPointerException(), "DONE")
        val doneLayoutListener = PostboxViewDragListener(this)
        doneLayoutListener.listener = this
        doneConstraintLayout.setOnDragListener(doneLayoutListener)
        val donePostboxView = layoutInflater.inflate(R.layout.layout_postbox, null).apply {
            val textView = findViewById<TextView>(R.id.postbox_title).apply {
                setTextColor(resources.getColor(R.color.colorBlack))
                text = resources.getString(R.string.done)
            }
        }
        doneConstraintLayout.addView(donePostboxView)

        addButton = findViewById(R.id.add_todo_button)
        addButton.setOnClickListener(AddButtonClickListener(this) {
            (todoBoardRecyclerView.adapter as TodoListAdapter).insert()
        })

        doneButton = findViewById(R.id.done_button)
        doneButton.setOnClickListener {
            this.updatePreview(DB_DEFAULT_STATE_TABLE["DONE"] ?: throw NullPointerException())
        }


        updatePreview(previewStateId)

    }


    override fun onResume() {
        super.onResume()

    }

    override fun callPostAnimation(
        adapter: RecyclerView.Adapter<PostboxListAdapter.ViewHolder>,
        view: View,
        start: Point,
        end: Point
    ) {

        val imageView = ImageView(this).apply {
            id = View.generateViewId()
            setImageResource(R.drawable.ani_card_push)
            scaleType = ImageView.ScaleType.FIT_CENTER
            top = start.y
            left = start.x
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            z = todoBoardConstraintLayout.z + 1
        }
        Log.d("[DEBUG]", "width: %f , height: %f".format(resources.getDimension(R.dimen.post_image_width), resources.getDimension(R.dimen.post_image_height)))
        imageView.layoutParams = ConstraintLayout.LayoutParams(
            resources.getDimension(R.dimen.post_image_width).toInt(),
            resources.getDimension(R.dimen.post_image_height).toInt())

        mainConstraintLayout.addView(imageView)     // Add view to layout

        val constrainSet2 = ConstraintSet()
        constrainSet2.clone(mainConstraintLayout)
        constrainSet2.connect(imageView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, start.x)
        constrainSet2.connect(imageView.id, ConstraintSet.TOP,ConstraintSet.PARENT_ID, ConstraintSet.TOP, start.y)
        constrainSet2.applyTo(mainConstraintLayout)

        ViewCompat.animate(imageView).apply {
            duration = 500
            x(end.x.toFloat())
            y(end.y.toFloat())
            interpolator = DecelerateInterpolator() // アニメーション速度
            setListener(
                object: ViewPropertyAnimatorListener {
                    override fun onAnimationEnd(view: View?) {
                        val drawable = imageView.drawable as? AnimatedVectorDrawable
                        if (drawable != null) {
                            try {
                                val handler = Handler()
                                Thread {
                                    drawable.start()
                                    Thread.sleep(1200)
                                    handler.post {
                                        mainConstraintLayout.removeView(imageView)
                                        Thread.sleep(300)
                                        hiddenDonePost()
                                    }
                                }.start()
                            } catch (e: Exception) {
                                hiddenDonePost()
                            }

                        } else {
                            (imageView.drawable as Animatable).start()
                        }
                    }

                    override fun onAnimationCancel(view: View?) {

                    }

                    override fun onAnimationStart(view: View?) {

                    }

                }
            )
            start()
        }
    }

    override fun postAnimation(view: View, start: Point, end: Point) {
        callPostAnimation(postboxListAdapter, view, start, end)
    }


    fun updatePreview(stateId: Int) {
        previewStateId = stateId

        dbHelper.readState(where = "id == ?", whereValue = arrayOf(stateId.toString())) {
            it.moveToFirst()
            val stateName = it.getString(it.getColumnIndex("name"))
            boardTitleTextView.text = stateName
            if (stateName == DB_DEFAULT_STATE_TABLE.keys.elementAt(1) || stateName == DB_DEFAULT_STATE_TABLE.keys.elementAt(3))
                addButton.visibility = View.VISIBLE
            else
                addButton.visibility = View.INVISIBLE
        }

        // update postbox adapter
        postboxListAdapter.changeState(stateId)
        // update todo list adapter
        todoListAdapter.changeState(stateId)
    }

    fun showDonePost() {
        if (previewStateId != DB_DEFAULT_STATE_TABLE["DONE"]) {
            try {
                doneConstraintLayout.visibility = View.VISIBLE
                doneConstraintLayout.alpha = 0.0f
                Log.d("[DEBUG]", "tmp")
                ViewCompat.animate(doneConstraintLayout).apply {
                    duration = 300
                    interpolator = FastOutLinearInInterpolator()
                    alpha(1.0f)
                    setListener(
                        object : ViewPropertyAnimatorListener {
                            override fun onAnimationEnd(view: View?) {
                                doneConstraintLayout.visibility = View.VISIBLE
                            }

                            override fun onAnimationCancel(view: View?) {
                                doneConstraintLayout.visibility = View.VISIBLE
                            }

                            override fun onAnimationStart(view: View?) {

                            }

                        }
                    )
                    start()
                }
            } catch (e: Exception) {
                doneConstraintLayout.visibility = View.VISIBLE
            }
        }
    }

    fun hiddenDonePost() {
        try {
            doneConstraintLayout.alpha = 1.0f
            ViewCompat.animate(doneConstraintLayout).apply {
                duration = 300
                interpolator = FastOutLinearInInterpolator()
                Log.d("[DEBUG]", "tmp___")
                alpha(0.0f)
                setListener(
                    object : ViewPropertyAnimatorListener {
                        override fun onAnimationEnd(view: View?) {
                            doneConstraintLayout.visibility = View.INVISIBLE
                        }

                        override fun onAnimationCancel(view: View?) {
                            doneConstraintLayout.visibility = View.INVISIBLE
                        }

                        override fun onAnimationStart(view: View?) {

                        }

                    }
                )
                start()
            }
        } catch (e: Exception) {
            doneConstraintLayout.visibility = View.INVISIBLE
        }


    }

}
