package com.cyan_namid09.postbox

import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyan_namid09.postbox.Adapter.PostboxListAdapter
import com.cyan_namid09.postbox.Adapter.TodoListAdapter
import com.cyan_namid09.postbox.Functions.*
import com.cyan_namid09.postbox.Helper.DB_DEFAULT_STATE_TABLE
import com.cyan_namid09.postbox.Helper.TodoDataBaseOpenHelper
import com.cyan_namid09.postbox.MyClass.PostboxView
import com.cyan_namid09.postbox.Service.operateFirstJobService
import com.cyan_namid09.postbox.Widget.AddButtonClickListener
import com.cyan_namid09.postbox.Widget.PostboxItemDecoration
import com.cyan_namid09.postbox.Widget.PostboxViewDragListener
import java.lang.Exception
import java.lang.NullPointerException

class MainActivity : AppCompatActivity(), PostboxListAdapter.ItemDragListener, PostboxViewDragListener.AnimateListener {

    private lateinit var mainConstraintLayout: ConstraintLayout
    private lateinit var todoBoardConstraintLayout: ConstraintLayout
    private lateinit var doneConstraintLayout: ConstraintLayout
    private lateinit var postboxRecyclerView: RecyclerView
    private lateinit var todoBoardRecyclerView: RecyclerView
    private lateinit var addButton: ImageButton
    private lateinit var doneButton: ImageButton
    private lateinit var yetButton: ImageButton
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



        val todoDataPref = getSharedPreferences("todo_database", Context.MODE_PRIVATE)
        if (!todoDataPref.getBoolean("default_setup", false)) {
            dbHelper.defaultSetup()

            Toast.makeText(this, "あんた、おかしいよ！", Toast.LENGTH_LONG).show()

            // 通知用初期設定Job開始
            operateFirstJobService(this)

        }

        mainConstraintLayout = findViewById(R.id.MainConstraintLayout)

        todoBoardConstraintLayout = findViewById(R.id.todo_board)

        boardTitleTextView = findViewById(R.id.todo_board_title_textview)

        previewStateId = DB_DEFAULT_STATE_TABLE["TODAY"] ?: throw NullPointerException()

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
        val doneLayoutListener = PostboxViewDragListener(this, openDrawable = R.drawable.ani_done_postbox_responce, closeDrawable = R.drawable.ani_done_postbox_responce_reverse)
        doneLayoutListener.listener = this
        doneConstraintLayout.setOnDragListener(doneLayoutListener)
        val donePostboxView = layoutInflater.inflate(R.layout.layout_postbox, null).apply {
            val textView = findViewById<TextView>(R.id.postbox_title).apply {
                setTextColor(resources.getColor(R.color.colorBlack))
                text = resources.getString(R.string.done)
            }
            val imageVIew = findViewById<ImageView>(R.id.postbox_image).apply {
                setImageResource(R.drawable.ani_done_postbox_responce)
            }
        }
        doneConstraintLayout.addView(donePostboxView)

        addButton = findViewById(R.id.add_todo_button)

        doneButton = findViewById(R.id.done_button)
        doneButton.setOnClickListener {
            this.updatePreview(DB_DEFAULT_STATE_TABLE["DONE"] ?: throw NullPointerException())
        }

        yetButton = findViewById(R.id.yet_button)
        yetButton.setOnClickListener {
            this.updatePreview(DB_DEFAULT_STATE_TABLE["YET"] ?: throw NullPointerException())
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
            setImageResource(R.drawable.ani_mail_push)
            scaleType = ImageView.ScaleType.FIT_CENTER
            top = start.y
            left = start.x
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            z = todoBoardConstraintLayout.z + 1
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (imageView.drawable as AnimatedVectorDrawable).reset()
        }
        imageView.layoutParams = ConstraintLayout.LayoutParams(
            resources.getDimension(R.dimen.post_image_width).toInt(),
            resources.getDimension(R.dimen.post_image_height).toInt())

        mainConstraintLayout.addView(imageView)     // Add view to layout

        val constrainSet = ConstraintSet()
        constrainSet.clone(mainConstraintLayout)
        constrainSet.connect(imageView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, start.x)
        constrainSet.connect(imageView.id, ConstraintSet.TOP,ConstraintSet.PARENT_ID, ConstraintSet.TOP, start.y)
        constrainSet.applyTo(mainConstraintLayout)

        ViewCompat.animate(imageView).apply {
            duration = 500
            x(end.x.toFloat())
            y(end.y.toFloat())
            interpolator = DecelerateInterpolator() // アニメーション速度
            setListener(
                object: ViewPropertyAnimatorListener {
                    override fun onAnimationEnd(view: View?) {
                        (imageView.drawable as Animatable).start()
                        val handler = Handler()
                        Thread {
                            Thread.sleep(1200)
                            handler.post {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    (imageView.drawable as AnimatedVectorDrawable).reset()
                                }
                                mainConstraintLayout.removeView(imageView)
                                hiddenDonePost()
                            }
                        }.start()
                    }

                    override fun onAnimationCancel(view: View?) {

                    }

                    override fun onAnimationStart(view: View?) {

                    }

                })
            start()
        }
    }

    override fun postAnimation(view: View, start: Point, end: Point) {
        callPostAnimation(postboxListAdapter, view, start, end)
    }


    fun updatePreview(stateId: Int) {
        previewStateId = stateId

        addButton.setOnClickListener(AddButtonClickListener(this, previewStateId) {
            (todoBoardRecyclerView.adapter as TodoListAdapter).insert()
        })

        dbHelper.readState(where = "id == ?", whereValue = arrayOf(stateId.toString())) {
            it.moveToFirst()
            val stateName = it.getString(it.getColumnIndex("name"))
            boardTitleTextView.text = stateName
            if (stateId != DB_DEFAULT_STATE_TABLE["YET"] && stateId != DB_DEFAULT_STATE_TABLE["DONE"])
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
