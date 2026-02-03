package com.evan.englishSearch

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {
    private val mDictionaryMap = HashMap<String, String>()
    private var mToast: Toast? = null
    val mDictionary = MutableLiveData<ArrayList<View>>()

    @SuppressLint("InflateParams")
    fun showToast(activity: Activity, msg: String) {
        if (mToast != null) {
            mToast?.cancel()
            mToast = null
        }
        mToast = Toast(activity)
        val inflater = LayoutInflater.from(activity)
        val layout = inflater.inflate(R.layout.custom_toast, null) // 確保 ID 正確
        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = msg
        mToast?.setGravity(Gravity.CENTER, 0, 0)
        mToast?.view = layout
        mToast?.show()
    }

    fun cancelToast() {
        mToast?.cancel()
        mToast = null
    }

    /**
     * 關閉鍵盤
     */
    fun hideKeyboardOnClickOutside(activity: Activity, view: View) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
//        // 如果是 ViewGroup，則遞迴地為其子視圖設定監聽器
//        if (view is ViewGroup) {
//            for (i in 0 until view.childCount) {
//                val innerView = view.getChildAt(i)
//                hideKeyboardOnClickOutside(activity, innerView)
//            }
//        }
    }

    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // 找到目前擁有焦點的 View
        var view = activity.currentFocus
        // 如果沒有焦點的 View，則為 Activity 建立一個新的 View 以便取得 window token
        if (view == null) {
            view = View(activity)
        }
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun setDictionary(pAct: MainActivity) {
        viewModelScope.launch {
            mDictionaryMap.apply {
                this["Collins"] = "https://www.collinsdictionary.com/dictionary/english/"
                this["Dict"] = "https://m.dict.cn/"
                this["DictionaryCom"] = "https://www.dictionary.com/browse/"
                this["Etymology"] =
                    "https://www.etymonline.com/index.php?allowed_in_frame=0&search="
                this["Idioms"] = "https://idioms.thefreedictionary.com/"
//                this["Macmillan"] = "https://www.macmillandictionary.com/dictionary/british/"
                this["OxFord"] = "https://www.oxfordlearnersdictionaries.com/definition/english/"
//                this["Thesaurus"] = "https://thesaurus.plus/thesaurus/"
                this["Voice"] = "https://zh.forvo.com/word/"
                this["Youdao"] = "https://www.youdao.com/w/eng/"
                this["cambridge"] =
                    "https://dictionary.cambridge.org/dictionary/english-chinese-traditional/"
                this["google"] =
                    "https://translate.google.com.tw/?hl=zh-TW&sl=en&tl=zh-CN&text=%s&op=translate"
                this["ldoceonline"] = "https://www.ldoceonline.com/dictionary/"
                this["merriam-webster"] = "https://www.merriam-webster.com/dictionary/"
            }
            val viewArr = ArrayList<View>()
            var iIndex = 0
            for (iDic in mDictionaryMap) {
                iIndex++
                viewArr.add(funCreateButton(iIndex, iDic.key, iDic.value, pAct))
            }
            mDictionary.value = viewArr
        }
    }

    private fun funCreateButton(
        pIndex: Int,
        pTitle: String,
        pUrl: String,
        pAct: MainActivity
    ): View {
        val iButton = Button(pAct)
        val params = LinearLayout.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT
        )
        val res: Resources = pAct.resources
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            10f,
            res.displayMetrics
        ).toInt()
        //set margin https://stackoverflow.com/questions/12728255/in-android-how-do-i-set-margins-in-dp-programmatically
        params.setMargins(px, 0, 0, 0)
        // set padding https://stackoverflow.com/questions/9685658/add-padding-on-view-programmatically
        iButton.setPadding(px, px, px, px)
        iButton.text = pTitle
        iButton.tag = pUrl
        iButton.id = pIndex
        iButton.layoutParams = params
        iButton.setBackgroundResource(R.drawable.bg_radius_4_solid_1c89c4)
        return iButton
    }
}