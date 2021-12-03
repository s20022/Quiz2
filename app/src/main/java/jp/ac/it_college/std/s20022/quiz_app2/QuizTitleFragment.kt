package jp.ac.it_college.std.s20022.quiz_app2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import jp.ac.it_college.std.s20022.quiz_app2.databinding.FragmentQuizTitleBinding

class QuizTitleFragment : Fragment() {
    private var _binding: FragmentQuizTitleBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizTitleBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.quizStart.setOnClickListener {
            findNavController().navigate(R.id.action_to_main)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}