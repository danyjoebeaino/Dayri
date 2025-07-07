package com.danyjoe.lebanesemonateries

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

object TranslationManager {
    // Available languages
    val LANGUAGES = mapOf(
        "en" to "English",
        "fr" to "Français",
        "ar" to "العربية"
    )

    // Map of translations: language code -> string key -> translated value
    private val translationMap = mapOf(
        "en" to mapOf(
            // Common strings
            "app_name" to "Dayri",
            "email" to "Email",
            "password" to "Password",
            "login" to "Login",
            "sign_up" to "Sign Up",
            "forgot_password" to "Forgot Password?",
            "no_account_yet" to "Don't have an account yet?",
            "already_have_account" to "Already have an account?",

            // Welcome screen
            "welcome_title" to "Welcome to Lebanese Monasteries",
            "welcome_description" to "Explore the beautiful monasteries of Lebanon",
            "get_started" to "Get Started",

            // Auth strings
            "create_account" to "Create Account",
            "full_name" to "Full Name",
            "gender" to "Gender",
            "male" to "Male",
            "female" to "Female",
            "birthday" to "Birthday",
            "select_date" to "Select Date",
            "phone_number" to "Phone Number",
            "confirm_password" to "Confirm Password",
            "phone_number_hint" to "Enter phone number",
            "verification_code" to "Verification Code",
            "verification_description" to "Please enter the verification code sent to",
            "verify" to "Verify",
            "didnt_receive_code" to "Didn't receive code?",
            "resend_code" to "Resend Code",
            "resend_timer" to "Resend code in %d seconds",
            "email_verification" to "Email Verification",
            "email_verification_description" to "We've sent a verification link to",
            "password_required" to "Password is required",
            "invalid_password" to "Invalid password",
            "invalid_email" to "Email not found or invalid",
            "user_not_found" to "No account found with this email",


            // Main app strings
            "home" to "Home",
            "search" to "Search",
            "profile" to "Profile",
            "settings" to "Settings",
            "logout" to "Logout",
            "favorites" to "Favorites",

            // Monastery details
            "description" to "Description",
            "history" to "History",
            "year_founded" to "Year Founded",
            "location" to "Location",
            "get_directions" to "Get Directions",
            "share" to "Share",
            "add_to_favorites" to "Add to Favorites",
            "remove_from_favorites" to "Remove from Favorites",

            // Settings
            "theme" to "Theme",
            "light_theme" to "Light Theme",
            "dark_theme" to "Dark Theme",
            "system_theme" to "System Default",
            "notifications" to "Notifications",
            "enable_notifications" to "Enable Notifications",
            "new_monasteries" to "New Monasteries Added",
            "app_updates" to "App Updates",
            "language" to "Language",
            "account" to "Account",
            "delete_account" to "Delete Account",
            "save_settings" to "Save Settings",
            "language_updated" to "Language updated",
            "delete" to "Delete",
            "cancel" to "Cancel",
            "delete_account_confirm" to "Are you sure you want to delete your account? This action cannot be undone.",
            "account_deleted" to "Account deleted successfully",
            "settings_saved" to "Settings saved successfully",
            "please_login" to "Please login to access settings",
            "founded_year_format" to "Founded: %d",

            // Profile
            "personal_information" to "Personal Information",
            "favorite_monasteries" to "Favorite Monasteries",
            "no_favorites" to "You haven't added any favorite monasteries yet.",
            "edit_profile" to "Edit Profile",
            "save_changes" to "Save Changes",

            // Search
            "search_monasteries" to "Search monasteries",
            "recent_searches" to "Recent Searches",
            "clear_all" to "Clear All",
            "no_monasteries_found" to "No monasteries found",
            "name" to "Name",
            "before_1800" to "Before 1800",
            "after_1800" to "After 1800",

            //Forgot Password
            "forgot_password_description" to "Enter your email address and we will send you instructions to reset your password.",
            "reset_password" to "Reset Password",
            "email_required" to "Email is required",
            "reset_email_sent" to "Password reset email sent. Please check your inbox.",
            "reset_email_failed" to "Failed to send password reset email",

            // Feedback
            "feedback" to "Feedback",
            "feedback_hint" to "Tell us what you think about the app",
            "submit" to "Submit",
            "cancel" to "Cancel",
            "feedback_empty" to "Please enter your feedback",
            "feedback_submitted" to "Thank you for your feedback!",
            "feedback_error" to "Error submitting feedback",
            "please_wait" to "Please wait...",

            //Search
            "search_monasteries" to "Search Monasteries",
            "recent_searches" to "Recent Searches",
            "clear_all" to "Clear All",
            "no_monasteries_found" to "No Monasteries Found",
            "name" to "Name",
            "before_1800" to "Before 1800",
            "after_1800" to "After 1800",

            // Other
            "discover_lebanese_cultural_heritage" to "Discover Lebanese Cultural Heritage"


        ),

        "fr" to mapOf(
            // Common strings
            "app_name" to "Dayri",
            "email" to "E-mail",
            "password" to "Mot de passe",
            "login" to "Connexion",
            "sign_up" to "S'inscrire",
            "forgot_password" to "Mot de passe oublié ?",
            "no_account_yet" to "Pas encore de compte ?",
            "already_have_account" to "Vous avez déjà un compte ?",

            // Welcome screen
            "welcome_title" to "Bienvenue aux Monastères Libanais",
            "welcome_description" to "Explorez les beaux monastères du Liban",
            "get_started" to "Commencer",

            // Auth strings
            "create_account" to "Créer un Compte",
            "full_name" to "Nom Complet",
            "gender" to "Genre",
            "male" to "Homme",
            "female" to "Femme",
            "birthday" to "Date de Naissance",
            "select_date" to "Sélectionner une date",
            "phone_number" to "Numéro de Téléphone",
            "confirm_password" to "Confirmer le Mot de Passe",
            "phone_number_hint" to "Entrer le numéro de téléphone",
            "verification_code" to "Code de Vérification",
            "verification_description" to "Veuillez entrer le code de vérification envoyé à",
            "verify" to "Vérifier",
            "didnt_receive_code" to "Vous n'avez pas reçu de code ?",
            "resend_code" to "Renvoyer le Code",
            "resend_timer" to "Renvoyer le code dans %d secondes",
            "email_verification" to "Vérification d'E-mail",
            "email_verification_description" to "Nous avons envoyé un lien de vérification à",
            "password_required" to "Mot de passe requis",
            "invalid_password" to "Mot de passe incorrect",
            "invalid_email" to "Email introuvable ou invalide",
            "user_not_found" to "Aucun compte trouvé avec cet e-mail",


            // Main app strings
            "home" to "Accueil",
            "search" to "Rechercher",
            "profile" to "Profil",
            "settings" to "Paramètres",
            "logout" to "Déconnexion",
            "favorites" to "Favoris",

            // Monastery details
            "description" to "Description",
            "history" to "Histoire",
            "year_founded" to "Année de Fondation",
            "location" to "Emplacement",
            "get_directions" to "Obtenir l'Itinéraire",
            "share" to "Partager",
            "add_to_favorites" to "Ajouter aux Favoris",
            "remove_from_favorites" to "Retirer des Favoris",

            // Settings
            "theme" to "Thème",
            "light_theme" to "Thème Clair",
            "dark_theme" to "Thème Sombre",
            "system_theme" to "Thème Système",
            "notifications" to "Notifications",
            "enable_notifications" to "Activer les Notifications",
            "new_monasteries" to "Nouveaux Monastères Ajoutés",
            "app_updates" to "Mises à Jour de l'Application",
            "language" to "Langue",
            "account" to "Compte",
            "delete_account" to "Supprimer le Compte",
            "save_settings" to "Enregistrer les Paramètres",
            "language_updated" to "Langue mise à jour",
            "delete" to "Supprimer",
            "cancel" to "Annuler",
            "delete_account_confirm" to "Êtes-vous sûr de vouloir supprimer votre compte ? Cette action ne peut pas être annulée.",
            "account_deleted" to "Compte supprimé avec succès",
            "settings_saved" to "Paramètres enregistrés avec succès",
            "please_login" to "Veuillez vous connecter pour accéder aux paramètres",
            "founded_year_format" to "Fondé en: %d",

            // Profile
            "personal_information" to "Informations Personnelles",
            "favorite_monasteries" to "Monastères Favoris",
            "no_favorites" to "Vous n'avez pas encore ajouté de monastères favoris.",
            "edit_profile" to "Modifier le Profil",
            "save_changes" to "Enregistrer les Modifications",

            // Search
            "search_monasteries" to "Rechercher des monastères",
            "recent_searches" to "Recherches Récentes",
            "clear_all" to "Tout Effacer",
            "no_monasteries_found" to "Aucun monastère trouvé",
            "name" to "Nom",
            "before_1800" to "Avant 1800",
            "after_1800" to "Après 1800",

            //Forgot Password
            "forgot_password_description" to "Entrez votre adresse e-mail et nous vous enverrons des instructions pour réinitialiser votre mot de passe.",
            "reset_password" to "Réinitialiser le Mot de Passe",
            "email_required" to "L'e-mail est requis",
            "reset_email_sent" to "E-mail de réinitialisation de mot de passe envoyé. Veuillez vérifier votre boîte de réception.",
            "reset_email_failed" to "Échec de l'envoi de l'e-mail de réinitialisation du mot de passe",

            //Feedback
            "feedback" to "Commentaires",
            "feedback_hint" to "Dites-nous ce que vous pensez de l'application",
            "submit" to "Soumettre",
            "cancel" to "Annuler",
            "feedback_empty" to "Veuillez entrer vos commentaires",
            "feedback_submitted" to "Merci pour vos commentaires !",
            "feedback_error" to "Erreur lors de l'envoi des commentaires",
            "please_wait" to "Veuillez patienter...",


            //Search
            "search_monasteries" to "Rechercher des Monastères",
            "recent_searches" to "Recherches Récentes",
            "clear_all" to "Tout Effacer",
            "no_monasteries_found" to "Aucun Monastère Trouvé",
            "name" to "Nom",
            "before_1800" to "Avant 1800",
            "after_1800" to "Après 1800",


            // Other
            "discover_lebanese_cultural_heritage" to "Découvrez le Patrimoine Culturel Libanais"
        ),

        "ar" to mapOf(
            // Common strings
            "app_name" to "ديري",
            "email" to "البريد الإلكتروني",
            "password" to "كلمة المرور",
            "login" to "تسجيل الدخول",
            "sign_up" to "إنشاء حساب",
            "forgot_password" to "نسيت كلمة المرور؟",
            "no_account_yet" to "ليس لديك حساب بعد؟",
            "already_have_account" to "لديك حساب بالفعل؟",

            // Welcome screen
            "welcome_title" to "مرحبًا بك في الأديرة اللبنانية",
            "welcome_description" to "استكشف الأديرة الجميلة في لبنان",
            "get_started" to "ابدأ الآن",

            // Auth strings
            "create_account" to "إنشاء حساب",
            "full_name" to "الاسم الكامل",
            "gender" to "الجنس",
            "male" to "ذكر",
            "female" to "أنثى",
            "birthday" to "تاريخ الميلاد",
            "select_date" to "اختر تاريخًا",
            "phone_number" to "رقم الهاتف",
            "confirm_password" to "تأكيد كلمة المرور",
            "phone_number_hint" to "أدخل رقم الهاتف",
            "verification_code" to "رمز التحقق",
            "verification_description" to "الرجاء إدخال رمز التحقق المرسل إلى",
            "verify" to "تحقق",
            "didnt_receive_code" to "لم تتلق الرمز؟",
            "resend_code" to "إعادة إرسال الرمز",
            "resend_timer" to "إعادة إرسال الرمز خلال %d ثانية",
            "email_verification" to "التحقق من البريد الإلكتروني",
            "email_verification_description" to "لقد أرسلنا رابط التحقق إلى",
            "password_required" to "كلمة المرور مطلوبة",
            "invalid_password" to "كلمة مرور غير صحيحة",
            "invalid_email" to "البريد الإلكتروني غير موجود أو غير صالح",
            "user_not_found" to "لم يتم العثور على حساب بهذا البريد الإلكتروني",

            // Main app strings
            "home" to "الرئيسية",
            "search" to "بحث",
            "profile" to "الملف الشخصي",
            "settings" to "الإعدادات",
            "logout" to "تسجيل الخروج",
            "favorites" to "المفضلة",

            // Monastery details
            "description" to "الوصف",
            "history" to "التاريخ",
            "year_founded" to "سنة التأسيس",
            "location" to "الموقع",
            "get_directions" to "الحصول على الاتجاهات",
            "share" to "مشاركة",
            "add_to_favorites" to "إضافة إلى المفضلة",
            "remove_from_favorites" to "إزالة من المفضلة",

            // Settings
            "theme" to "المظهر",
            "light_theme" to "المظهر الفاتح",
            "dark_theme" to "المظهر الداكن",
            "system_theme" to "مظهر النظام",
            "notifications" to "الإشعارات",
            "enable_notifications" to "تمكين الإشعارات",
            "new_monasteries" to "أديرة جديدة مضافة",
            "app_updates" to "تحديثات التطبيق",
            "language" to "اللغة",
            "account" to "الحساب",
            "delete_account" to "حذف الحساب",
            "save_settings" to "حفظ الإعدادات",
            "language_updated" to "تم تحديث اللغة",
            "delete" to "حذف",
            "cancel" to "إلغاء",
            "delete_account_confirm" to "هل أنت متأكد أنك تريد حذف حسابك؟ لا يمكن التراجع عن هذا الإجراء.",
            "account_deleted" to "تم حذف الحساب بنجاح",
            "settings_saved" to "تم حفظ الإعدادات بنجاح",
            "please_login" to "الرجاء تسجيل الدخول للوصول إلى الإعدادات",
            "founded_year_format" to "تأسس في: %d",

            // Profile
            "personal_information" to "المعلومات الشخصية",
            "favorite_monasteries" to "الأديرة المفضلة",
            "no_favorites" to "لم تضف أي أديرة مفضلة بعد.",
            "edit_profile" to "تعديل الملف الشخصي",
            "save_changes" to "حفظ التغييرات",

            // Search
            "search_monasteries" to "البحث عن الأديرة",
            "recent_searches" to "عمليات البحث الأخيرة",
            "clear_all" to "مسح الكل",
            "no_monasteries_found" to "لم يتم العثور على أديرة",
            "name" to "الاسم",
            "before_1800" to "قبل 1800",
            "after_1800" to "بعد 1800",

            //Forgot Password
            "forgot_password_description" to "أدخل عنوان بريدك الإلكتروني وسنرسل لك تعليمات لإعادة تعيين كلمة المرور.",
            "reset_password" to "إعادة تعيين كلمة المرور",
            "email_required" to "البريد الإلكتروني مطلوب",
            "reset_email_sent" to "تم إرسال بريد إلكتروني لإعادة تعيين كلمة المرور. يرجى التحقق من صندوق الوارد الخاص بك.",
            "reset_email_failed" to "فشل في إرسال البريد الإلكتروني لإعادة تعيين كلمة المرور",

            // Feedback
            "feedback" to "تعليقات",
            "feedback_hint" to "أخبرنا برأيك في التطبيق",
            "submit" to "إرسال",
            "cancel" to "إلغاء",
            "feedback_empty" to "الرجاء إدخال تعليقاتك",
            "feedback_submitted" to "شكرا لملاحظاتك!",
            "feedback_error" to "خطأ في تقديم التعليقات",
            "please_wait" to "يرجى الانتظار...",


            // Other
            "discover_lebanese_cultural_heritage" to "اكتشف التراث الثقافي اللبناني"
        )
    )

    // Cache for monastery translations loaded from Firestore
    private val monasteryTranslationsCache = ConcurrentHashMap<String, Map<String, Map<String, String>>>()

    // Get the translation in the current language
    fun getString(key: String, context: Context): String {
        val language = LocaleHelper.getLanguage(context)
        return translationMap[language]?.get(key) ?: translationMap["en"]?.get(key) ?: key
    }

    // Extension functions to set text on common UI elements
    fun TextView.setTranslatedText(key: String, context: Context) {
        this.text = getString(key, context)
    }

    fun Button.setTranslatedText(key: String, context: Context) {
        this.text = getString(key, context)
    }

    fun TextInputLayout.setTranslatedHint(key: String, context: Context) {
        this.hint = getString(key, context)
    }

    fun Toolbar.setTranslatedTitle(key: String, context: Context) {
        this.title = getString(key, context)
    }

    // Function to format a translated string with arguments
    fun getFormattedString(key: String, context: Context, vararg args: Any): String {
        val format = getString(key, context)
        return String.format(format, *args)
    }

    // Load translations from Firestore
    suspend fun loadMonasteryTranslations() {
        try {
            val db = FirebaseFirestore.getInstance()
            val translationsSnapshot = db.collection("monasteryTranslations").get().await()

            val translations = mutableMapOf<String, MutableMap<String, MutableMap<String, String>>>()

            for (document in translationsSnapshot.documents) {
                val monasteryId = document.id
                val monasteryTranslations = mutableMapOf<String, MutableMap<String, String>>()

                val data = document.data ?: continue

                // Process each language translation
                for ((language, translationData) in data) {
                    if (translationData is Map<*, *>) {
                        val fieldTranslations = mutableMapOf<String, String>()

                        for ((field, translation) in translationData) {
                            if (field is String && translation is String) {
                                fieldTranslations[field] = translation
                            }
                        }

                        if (fieldTranslations.isNotEmpty()) {
                            monasteryTranslations[language] = fieldTranslations
                        }
                    }
                }

                if (monasteryTranslations.isNotEmpty()) {
                    translations[monasteryId] = monasteryTranslations
                }
            }

            // Cache translations
            monasteryTranslationsCache.clear()
            monasteryTranslationsCache.putAll(translations)

        } catch (e: Exception) {
            Log.e("TranslationManager", "Error loading monastery translations: ${e.message}")
        }
    }

    // Get translated monastery field
    fun getMonasteryTranslation(monasteryId: String, field: String, originalText: String, context: Context): String {
        val language = LocaleHelper.getLanguage(context)
        if (language == "en") return originalText // English is base language

        return monasteryTranslationsCache[monasteryId]?.get(language)?.get(field) ?: originalText
    }
}