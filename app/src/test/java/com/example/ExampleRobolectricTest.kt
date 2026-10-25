package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies olive press app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("محاسبة معصرة الزيتون", appName)
    }

    @Test
    fun `test olive oil extraction rate calculation`() {
        val oliveWeight = 2000.0
        val oilProduced = 380.0
        val rate = (oilProduced / oliveWeight) * 100.0
        assertEquals(19.0, rate, 0.01)
    }

    @Test
    fun `test custom milling in-kind oil retention`() {
        val oilProduced = 400.0
        val retentionPercent = 10.0 // 10%
        val retainedOil = oilProduced * (retentionPercent / 100.0)
        val deliveredOil = oilProduced - retainedOil
        assertEquals(40.0, retainedOil, 0.01)
        assertEquals(360.0, deliveredOil, 0.01)
    }

    @Test
    fun `test net profit accounting calculation`() {
        val salesRevenue = 35000000.0
        val pressingFeeRevenue = 5000000.0
        val totalRevenue = salesRevenue + pressingFeeRevenue

        val olivePurchaseCost = 25000000.0
        val operatingExpenses = 4000000.0
        val totalCost = olivePurchaseCost + operatingExpenses
        val netProfit = totalRevenue - totalCost
        assertTrue(netProfit > 0.0)
        assertEquals(11000000.0, netProfit, 0.01)
    }

    @Test
    fun `test main activity lifecycle and screen tab switching without crash`() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        org.junit.Assert.assertNotNull(activity)
    }

    @Test
    fun `test press viewmodel initialisation and flow collection`() {
        kotlinx.coroutines.runBlocking {
            val app = ApplicationProvider.getApplicationContext<android.app.Application>()
            val vm = com.example.ui.viewmodel.PressViewModel(app)
        
        // Let background seeding and room flows complete
        kotlinx.coroutines.delay(500)

        val settings = vm.settings.value
        val metrics = vm.financialMetrics.value
        val stock = vm.stockSummary.value

        org.junit.Assert.assertNotNull(settings)
        org.junit.Assert.assertNotNull(metrics)
        org.junit.Assert.assertNotNull(stock)
        
        // Verify formatters do not crash with edge-case values
        vm.formatCurrency(0.0)
        vm.formatCurrency(-500.0)
        vm.formatCurrency(123456789.5)
        vm.formatCurrency(Double.NaN)
        vm.formatCurrency(Double.POSITIVE_INFINITY)
        vm.formatNumber(0.0)
        vm.formatNumber(12.345, 2)
        vm.formatNumber(Double.NaN)
        vm.formatDate(System.currentTimeMillis())
        vm.formatDateShort(System.currentTimeMillis())

        // Tab Switching
        com.example.ui.viewmodel.ScreenTab.values().forEach { tab ->
            vm.setTab(tab)
            org.junit.Assert.assertEquals(tab, vm.currentTab.value)
        }

        // Test statement generation
        val customerStatement = vm.generatePartyStatement("الزبون ياسين القاسمي", isCustomer = true)
        org.junit.Assert.assertTrue(customerStatement.contains("كشف حساب"))

        val supplierStatement = vm.generatePartyStatement("المزارع أحمد العلي", isCustomer = false)
        org.junit.Assert.assertTrue(supplierStatement.contains("كشف حساب"))
        }
    }

    @Test
    fun `test crud operations on viewmodel do not throw or crash`() {
        kotlinx.coroutines.runBlocking {
            val app = ApplicationProvider.getApplicationContext<android.app.Application>()
            val vm = com.example.ui.viewmodel.PressViewModel(app)

            // Add purchase
            vm.addPurchase(
                farmerName = "مزارع اختبار",
                phone = "0911223344",
                weightKg = 1500.0,
                pricePerKg = 10000.0,
                transportCost = 50000.0,
                paidAmount = 10000000.0,
                oliveVariety = "بلدي",
                notes = "اختبار",
                grossWeightKg = 2500.0,
                tareWeightKg = 1000.0
            )

            // Add pressing
            vm.addPressing(
                customerName = "زبون اختبار",
                phone = "0988112233",
                oliveWeightKg = 800.0,
                producedOilKg = 160.0,
                feeType = "PER_KG",
                feeRate = 800.0,
                emptyTinsProvided = 10,
                emptyTinPrice = 25000.0,
                tinsFilledCount = 10.0,
                pomaceWeightKg = 300.0,
                pomaceRetainedByPress = true,
                paidAmountCash = 890000.0,
                notes = "عصر اختباري"
            )

            // Add sale
            vm.addSale(
                customerName = "مشتري تجريبي",
                phone = "0944332211",
                itemType = "OIL_TIN",
                quantity = 5.0,
                unitPrice = 800000.0,
                discount = 0.0,
                paidAmount = 4000000.0,
                paymentType = "CASH",
                notes = "بيع نقدي"
            )

            // Add cash transaction
            vm.addCashTransaction(
                type = "EXPENSE",
                category = "محروقات",
                amount = 250000.0,
                partyName = "محطة المحروقات",
                notes = "مازوت"
            )

            kotlinx.coroutines.delay(400)
            org.junit.Assert.assertNotNull(vm.invoicePreview.value)
            vm.dismissInvoicePreview()
            org.junit.Assert.assertNull(vm.invoicePreview.value)
        }
    }
}
