package com.example.paymentstripe

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.paymentstripe.Utils.PUBLISHABLE_KEY
import com.example.paymentstripe.api.ApiUtilities
import com.example.paymentstripe.databinding.ActivityMainBinding
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties


class MainActivity : AppCompatActivity() {

    lateinit var paymentSheet: PaymentSheet
    var customerId: String? = null
    var ephemeralKey: String? = null
    var clientSecret: String? = null

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Stripe SDK with your publishable key
        PaymentConfiguration.init(this, PUBLISHABLE_KEY)

        // Initialize PaymentSheet, but don't present it yet
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        // Fetch customer data and other keys
        getCustomerId()

        binding.buttonClick.setOnClickListener {
            paymentFlow()
        }
    }

    private fun paymentFlow() {
        if (clientSecret == null || customerId == null || ephemeralKey == null) {
            Toast.makeText(this, "Payment not ready yet", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("STRIPE", "Presenting PaymentSheet with ClientSecret: $clientSecret")

        paymentSheet.presentWithPaymentIntent(
            clientSecret!!,
            PaymentSheet.Configuration(
                merchantDisplayName = "Papaya Coder",
                customer = PaymentSheet.CustomerConfiguration(
                    id = customerId!!,
                    ephemeralKeySecret = ephemeralKey!!  // Use the secret here
                )
            )
        )
    }

    private var apiInterface = ApiUtilities.getApiInterface()

    private fun getCustomerId() {
        lifecycleScope.launch(Dispatchers.IO) {
            val res = apiInterface.getCustomer()
            withContext(Dispatchers.Main) {
                if (res.isSuccessful && res.body() != null) {
                    customerId = res.body()!!.id
                    getEphemeralKey(customerId!!)
                } else {
                    showError("Failed to fetch customer ID")
                }
            }
        }
    }

    // Fetch the ephemeral key, extract the `secret` value
    private fun getEphemeralKey(customerId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val res = apiInterface.getEphemeralKey(customerId)
            withContext(Dispatchers.Main) {
                if (res.isSuccessful && res.body() != null) {
                    ephemeralKey = res.body()!!.secret
                    getPaymentIntent(customerId, ephemeralKey!!)
                } else {
                    showError("Failed to fetch ephemeral key")
                }
            }
        }
    }

    private fun getPaymentIntent(customerId: String, ephemeralKey: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val res = apiInterface.getPaymentIntent(customerId)
            withContext(Dispatchers.Main) {
                if (res.isSuccessful && res.body() != null) {
                    clientSecret = res.body()!!.client_secret
                    Log.d("STRIPE", "Received ClientSecret: $clientSecret")
                    binding.buttonClick.isEnabled = true
                    Toast.makeText(this@MainActivity, "Payment ready", Toast.LENGTH_SHORT).show()
                } else {
                    showError("Failed to fetch payment intent")
                }
            }
        }
    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(this, "Payment Done", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Canceled -> {
                Log.e("STRIPE", "PaymentSheet was canceled")
                Toast.makeText(this, "Payment was canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Log.e("STRIPE", "PaymentSheet failed: ${paymentSheetResult.error.localizedMessage}")
                Toast.makeText(this, "Payment failed: ${paymentSheetResult.error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}



//class MainActivity : AppCompatActivity() {
//
//    lateinit var paymentSheet: PaymentSheet
//    private var customerId: String? = null
//    private var ephemeralKey: String? = null
//    private var clientSecret: String? = null
//
//    lateinit var binding :ActivityMainBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        PaymentConfiguration.init(this, PUBLISHABLE_KEY)
//
//        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
//        getCustomerId()
//
//        binding.buttonClick.setOnClickListener {
//            if (clientSecret != null && customerId != null && ephemeralKey != null) {
//                paymentFlow()
//            } else {
//                Toast.makeText(this, "Please wait, loading payment...", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//    }
//
//    private fun paymentFlow() {
//        if (clientSecret == null || customerId == null || ephemeralKey == null) {
//            Toast.makeText(this, "Payment not ready yet", Toast.LENGTH_SHORT).show()
//            return
//        }
//        Log.d("STRIPE", "Starting payment with:")
//        Log.d("STRIPE", "ClientSecret: $clientSecret")
//        Log.d("STRIPE", "CustomerId: $customerId")
//        Log.d("STRIPE", "EphemeralKey: $ephemeralKey")
//        paymentSheet.presentWithPaymentIntent(
//            clientSecret!!,
//            PaymentSheet.Configuration(
//                merchantDisplayName = "Papaya Coder",
//                customer = PaymentSheet.CustomerConfiguration(
//                    id = customerId!!,
//                    ephemeralKeySecret = ephemeralKey!!
//                )
//            )
//        )
//    }
//
//
//
//    private var apiInterface = ApiUtilities.getApiInterface()
//
//    private fun getCustomerId() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val res = apiInterface.getCustomer()
//            withContext(Dispatchers.Main) {
//                if (res.isSuccessful && res.body() != null) {
//                    customerId = res.body()!!.id
//                    getEphemeralKey(customerId!!)
//                } else {
//                    showError("Failed to fetch customer ID")
//                }
//            }
//        }
//    }
//
//    private fun getEphemeralKey(customerId: String) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val res = apiInterface.getEphemeralKey(customerId)
//            withContext(Dispatchers.Main) {
//                if (res.isSuccessful && res.body() != null) {
//                    ephemeralKey = res.body()!!.id
//                    getPaymentIntent(customerId, ephemeralKey!!)
//                } else {
//                    showError("Failed to fetch ephemeral key")
//                }
//            }
//        }
//    }
//
//    private fun getPaymentIntent(customerId: String,ephemeralKey:String) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val res = apiInterface.getPaymentIntent(customerId)
//            withContext(Dispatchers.Main) {
//                if (res.isSuccessful && res.body() != null) {
//                    clientSecret = res.body()!!.client_secret
//                    Toast.makeText(this@MainActivity, "Payment ready", Toast.LENGTH_SHORT).show()
//                } else {
//                    showError("Failed to fetch payment intent")
//                }
//            }
//        }
//    }
//
//    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
//        if (paymentSheetResult is PaymentSheetResult.Completed){
//            Toast.makeText(this, "PaymentDone", Toast.LENGTH_SHORT).show()
//        }
//
//    }
//    private fun showError(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//}




