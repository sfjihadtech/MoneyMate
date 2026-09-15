package com.moneymate.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*

/**
 * Google Play is the source of truth for Premium prices and entitlement.
 * Create subscription products in Play Console with these IDs; displayed prices
 * are then read directly from ProductDetails, never hardcoded in the app.
 */
class PlayBillingManager(
    context: Context,
    private val onChanged: (BillingSnapshot) -> Unit
) : PurchasesUpdatedListener {

    data class Plan(val id: String, val title: String, val price: String, val details: ProductDetails)
    data class BillingSnapshot(
        val ready: Boolean = false,
        val premium: Boolean = false,
        val plans: List<Plan> = emptyList(),
        val message: String? = null
    )

    companion object {
        const val MONTHLY = "moneymate_premium_monthly"
        const val SIX_MONTHS = "moneymate_premium_6months"
        const val YEARLY = "moneymate_premium_yearly"
        val PRODUCT_IDS = listOf(MONTHLY, SIX_MONTHS, YEARLY)
    }

    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    fun start() {
        if (billingClient.isReady) { refresh(); return }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) refresh()
                else onChanged(BillingSnapshot(message = result.debugMessage))
            }
            override fun onBillingServiceDisconnected() {
                onChanged(BillingSnapshot(message = "Google Play Billing disconnected"))
            }
        })
    }

    private fun refresh() {
        queryPlans()
        queryEntitlement()
    }

    private fun queryPlans() {
        val products = PRODUCT_IDS.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(products).build()
        ) { result, response ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                onChanged(BillingSnapshot(ready = true, message = result.debugMessage)); return@queryProductDetailsAsync
            }
            val plans = response.productDetailsList.mapNotNull { details ->
                val phase = details.subscriptionOfferDetails
                    ?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()
                    ?: return@mapNotNull null
                val title = when (details.productId) {
                    MONTHLY -> "Monthly"
                    SIX_MONTHS -> "6 Months"
                    YEARLY -> "Yearly"
                    else -> details.name
                }
                Plan(details.productId, title, phase.formattedPrice, details)
            }
            onChanged(BillingSnapshot(ready = true, plans = plans))
        }
    }

    private fun queryEntitlement() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val active = purchases.any { p ->
                    p.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        p.products.any(PRODUCT_IDS::contains)
                }
                onChanged(BillingSnapshot(ready = true, premium = active))
                purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
                    .forEach(::acknowledge)
            }
        }
    }

    fun purchase(activity: Activity, plan: Plan) {
        val offerToken = plan.details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken.isNullOrBlank()) {
            onChanged(BillingSnapshot(ready = true, message = "No eligible Google Play offer is available")); return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(plan.details)
            .setOfferToken(offerToken)
            .build()
        billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productParams)).build()
        )
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases.orEmpty().filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach(::acknowledge)
            queryEntitlement()
        } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            onChanged(BillingSnapshot(ready = true, message = result.debugMessage))
        }
    }

    private fun acknowledge(purchase: Purchase) {
        billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        ) { queryEntitlement() }
    }

    fun close() { if (billingClient.isReady) billingClient.endConnection() }
}
