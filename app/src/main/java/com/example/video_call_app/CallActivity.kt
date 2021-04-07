package com.example.video_call_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class CallActivity : AppCompatActivity() {

    var username = ""
    var frienduser = ""
    var isPeerConnected = false
    var firebaseRef = Firebase.database.getReference("users")
    var isAudio = true
    var isVideo = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        val call = findViewById<Button>(R.id.callBtn)
        val togAudio = findViewById<ImageView>(R.id.toogleAudio)
        val togVideo = findViewById<ImageView>(R.id.toggleVideo)


        //Correct the Error of Empty Intent
        //No Values Coming Through
        username = "kulraj"//intent.getStringExtra("username")!!

        call.setOnClickListener {
            val fname_edit = findViewById<EditText>(R.id.friendNameEdit)
            frienduser = fname_edit.text.toString()
            sendCallRequest()
        }

        togAudio.setOnClickListener {
            isAudio = !isAudio
            callJavascriptFunction("javascript:toggleAudio(\"${isAudio}\")")
            val toggle_audio = findViewById<ImageView>(R.id.toogleAudio)

            toggle_audio.setImageResource(if (isAudio) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24)
        }

        togVideo.setOnClickListener {
            isVideo = !isVideo
            callJavascriptFunction("javascript:toggleVideo(\"${isVideo}\")")
            val toggle_video = findViewById<ImageView>(R.id.toggleVideo)

            toggle_video.setImageResource(if (isVideo) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_videocam_off_24)
        }

        setupWebView()
    }

    private fun sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "You are not Connected. Check your Internet", Toast.LENGTH_LONG).show()
            return
        }

        firebaseRef.child(frienduser).child("incoming").setValue(username)
        firebaseRef.child(frienduser).child("isAvailable").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value.toString() == "true") {
                    listenForConnId()
                }
            }
        })
    }

    private fun listenForConnId() {
        firebaseRef.child(frienduser).child("connId").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null)
                    return
                switchToControls()
                callJavascriptFunction("javascript:startCall(\"${snapshot.value}\"")
            }
        })
    }

    private fun setupWebView() {
        val webV = findViewById<WebView>(R.id.webView)
        webV.webChromeClient = object: WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                super.onPermissionRequest(request)
                request?.grant(request.resources)
            }
        }

        webV.settings.javaScriptEnabled = true
        webV.settings.mediaPlaybackRequiresUserGesture = false
        webV.addJavascriptInterface(Javascriptinterface(this), "Android")

        loadVideoCall()
    }

    private fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"
        val webV = findViewById<WebView>(R.id.webView)
        webV.loadUrl(filePath)

        webV.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                intializePeer()
            }
        }
    }

    var u_id = ""

    private fun intializePeer() {

        u_id = getUniqueId()

        callJavascriptFunction("javascript:init(\"${getUniqueId()}\")")

        firebaseRef.child(username).child("incoming").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                onCallRequest(snapshot.value as? String)
            }

        })
    }

    private fun onCallRequest(caller: String?) {
        if (caller == null) return

        val call = findViewById<View>(R.id.calllayout)
        call.visibility = View.VISIBLE
        val itext = findViewById<TextView>(R.id.incomingCallTxt)
        itext.text = "$caller is calling..."

        val acc_btn = findViewById<ImageView>(R.id.acceptCall)
        acc_btn.setOnClickListener {
            firebaseRef.child(username).child("connId").setValue(u_id)
            firebaseRef.child(username).child("isAvailable").setValue(true)

            call.visibility = View.GONE

            switchToControls()
        }

        val r_btn = findViewById<ImageView>(R.id.rejectCall)

        r_btn.setOnClickListener {
            firebaseRef.child(username).child("incoming").setValue(null)
            call.visibility = View.GONE
        }

    }

    private fun switchToControls() {
        val input = findViewById<View>(R.id.inputLayout)
        val controls = findViewById<View>(R.id.callControl)

        input.visibility = View.GONE
        controls.visibility = View.VISIBLE
    }

    private fun getUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    private fun callJavascriptFunction(functionString: String) {
        val webV = findViewById<WebView>(R.id.webView)
        webV.post { webV.evaluateJavascript(functionString, null) }
    }

    fun onPeerConnected() {
        isPeerConnected = true
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        firebaseRef.child(username).setValue(null)
        val web = findViewById<WebView>(R.id.webView)
        web.loadUrl("about:blank")
        super.onDestroy()
    }
}