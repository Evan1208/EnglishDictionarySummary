package customView.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle

open class BaseDialog(pContext: Context, pStyle:Int):
    Dialog(pContext, pStyle) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    interface OnDialogClickListener {

        fun onClick(dialog: BaseDialog) {
        }
    }

    interface  ItfDialogFinish {


        fun onData(pAny: Any) {

        }

        fun onData(pString: String) {

        }

        fun onGoto() {

        }

        fun onSearchNow() {

        }

        fun onFinish() {

        }

    }

}