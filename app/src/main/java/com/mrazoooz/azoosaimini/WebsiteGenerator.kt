package com.mrazoooz.azoosaimini

/**
 * Generates a responsive starter website from a natural-language brief.
 *
 * This is a deterministic Kotlin generator guided by text features. It does not
 * claim to be a general-purpose LLM and does not call an external API.
 */
object WebsiteGenerator {
    data class GeneratedSite(val title: String, val html: String, val sections: List<String>)

    fun generate(prompt: String): GeneratedSite {
        val p = normalize(prompt)
        val title = when {
            has(p, "متجر", "تسوق", "منتجات", "بيع") -> "متجري الإلكتروني"
            has(p, "مطعم", "مقهى", "قهوه", "وجبات") -> "مطعمي"
            has(p, "مدرسه", "تعليم", "دوره", "تعلم") -> "منصة التعلم"
            has(p, "شخصي", "اعمالي", "معرض", "portfolio") -> "معرض أعمالي"
            has(p, "شركة", "خدمات", "مؤسسه") -> "موقع الشركة"
            else -> "موقعي الجديد"
        }
        val dark = has(p, "داكن", "مظلم", "اسود", "dark")
        val primary = when {
            has(p, "اخضر", "خضراء") -> "#16845B"
            has(p, "بنفسجي", "ارجواني") -> "#7956C8"
            has(p, "برتقالي") -> "#E87932"
            has(p, "احمر", "حمراء") -> "#D94B55"
            else -> "#4263EB"
        }
        val sections = buildList {
            add("الرئيسية")
            if (has(p, "منتجات", "متجر", "تسوق", "خدمات")) add("الخدمات والمنتجات")
            if (has(p, "اسعار", "السعر", "اشتراك", "باقات")) add("الأسعار")
            if (has(p, "من نحن", "نبذه", "عن الشركة")) add("من نحن")
            if (has(p, "تواصل", "اتصال", "رساله")) add("تواصل معنا")
            if (has(p, "تسجيل", "دخول", "حساب")) add("تسجيل الدخول")
        }.distinct()
        val background = if (dark) "#101827" else "#F4F6FB"
        val surface = if (dark) "#1D2939" else "#FFFFFF"
        val foreground = if (dark) "#F8FAFC" else "#172033"
        val muted = if (dark) "#B8C2D3" else "#667085"
        val safeTitle = escape(title)
        val sectionHtml = sections.drop(1).joinToString("\n") { section ->
            val safe = escape(section)
            "<section class='card'><h2>$safe</h2><p>أضف المحتوى الخاص بقسم $safe هنا.</p></section>"
        }
        val buttonLabel = if (has(p, "شراء", "متجر", "منتجات")) "تسوق الآن" else "ابدأ الآن"
        val html = """
<!doctype html>
<html lang="ar" dir="rtl">
<head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>$safeTitle</title>
<style>
*{box-sizing:border-box}body{margin:0;background:$background;color:$foreground;font-family:Arial,sans-serif;line-height:1.7}
nav{display:flex;justify-content:space-between;align-items:center;gap:12px;padding:18px 6%;background:$surface;position:sticky;top:0}
.brand{font-weight:800;color:$primary;font-size:20px}nav a{color:$foreground;text-decoration:none;margin:0 7px;font-size:13px}
main{max-width:1000px;margin:auto;padding:28px 18px}.hero{padding:54px 24px;text-align:center;background:$surface;border-radius:24px;box-shadow:0 12px 35px #00000012}
h1{font-size:clamp(28px,6vw,46px);margin:0 0 12px}.hero p,.card p{color:$muted}
button{border:0;border-radius:12px;padding:13px 24px;background:$primary;color:white;font-weight:bold;cursor:pointer}
.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:14px;margin-top:18px}
.card{background:$surface;border-radius:18px;padding:22px;margin-top:16px;box-shadow:0 8px 24px #0000000c}
input{display:block;width:100%;padding:12px;margin:9px 0;border:1px solid #98A2B3;border-radius:10px}
footer{text-align:center;color:$muted;padding:28px}
@media(max-width:520px){nav{align-items:flex-start;flex-direction:column}nav a{margin:0 0 0 8px}}
</style></head>
<body>
<nav><div class="brand">$safeTitle</div><div>${sections.joinToString("") { "<a href='#top'>${escape(it)}</a>" }}</div></nav>
<main id="top"><section class="hero"><h1>$safeTitle</h1><p>موقع تم إنشاؤه من وصفك باستخدام محرك Azooz AI المكتوب بلغة Kotlin.</p><button onclick="document.getElementById('notice').textContent='أهلاً بك! تم تنفيذ الزر.'">$buttonLabel</button><p id="notice" aria-live="polite"></p></section>
<div class="grid">$sectionHtml</div></main>
<footer>تم الإنشاء بواسطة Azooz AI</footer>
</body></html>
        """.trimIndent()
        return GeneratedSite(title, html, sections)
    }

    private fun normalize(value: String) = value.lowercase()
        .replace(Regex("[أإآٱ]"), "ا")
        .replace("ة", "ه").replace("ى", "ي")
        .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")

    private fun has(text: String, vararg words: String) = words.any { normalize(it) in text }
    private fun escape(value: String) = value.replace("&", "&amp;")
        .replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&#39;")
}