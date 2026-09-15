package com.moneymate.app.core.localization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Lightweight runtime localization for the existing Compose UI.
 * New UI text should always pass through tr(). Bengali is a first-class app language.
 */
object AppLanguageRuntime { var language by mutableStateOf("en") }

private val bn = mapOf(
    "Home" to "হোম", "Activity" to "অ্যাক্টিভিটি", "Insights" to "ইনসাইটস", "Profile" to "প্রোফাইল",
    "Available Balance" to "উপলভ্য ব্যালেন্স", "Total Income" to "মোট আয়", "Total Expenses" to "মোট খরচ", "Savings" to "সঞ্চয়",
    "Add" to "যোগ করুন", "Transfer" to "ট্রান্সফার", "Budget" to "বাজেট", "Goals" to "লক্ষ্য",
    "Financial Health" to "আর্থিক স্বাস্থ্য", "Monthly Overview" to "মাসিক সারসংক্ষেপ", "Details" to "বিস্তারিত",
    "Good standing" to "ভালো অবস্থায়", "Updated just now" to "এইমাত্র আপডেট হয়েছে", "No activity" to "কোনো কার্যক্রম নেই",
    "Accounts" to "অ্যাকাউন্টসমূহ", "Add Account" to "অ্যাকাউন্ট যোগ করুন", "Edit" to "এডিট", "Delete" to "ডিলিট",
    "Add Income" to "আয় যোগ করুন", "Add Expense" to "খরচ যোগ করুন", "Amount" to "পরিমাণ", "Category" to "ক্যাটাগরি",
    "Account" to "অ্যাকাউন্ট", "Payment Method" to "পেমেন্ট মেথড", "Notes" to "নোট", "Save" to "সেভ করুন",
    "Cancel" to "বাতিল", "Confirm" to "নিশ্চিত করুন", "Date" to "তারিখ", "Income" to "আয়", "Expense" to "খরচ",
    "Transactions" to "লেনদেন", "Search" to "খুঁজুন", "Filters" to "ফিল্টার", "All" to "সব",
    "Settings" to "সেটিংস", "General" to "সাধারণ", "Language" to "ভাষা", "Currency" to "কারেন্সি", "Appearance" to "অ্যাপিয়ারেন্স",
    "Security" to "নিরাপত্তা", "App Lock" to "অ্যাপ লক", "Backup" to "ব্যাকআপ", "Restore" to "রিস্টোর", "Export Data" to "ডাটা এক্সপোর্ট",
    "Import Data" to "ডাটা ইমপোর্ট", "Cloud Sync" to "ক্লাউড সিঙ্ক", "FAQ" to "সাধারণ প্রশ্ন", "Contact Support" to "সাপোর্টে যোগাযোগ",
    "Privacy" to "গোপনীয়তা", "Terms of Service" to "সেবার শর্তাবলি", "About MoneyMate" to "MoneyMate সম্পর্কে",
    "Premium" to "প্রিমিয়াম", "MoneyMate Premium" to "MoneyMate প্রিমিয়াম", "Premium Themes" to "প্রিমিয়াম থিম",
    "Full Name" to "পূর্ণ নাম", "Email Address" to "ইমেইল ঠিকানা", "Password" to "পাসওয়ার্ড", "Create Account" to "অ্যাকাউন্ট তৈরি করুন",
    "Sign In" to "সাইন ইন", "Forgot Password?" to "পাসওয়ার্ড ভুলে গেছেন?", "Send Reset Link" to "রিসেট লিংক পাঠান",
    "Back to Sign In" to "সাইন ইন-এ ফিরে যান", "Change Password" to "পাসওয়ার্ড পরিবর্তন", "Current password" to "বর্তমান পাসওয়ার্ড",
    "New password" to "নতুন পাসওয়ার্ড", "Confirm PIN" to "PIN নিশ্চিত করুন", "Incorrect PIN" to "ভুল PIN",
    "MoneyMate Locked" to "MoneyMate লক করা আছে", "Enter your PIN to continue" to "চালিয়ে যেতে PIN দিন",
    "Notifications" to "নোটিফিকেশন", "Mark all as read" to "সব পড়া হয়েছে হিসেবে চিহ্নিত করুন",
    "Export format" to "এক্সপোর্ট ফরম্যাট", "Date Range" to "তারিখের পরিসর", "Choose File" to "ফাইল নির্বাচন করুন",
    "Delete transaction?" to "লেনদেনটি ডিলিট করবেন?", "Delete Account Permanently" to "অ্যাকাউন্ট স্থায়ীভাবে ডিলিট করুন",
    "Message" to "বার্তা", "Message Sent" to "বার্তা পাঠানো হয়েছে", "Dark mode" to "ডার্ক মোড",
    "Selected" to "নির্বাচিত", "Currency" to "কারেন্সি", "Display currency; existing amounts are not converted" to "প্রদর্শনের কারেন্সি; আগের পরিমাণ স্বয়ংক্রিয়ভাবে রূপান্তর হবে না",
    "Choose Your Plan" to "আপনার প্ল্যান বেছে নিন", "Premium Active" to "প্রিমিয়াম সক্রিয়",
    "Account & Sync" to "অ্যাকাউন্ট ও সিঙ্ক", "Backup & Restore" to "ব্যাকআপ ও রিস্টোর", "Troubleshooting" to "সমস্যা সমাধান",
    "Getting Started" to "শুরু করুন", "Managing Transactions" to "লেনদেন ব্যবস্থাপনা", "Budgets & Goals" to "বাজেট ও লক্ষ্য",
    "Premium & Billing" to "প্রিমিয়াম ও বিলিং", "Help topics and frequently asked questions" to "সহায়তা ও সাধারণ প্রশ্নসমূহ"
)

fun tr(text: String): String = if (AppLanguageRuntime.language == "bn") bn[text] ?: text else text
