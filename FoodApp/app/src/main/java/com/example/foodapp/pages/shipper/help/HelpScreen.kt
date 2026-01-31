package com.example.foodapp.pages.shipper.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun HelpScreen() {
    val categories = listOf(
        HelpCategory(
            "1",
            "rocket",
            stringResource(R.string.shipper_help_cat_getting_started),
            stringResource(R.string.shipper_help_cat_getting_started_desc),
            emptyList()
        ),
        HelpCategory(
            "2",
            "package",
            stringResource(R.string.shipper_help_cat_orders),
            stringResource(R.string.shipper_help_cat_orders_desc),
            emptyList()
        ),
        HelpCategory(
            "3",
            "wallet",
            stringResource(R.string.shipper_help_cat_earnings),
            stringResource(R.string.shipper_help_cat_earnings_desc),
            emptyList()
        ),
        HelpCategory(
            "4",
            "settings",
            stringResource(R.string.shipper_help_cat_account),
            stringResource(R.string.shipper_help_cat_account_desc),
            emptyList()
        ),
        HelpCategory(
            "5",
            "help",
            stringResource(R.string.shipper_help_cat_other),
            stringResource(R.string.shipper_help_cat_other_desc),
            emptyList()
        )
    )

    val faqs = listOf(
        FAQ(
            stringResource(R.string.shipper_help_faq1_q),
            stringResource(R.string.shipper_help_faq1_a)
        ),
        FAQ(
            stringResource(R.string.shipper_help_faq2_q),
            stringResource(R.string.shipper_help_faq2_a)
        ),
        FAQ(
            stringResource(R.string.shipper_help_faq3_q),
            stringResource(R.string.shipper_help_faq3_a)
        ),
        FAQ(
            stringResource(R.string.shipper_help_faq4_q),
            stringResource(R.string.shipper_help_faq4_a)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ContactSupportCard()

            Text(
                text = stringResource(R.string.shipper_help_categories),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ShipperColors.TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            categories.forEach { category ->
                HelpCategoryCard(category)
            }

            Text(
                text = stringResource(R.string.shipper_help_faq),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ShipperColors.TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            faqs.forEach { faq ->
                FAQCard(faq)
            }
        }
    }
}
