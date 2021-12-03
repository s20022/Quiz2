package jp.ac.it_college.std.s20022.quiz_app2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import jp.ac.it_college.std.s20022.quiz_app2.databinding.FragmentQuizResultBinding


class QuizResultFragment : Fragment() {
    private val args by navArgs<QuizResultFragmentArgs>()

    private var _binding: FragmentQuizResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_to_title)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvCleartime.text = getString(R.string.point_format, args.correctCount)
        val minutes = args.totalElapsedTime / 1000 / 60
        val second = args.totalElapsedTime / 1000 % 60
        val millis = args.totalElapsedTime % 1000 / 10
        binding.tvanswerResult.text = getString(
            R.string.time_format,
            minutes, second, millis
        )
        binding.btBack.setOnClickListener {
            findNavController().navigate(R.id.action_to_title)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
