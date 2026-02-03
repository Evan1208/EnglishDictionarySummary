package customView.editText

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText

@SuppressLint("AppCompatCustomView")
class ViewEditText(context: Context, attrs: AttributeSet?) : EditText(context, attrs) {

    //Define our custom Listener interface
    interface ItfOnKeyPreIme {
        //系統的back
        fun onPressedBackKey()
        //keyboard的finish or enter
        fun onDoneOrEnter() {
        }
    }

    private var onKeyPre: ItfOnKeyPreIme? = null

    fun setOnKeyPreIme(onKeyPreIme: ItfOnKeyPreIme) {
        onKeyPre = onKeyPreIme
        this.setOnKeyListener { _, keyCode, event ->
            if (keyCode == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                onKeyPre?.onDoneOrEnter()
            }
            false
        }
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onKeyPre?.onPressedBackKey()
            return false
        }
        return super.onKeyPreIme(keyCode, event)
    }

}