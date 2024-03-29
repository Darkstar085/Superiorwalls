package dev.jahir.frames.extensions.utils

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.google.gson.Gson
import dev.jahir.frames.data.models.DetailedPurchaseRecord
import dev.jahir.frames.data.models.InternalDetailedPurchaseRecord
import dev.jahir.frames.data.models.PseudoDetailedPurchaseRecord

fun Purchase.asDetailedPurchase(): DetailedPurchaseRecord? =
    try {
        val pseudoDetailedRecord =
            Gson().fromJson(originalJson, PseudoDetailedPurchaseRecord::class.java)
        val internalDetailedRecord = InternalDetailedPurchaseRecord(pseudoDetailedRecord, this)
        Gson().fromJson(internalDetailedRecord.toJSONString(0), DetailedPurchaseRecord::class.java)
    } catch (e: Exception) {
        null
    }

fun PurchaseHistoryRecord.asDetailedPurchase(): DetailedPurchaseRecord? =
    try {
        val purchase = Purchase(originalJson, signature)
        val pseudoDetailedRecord =
            Gson().fromJson(originalJson, PseudoDetailedPurchaseRecord::class.java)
        val internalDetailedRecord =
            InternalDetailedPurchaseRecord(pseudoDetailedRecord, purchase, true)
        Gson().fromJson(internalDetailedRecord.toJSONString(0), DetailedPurchaseRecord::class.java)
    } catch (e: Exception) {
        null
    }

internal fun purchaseStateToText(@Purchase.PurchaseState purchaseState: Int): String =
    when (purchaseState) {
        Purchase.PurchaseState.PENDING -> "Pending"
        Purchase.PurchaseState.PURCHASED -> "Purchased"
        else -> "Unspecified"
    }

val ProductDetails.price: String
    get() = if (productType == BillingClient.ProductType.INAPP)
        oneTimePurchaseOfferDetails?.formattedPrice ?: "0"
    else subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
        ?: "0"

val ProductDetails.priceAmountMicros: Long
    get() = if (productType == BillingClient.ProductType.INAPP)
        oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L
    else subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceAmountMicros
        ?: 0L
