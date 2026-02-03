package com.evan.englishSearch

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Toast
import java.io.PrintWriter
import java.io.StringWriter


class CrashReportApplication: Application()  {
    override fun onCreate() {
        super.onCreate()
        CrashCatch(this)
    }
}

class CrashCatch(private val crashReportApplication: CrashReportApplication) {
    private var appCtx: Context = crashReportApplication.applicationContext
    private var currentActivity: Activity? = null
    private val handler = Handler(Looper.getMainLooper())
    companion object {
        private val crashMap: HashMap<String, String> = HashMap(10)
    }
    init {
        setDefaultUncaughtExceptionHandler()
        registerActivityLifecycle()
        catchMainThreadException()
    }

    private fun setDefaultUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, e -> interceptCrash(e) }
    }

    private fun registerActivityLifecycle() {
        crashReportApplication.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = activity
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }

    private fun catchMainThreadException() {
        handler.post {
            while (true) {
                try {
                    Looper.loop()
                } catch (t: Throwable) {
                    interceptCrash(t)
                }
            }
        }
    }

    private fun interceptCrash(t: Throwable) {
        try {
            val packageManager = appCtx.packageManager
            val packageName = appCtx.packageName
            val builder = StringBuilder()
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            if (packageInfo != null) {
                crashMap.clear()
                crashMap["versionName : "] = packageInfo.versionName.toString()
                crashMap["versionCode : "] =
                    packageInfo.versionCode.toString() + ""

                val fields = Build::class.java.fields
                if (fields.isNotEmpty()) {
                    for (field in fields) {
                        field.isAccessible = true
                        crashMap[field.name] =  try {
                            field[null]!!.toString()
                        } catch (_: Exception) {
                            ""
                        }
                    }
                }


                val stringWriter = StringWriter()
                val printWriter = PrintWriter(stringWriter)
                t.printStackTrace(printWriter)

                var cause = t.cause
                while (cause != null) {
                    cause.printStackTrace(printWriter)
                    cause = t.cause
                }

                printWriter.close()
                val tErrorStr = stringWriter.toString()

                builder.append("\n===========Crash  Log  Begin============\n")
                    .append(tErrorStr).append("\n")

                for ((key, value) in crashMap) {
                    builder.append(key).append(" : ").append(value).append("\n\n")
                }

                builder.append("\n===========Crash  Log   End==============\n")
            }

            val errorStr = builder.toString()


            val alertDialog = AlertDialog.Builder(currentActivity)
                .setCancelable(true)
                .setTitle("crash-info")
                .setMessage(errorStr)
                .setPositiveButton(
                    "確定"
                ) { dialog, _ ->
                    dialog?.dismiss()
                }


                .setNeutralButton(
                    "寄信"
                ) { dialog, _ ->
                    val clipboardManager =
                        crashReportApplication.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, errorStr))
                    val toast = Toast(appCtx)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.setText("複製完成")
                    toast.show()

                    val emailIntent = Intent(Intent.ACTION_SEND)
                    emailIntent.setType("message/rfc822"); // only email apps should handle this
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayListOf("blackere1208@gmail.com").toTypedArray())
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "測誤訊息傳送")
                    emailIntent.putExtra(Intent.EXTRA_TEXT, errorStr)

                    currentActivity?.startActivity(emailIntent)
                    dialog?.dismiss()
                }
                .setNegativeButton(
                    "取消"
                ) { dialog, _ ->
                    dialog?.dismiss()
                }.create()
            alertDialog.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}