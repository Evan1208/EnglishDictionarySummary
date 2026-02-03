package customView.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.evan.englishSearch.R

/** 全螢幕 dialog **/
class FullScreenLoadingDialog(
    pContext: Context,
    private val mViewBinding: ViewDataBinding
) : BaseDialog(pContext, R.style.dialogNobackground) {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(mViewBinding.root)

        // 讓 Dialog 畫面占滿整個手機螢幕
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mViewBinding.root.setOnClickListener {
            this@FullScreenLoadingDialog.cancel()
        }
    }
}