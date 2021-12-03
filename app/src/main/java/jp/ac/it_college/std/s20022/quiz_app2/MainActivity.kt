package jp.ac.it_college.std.s20022.quiz_app2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.ac.it_college.std.s20022.quiz_app2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}