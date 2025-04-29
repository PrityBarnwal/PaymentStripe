package com.example.paymentstripe.api

import com.example.paymentstripe.Utils.KEY
import com.example.paymentstripe.model.CustomerModel
import com.example.paymentstripe.model.EpheralModel
import com.example.paymentstripe.model.PaymentIntentModel
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterface {

    @Headers("Authorization: Bearer $KEY")
    @POST("v1/customers")
    suspend fun getCustomer(): Response<CustomerModel>

    @Headers("Authorization: Bearer $KEY", "Stripe-Version: 2025-03-31.basil")
    @POST("v1/ephemeral_keys")
    suspend fun getEphemeralKey(
        @Query("customer") customer: String
    ): Response<EpheralModel>

    @FormUrlEncoded
    @Headers("Authorization: Bearer $KEY")
    @POST("v1/payment_intents")
    suspend fun getPaymentIntent(
        @Field("customer") customer: String,
        @Field("amount") amount: String = "100",
        @Field("currency") currency: String = "eur",
        @Field("automatic_payment_methods[enabled]") automaticPayment: Boolean = true
    ): Response<PaymentIntentModel>
}