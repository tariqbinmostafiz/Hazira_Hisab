package com.example.domain.ai

import com.example.BuildConfig
import com.example.domain.model.CurrencyFormatter
import com.example.domain.model.FinancialSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiFinancialAdvisor {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeLedger(summary: FinancialSummary): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            val field = try {
                BuildConfig::class.java.getField("GEMINI_API_KEY")
            } catch (_: NoSuchFieldException) {
                try {
                    BuildConfig::class.java.getField("geminiApiKey")
                } catch (_: NoSuchFieldException) {
                    null
                }
            }
            (field?.get(null) as? String) ?: ""
        } catch (_: Throwable) {
            ""
        }

        // Offline smart financial advice if API key is not provided or network is not available
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalSummaryAdvice(summary)
        }

        try {
            val prompt = """
                তুমি একজন সহমর্মী, বাস্তববাদী আর্থিক হিসাব সহকারী। 
                একজন দৈনিক মজুর / শ্রমিকের আর্থিক হিসাব নিচে দেওয়া হলো:
                - মোট কাজ: ${CurrencyFormatter.formatAttendance(summary.totalAttendanceUnits)}
                - মোট প্রাপ্য আয়: ${CurrencyFormatter.formatTaka(summary.totalEarnedPoisha)}
                - মালিকের কাছ থেকে পাওয়া টাকা: ${CurrencyFormatter.formatTaka(summary.totalReceivedPoisha)}
                - বাকি টাকা: ${if (summary.isAdvance) "মালিকের কাছে অগ্রিম নেওয়া: " + CurrencyFormatter.formatTaka(summary.advancePoisha) else "মালিকের কাছে এখনও পাওনা বাকি: " + CurrencyFormatter.formatTaka(summary.unpaidDuesPoisha)}
                - মোট খরচ: ${CurrencyFormatter.formatTaka(summary.totalExpensePoisha)} (খাবার খরচ: ${CurrencyFormatter.formatTaka(summary.foodExpensePoisha)}, অন্যান্য খরচ: ${CurrencyFormatter.formatTaka(summary.otherExpensePoisha)})
                - বর্তমানে হাতে থাকা নগদ টাকা: ${CurrencyFormatter.formatTaka(summary.cashInHandPoisha)}
                
                অনুগ্রহ করে খুব সহজ, সাবলীল বাংলায় ৩-৪টি সংক্ষিপ্ত পয়েন্টে শ্রমিকের জন্য দরকারী বিশ্লেষণ ও পরামর্শ দাও:
                ১. কাজের তুলনায় আয় ও পাওনা আদায়ের বর্তমান অবস্থা
                ২. খরচের ভারসাম্য (বিশেষ করে খাবার খরচ ও অন্যান্য খরচ)
                ৩. বকেয়া টাকা তোলা বা সঞ্চয়ের বাস্তবমুখী পরামর্শ
                কোনো জটিল পরিভাষা ব্যবহার করবে না।
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                val genConfig = JSONObject().apply {
                    val thinkingConfig = JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    }
                    put("thinkingConfig", thinkingConfig)
                }
                put("generationConfig", genConfig)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext generateLocalSummaryAdvice(summary)
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text")
                    if (text.isNotBlank()) {
                        return@withContext text
                    }
                }
            }
            generateLocalSummaryAdvice(summary)
        } catch (_: Exception) {
            generateLocalSummaryAdvice(summary)
        }
    }

    private fun generateLocalSummaryAdvice(summary: FinancialSummary): String {
        val sb = StringBuilder()
        sb.append("📊 আপনার হিসাবের সারসংক্ষেপ:\n\n")

        // Point 1: Dues vs Received
        if (summary.isAdvance) {
            sb.append("• আপনি কাজের পরিমাণের চেয়ে ${CurrencyFormatter.formatTaka(summary.advancePoisha)} অগ্রিম নিয়েছেন। পরবর্তী কাজগুলোর সাথে এটি সমন্বয় করা হবে।\n")
        } else if (summary.unpaidDuesPoisha > 0L) {
            sb.append("• মালিকের কাছে এখনও আপনার বকেয়া পাওনা রয়েছে ${CurrencyFormatter.formatTaka(summary.unpaidDuesPoisha)}। সময়মত এই টাকা তুলে নেওয়া ভালো।\n")
        } else {
            sb.append("• আপনার কাজের পুরো টাকা পরিশোধিত হয়েছে, কোনো বকেয়া বাকি নেই।\n")
        }

        // Point 2: Cash in hand
        if (summary.cashInHandPoisha >= 0) {
            sb.append("• হাতে বর্তমানে নগদ অবশিষ্ট আছে ${CurrencyFormatter.formatTaka(summary.cashInHandPoisha)}।\n")
        } else {
            sb.append("• খরচের তুলনায় হাতে পাওয়া টাকা কম থাকায় নগদ ঘাটতি দেখা যাচ্ছে। হিসাব পুনরায় যাচাই করুন।\n")
        }

        // Point 3: Expense ratio
        if (summary.totalExpensePoisha > 0) {
            val foodPct = (summary.foodExpensePoisha * 100) / summary.totalExpensePoisha.coerceAtLeast(1)
            sb.append("• মোট খরচের মধ্যে খাবার খরচ প্রায় $foodPct% (${CurrencyFormatter.formatTaka(summary.foodExpensePoisha)})। অপচয় রোধ করে কিছু টাকা সঞ্চয় করার চেষ্টা করুন।")
        } else {
            sb.append("• কোনো খরচ এখনও অন্তর্ভুক্ত করা হয়নি। প্রতিদিনের বাস্তব খরচ লিখে রাখলে সঠিক হিসাব পাবেন।")
        }

        return sb.toString()
    }
}
