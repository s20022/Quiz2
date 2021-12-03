package jp.ac.it_college.std.s20022.quiz_app2

import android.animation.ObjectAnimator
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.annotation.UiThread
import androidx.core.os.postDelayed
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import jp.ac.it_college.std.s20022.quiz_app2.databinding.FragmentQuizMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL


class QuizMainFragment : Fragment() {
    companion object {
        const val MAX_COUNT = 10
        const val TIME_LIMIT = 10000L
        const val TIMER_INTERVAL = 100L
        const val CHOICE_DELAY_TIME = 2000L
        const val TIME_UP_DELAY_TIME = 1500L
        private const val QUIZ_URL = "https://script.google.com/macros/s/AKfycbznWpk2m8q6lbLWSS6qaz3uS6j3L4zPwv7CqDEiC433YOgAdaFekGJmjoAO60quMg6l/exec?f="
    }


    private var _binding: FragmentQuizMainBinding? = null
    private val binding get() = _binding!!
    private var current = -1
    private var timeLeftCountdown = TimeLeftCountdown()
    private var startTime = 0L
    private var totalElapsedTime = 0L
    private var correctCount = 0
    private val currentElapsedTime get() = SystemClock.elapsedRealtime() - startTime
    private var quizList: MutableList<MutableMap<String,String>> = mutableListOf()


    override fun onResume() {
        super.onResume()
        getVersion(QUIZ_URL +"version")
    }


    @UiThread
    private fun getVersion(url:String) {
        lifecycleScope.launch {
            val result = getJson(url)
        getVersionPost(return@launch)
        }
    }


    @UiThread
    private fun getData(url: String) {
        lifecycleScope.launch {
            val result = getJson(url)
            getVersionPost(return@launch)
        }
    }


    private suspend fun getJson(url: String): String {
        val res = withContext(Dispatchers.IO) {
            var result = ""
            val url = URL(url)
            val con = url.openConnection() as? HttpURLConnection
            con?.let {
                try {
                    it.connectTimeout = 10000
                    it.readTimeout = 10000
                    it.requestMethod = "GET"
                    it.connect()

                    val stream = it.inputStream
                    result = extendString(stream)
                    stream.close()
                } catch (ex: SocketTimeoutException) {
                    println("通信タイムアウト")
                }
                it.disconnect()
            }
            result
        }
        return res
    }


    private fun extendString(stream: InputStream?) : String {
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        return reader.readText()
    }


    @UiThread
    private fun getVersionPost(result: String) {
        val newVersion = JSONObject(result).getString("version")
        binding.versionView.text = newVersion

        getData(QUIZ_URL + "data")
    }


    @UiThread
    private fun getDataPost(result: String) {
        val rootData = JSONArray(result)
        binding.tvQuizmain.text = rootData.getJSONObject(0).getString("question")
    }


    private val onChoiceClick = View.OnClickListener { v ->
        if (v !is Button) return@OnClickListener // Button じゃなければ終了。スマートキャスト

        isBulkEnableButton(false)
        timeLeftCountdown.cancel()
        totalElapsedTime += currentElapsedTime

        if (v.text == quizList[current].choices[0]) {
            // 正解パターン
            binding.correctIcon.visibility = View.VISIBLE
            correctCount++
            delayNext(CHOICE_DELAY_TIME)
        } else {
            // 不正解パターン
            binding.incIcon.visibility = View.VISIBLE
            delayNext(CHOICE_DELAY_TIME)
        }
    }


    /**
     * 制限時間をカウントダウンするタイマー
     */
    inner class TimeLeftCountdown : CountDownTimer(TIME_LIMIT, TIMER_INTERVAL) {
        override fun onTick(millisUntilFinished: Long) {
            animateToProgress(millisUntilFinished.toInt())
        }


        override fun onFinish() {
            totalElapsedTime += TIME_LIMIT
            isBulkEnableButton(false)
            animateToProgress(0)
            binding.timeIcon.visibility = View.VISIBLE
            delayNext(TIME_UP_DELAY_TIME)
        }


        /**
         * API Level 24 であれば、ProgressBar 自体にアニメーションのパラメータがありますが
         * 今回は 23 なので、ObjectAnimator を使って実装
         */
        private fun animateToProgress(progress: Int) {
            val anim = ObjectAnimator.ofInt(binding.countTimebar, "progress", progress)
            anim.duration = TIMER_INTERVAL
            anim.start()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quizList = randomChooseQuiz(MAX_COUNT)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // クイズの途中に Back ボタンでタイトルへ戻れないよう
            // 何も処理をしない対策
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_main, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 全てのボタンで同一のイベントリスナを使用
        binding.btAnswer1.setOnClickListener(onChoiceClick)
        binding.btAnswer2.setOnClickListener(onChoiceClick)
        binding.btAnswer3.setOnClickListener(onChoiceClick)
        binding.btAnswer4.setOnClickListener(onChoiceClick)

        next()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        timeLeftCountdown.cancel()
        _binding = null
    }


    /**
     * 次の問題へ進む処理。
     * 問題がなければリザルトへ進む。
     */
    private fun next() {
        // 次の問題がまだある場合
        if (++current < MAX_COUNT) {
            timeLeftCountdown.cancel()
            binding.countTimebar.progress = 10000
            binding.incIcon.visibility = View.GONE
            binding.correctIcon.visibility = View.GONE
            binding.timeIcon.visibility = View.GONE
            setQuiz(current)
            isBulkEnableButton(true)
            timeLeftCountdown.start()
            startTime = SystemClock.elapsedRealtime()
            return
        }

        //次の問題がもう無い場合
        val action = QuizMainFragmentDirections.actionToResult()
        findNavController().navigate(action)
    }


    /**
     * next() を遅延実行するためのメソッド
     */
    private fun delayNext(delay: Long) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(delay) {
            next()
        }
    }


    /**
     * 選択肢ボタンの isEnabled プロパティを一括で変更するメソッド
     */
    private fun isBulkEnableButton(flag: Boolean) {
        binding.btAnswer1.isEnabled = flag
        binding.btAnswer2.isEnabled = flag
        binding.btAnswer3.isEnabled = flag
        binding.btAnswer4.isEnabled = flag
    }


    /**
     * 指定の番号で、各ビューに問題データをセットするメソッド
     */
    private fun setQuiz(position: Int) {
        binding.tvQuizmain.text = quizList[position].question


        val randomChoice = quizList[position].choices.shuffled()
        binding.btAnswer1.text = randomChoice[0]
        binding.btAnswer2.text = randomChoice[1]
        binding.btAnswer3.text = randomChoice[2]
        binding.btAnswer4.text = randomChoice[3]
    }



}