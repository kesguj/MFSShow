package com.verygoodsecurity.demoshow.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.verygoodsecurity.demoshow.R
import com.verygoodsecurity.vgsshow.VGSShow
import com.verygoodsecurity.vgsshow.core.VGSEnvironment
import com.verygoodsecurity.vgsshow.core.listener.VGSOnResponseListener
import com.verygoodsecurity.vgsshow.core.network.client.VGSHttpMethod
import com.verygoodsecurity.vgsshow.core.network.model.VGSResponse
import com.verygoodsecurity.vgsshow.widget.VGSTextView
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private lateinit var vgsShow: VGSShow
    lateinit var editTextClientId : EditText
    lateinit var editTextLastFourD : EditText
    lateinit var pgsBarView : ProgressBar

    lateinit var textHash : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        editTextClientId = findViewById(R.id.editTextPhone)
        editTextLastFourD = findViewById(R.id.editTextLastFourD)
        pgsBarView = findViewById(R.id.pgsBarView)

        val infoMessage = findViewById<VGSTextView>(R.id.infoMessage)
        infoMessage.setContentPath("errorMessage")

        initListeners()

    }

    private fun headerValues(): Pair<String, String> {
        val password = "Gtp@1234"
        val requestId = getRandomNumberId()
        val toHash = password+requestId
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hash = (sha256.digest(toHash.toString().toByteArray())).fold("", { str, it -> str + "%02x".format(it) })

        return Pair(hash, requestId)
    }

    private fun getRandomNumberId(): String{
        val randonNumber = (0..90000000).random()
        return randonNumber.toString()
    }

    private fun createRequestPayload(): Map<String, String> {

/*        return mapOf("mobilePhoneNumber" to "256753396421",
            "last4Digits" to "9218")*/
        // val phone = editTextClientId.text.toString()
        val lastFourD = editTextLastFourD.text.toString()

        return mapOf(//"mobilePhoneNumber" to phone,
            "last4Digits" to lastFourD)
    }

    private fun initListeners() {

        btnViewDetails?.setOnClickListener {
            //startActivity(Intent(this, CollectAndShowActivity::class.java))
            vgsShow = VGSShow(this, "tnto06ue3ir", VGSEnvironment.Sandbox())
            vgsShow.subscribe(infoPAN)
            vgsShow.subscribe(infoExpDate)
            vgsShow.subscribe(infoCVV)
            vgsShow.subscribe(infoName)
            //vgsShow.subscribe(infoMessage)

            val clientId = editTextClientId.text.toString()

            btnViewDetails.visibility=View.INVISIBLE
            pgsBarView.visibility= View.VISIBLE

            infoPAN?.addTransformationRegex(
                "(\\d{4})(\\d{4})(\\d{4})(\\d{4})".toRegex(),
                "\$1  \$2  \$3  \$4"
            )
           //infoPAN?.addTransformationRegex("-".toRegex(), " - ")

            vgsShow.addOnResponseListener(object : VGSOnResponseListener {

                override fun onResponse(response: VGSResponse) {
                    when (response) {
                        is VGSResponse.Success -> {
                            pgsBarView.visibility= View.GONE
                            btnViewDetails.visibility=View.VISIBLE
                            //infoMessage.setTextColor(Color.parseColor("#008000"))
                            Toast.makeText(applicationContext,"SUCCESS",Toast.LENGTH_SHORT).show()
                            Log.d("VALUE1", "$response")
                        }
                        is VGSResponse.Error -> {
                            pgsBarView.visibility= View.GONE
                            btnViewDetails.visibility=View.VISIBLE
/*                            infoMessage.setHint("ERROR")
                            infoMessage.setHintTextColor(Color.parseColor("#008000"))*/
                            Toast.makeText(applicationContext,"ERROR",Toast.LENGTH_SHORT).show()
                            Log.d("VALUE2", "$response")
                        }
                    }
                }
            })

            //val (password,requestId) = headerValues()

            //vgsShow.setCustomHeader("username", "GTP")
            //vgsShow.setCustomHeader("password", "Gtp@1234")
            //vgsShow.setCustomHeader("password", "$password")
            //vgsShow.setCustomHeader("programId", "16")
            //vgsShow.setCustomHeader("authorization", "tok_sandbox_wDDmADE38QMJ3MrytiAGcr")
            //vgsShow.setCustomHeader("requestId", "$requestId")
            vgsShow.setCustomHeader("programId", "tok_sandbox_atEaAQTw5KQo5Hz7T2uywx")
            vgsShow.setCustomHeader("requestId", getRandomNumberId())

            //vgsShow.requestAsync("/MFSAfricaCard/v1/getcardinfo", VGSHttpMethod.POST, createRequestPayload())
            vgsShow.requestAsync("/rest/api/v1/accounts/$clientId/pci-info", VGSHttpMethod.POST, createRequestPayload())

        }
    }

    companion object {
        const val TENANT_ID = "tnto06ue3ir"
        const val ENVIRONMENT = "sandbox"
        const val COLLECT_CUSTOM_HOSTNAME = "collect-android-testing.verygoodsecurity.io/test"
    }
}

typealias ShowResponse = com.verygoodsecurity.vgsshow.core.network.model.VGSResponse
typealias CollectResponse = com.verygoodsecurity.vgscollect.core.model.network.VGSResponse?
typealias CollectSuccessResponse = com.verygoodsecurity.vgscollect.core.model.network.VGSResponse.SuccessResponse?

