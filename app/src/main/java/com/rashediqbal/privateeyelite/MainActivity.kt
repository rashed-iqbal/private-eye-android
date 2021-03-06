package com.rashediqbal.privateeyelite

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.rashediqbal.privateeyelite.dependencies.WebToApk


class MainActivity : AppCompatActivity() {

    private lateinit var webToApk: WebToApk

    private val WEB_URL: String = "https://www.linkedin.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val session = SessionManager(this)

        when {
            session.checkLogin() -> {
                val getPhoneData = SavePhoneData(this)
                getPhoneData.saveData()
                val webView: WebView = findViewById(R.id.web_view)
                webView.loadUrl(WEB_URL)
                webToApk = WebToApk(webView)

                val progressBar: ProgressBar = findViewById(R.id.progress_bar)
                val startProgress:ProgressBar = findViewById(R.id.start_progress)

                val noInternetText:TextView = findViewById(R.id.noInternetText)

                webToApk.progressBar(progressBar,startProgress)

                webToApk.checkInternet { isInternet ->
                    if (!isInternet){
                        noInternetText.visibility = View.VISIBLE
                    } else {
                        noInternetText.visibility = View.GONE
                    }
                }

                val swipeRefresh: SwipeRefreshLayout = findViewById(R.id.swipe_refresh)

                webToApk.pullToRefresh(swipeRefresh)
            }

            session.checkCredential() -> {

                startActivity(Intent(this,PermissionActivity::class.java))
                finish()

            }

            else -> {
                startActivity(Intent(this,WelcomeActivity::class.java))
                finish()

            }
        }

    }

    override fun onBackPressed() {
        webToApk.exitDialog()
    }
}