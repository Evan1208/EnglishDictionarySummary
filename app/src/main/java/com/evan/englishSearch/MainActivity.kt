package com.evan.englishSearch

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.evan.englishSearch.databinding.DialogProgressBinding
import com.evan.englishSearch.databinding.MainActivityBinding
import customView.dialog.FullScreenLoadingDialog
import customView.editText.ViewEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : FragmentActivity() {

    private var isStartLoading = false
    private val mConnectUrl = StringBuffer()
    private val mUrlNet = StringBuffer()
    private var mFullScreenLoadingDialog: FullScreenLoadingDialog? = null
    private var mSelectedView: View? = null
        set(value) {
            field?.setBackgroundResource(R.drawable.bg_radius_4_solid_1c89c4)
            value?.setBackgroundResource(R.drawable.bg_radius_4_solid_4fb980)
            field = value
        }
    private val mMainViewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory()
        )[MainViewModel::class.java]
    }

    private val binding: MainActivityBinding by lazy {
        val iBinding = MainActivityBinding.inflate(layoutInflater)
        setContentView(iBinding.root)
        iBinding
    }

    private val clickWebView =
        View.OnClickListener { v ->
            mSelectedView = v
            connectUrl()
        }

    private fun connectUrl() {
        val selectedView = mSelectedView
        if (selectedView != null) {
            mUrlNet.delete(0, mUrlNet.length)
            mUrlNet.append(selectedView.tag.toString())

            mConnectUrl.delete(0, mConnectUrl.length)
            val searchEnglish = binding.etEnglish.text.toString()
            mMainViewModel.hideKeyboardOnClickOutside(this, binding.etEnglish)
            if (searchEnglish.isEmpty()) {
                mMainViewModel.showToast(this, this.getString(R.string.error_empty_input_field))
            } else {
                mMainViewModel.cancelToast()
                if (mUrlNet.toString().contains("Google", true)) {
                    mConnectUrl.append(mUrlNet.toString().replace("%s", searchEnglish))
                } else {
                    mConnectUrl.append(mUrlNet.toString() + searchEnglish)
                }
                setConnect()
            }
        }
    }

    private fun setConnect() {
        val noCacheHeaders: MutableMap<String, String> = HashMap(2)
        noCacheHeaders["Pragma"] = "no-cache"
        noCacheHeaders["Cache-Control"] = "no-cache"
        binding.webView01.loadUrl(mConnectUrl.toString(), noCacheHeaders)
        synchronized(this) {
            if (mFullScreenLoadingDialog == null) {
                val iViewBinding: DialogProgressBinding =
                    DialogProgressBinding.inflate(LayoutInflater.from(this@MainActivity))
                mFullScreenLoadingDialog = FullScreenLoadingDialog(this@MainActivity, iViewBinding)
                mFullScreenLoadingDialog?.apply {
                    setOnDismissListener {
                        removeLoading()
                    }
                    setOnCancelListener {
                        removeLoading()
                    }
                    show()
                }
            }
        }
    }

    private fun removeLoading() {
        mFullScreenLoadingDialog?.cancel()
        mFullScreenLoadingDialog = null
        isStartLoading = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 取得根佈局並設定 Insets 監聽
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 套用 padding，避免內容被狀態列或導航列遮住
            v.updatePadding(top = insets.top, bottom = insets.bottom)
            windowInsets
        }
        initObserve()
        logIntent(intent)
        setWebView()
        initListener()
        text()

    }

    private fun text() {
        val intent = Intent(this, MainActivity::class.java)
        intent.setAction(Intent.ACTION_VIEW)

        val shortcutInfo = ShortcutInfoCompat.Builder(this, "com.evan.englishSearch")
            .setShortLabel("Running")
            .setLongLabel("Start running")
            .addCapabilityBinding("actions.intent.GET_THING", "thing.name", arrayListOf("q"))
            .setIntent(intent) // Push the shortcut
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(this, shortcutInfo)
    }

    private fun initListener() {
        binding.apply {
            etEnglish.setOnKeyPreIme(object : ViewEditText.ItfOnKeyPreIme {
                override fun onPressedBackKey() {
                }

                override fun onDoneOrEnter() {
                    connectUrl()
                }

            })

            tvCross.setOnClickListener {
                etEnglish.setText("")
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebView() {
        val iWebSetting = binding.webView01.settings
        iWebSetting.javaScriptEnabled = true
        iWebSetting.defaultTextEncodingName = "utf-8"
        iWebSetting.domStorageEnabled = true
        //可以縮放
        iWebSetting.setSupportZoom(true)
        iWebSetting.builtInZoomControls = true
        // 符合螢幕大小
        iWebSetting.useWideViewPort = true
        iWebSetting.loadWithOverviewMode = true

        isStartLoading = false
        binding.webView01.webViewClient = object : WebViewClient() {
            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (isStartLoading) {
                    removeLoading()
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isStartLoading = true
            }
        }
    }

    private fun initObserve() {
        mMainViewModel.setDictionary(this@MainActivity)
        mMainViewModel.mDictionary.observe(this@MainActivity) {
            for (iView in it) {
                if (mSelectedView == null) {
                    mSelectedView = iView
                }
                iView.setOnClickListener(clickWebView)
                binding.linearLayout01.addView(iView)
            }
        }
    }

    private fun logIntent(intent: Intent) {
        val bundle: Bundle? = intent.extras

        Log.d("Evan", "======= logIntent ========= $bundle")
        Log.d("Evan", "Logging intent data start")
        if (bundle == null) {
            return
        }
        bundle.keySet().forEach { key ->
            Log.d("Evan", "[$key=${bundle.get(key)}]")
            Log.d("Evan", "LOL => ${bundle.keySet().first()}")
            Log.d("Evan", "LOL => ${"${bundle.get(bundle.keySet().first())}"}")
        }

        Log.d("Evan", "Logging intent data complete")

        runBlocking {
            val job = launch {

            }
        }
    }

}

