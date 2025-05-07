package com.example.memory.ui.components

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.memory.model.Flashcard
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun rememberTts(): TextToSpeech {
    val context = LocalContext.current
    // 1) Create TTS with an empty listener
    val tts = remember {
        TextToSpeech(context) {

        }
    }
        // 2) Now that `tts` exists, apply your default rate immediately
        .also { it.setSpeechRate(0.7f) }

    DisposableEffect(tts) {
        onDispose { tts.shutdown() }
    }
    return tts
}


suspend fun TextToSpeech.readCard(
    card: Flashcard,
    sourceLang: String,
    preferredVoiceName: String? = null  // Add a parameter for exact voice name
) {
    // inner helper with specific voice selection
    suspend fun TextToSpeech.awaitSpeak(text: String, langTag: String, voiceName: String? = null) {
        withContext(Dispatchers.Main) {
            val done = CompletableDeferred<Unit>()

            // Set language first
            language = Locale.forLanguageTag(langTag)

            // Try to set specific voice if provided
            if (voiceName != null) {
                val voices = voices
                val desiredVoice = voices?.find { voice ->
                    voice.name == voiceName &&
                            voice.locale.toLanguageTag().startsWith(langTag.split("-")[0])
                }

                if (desiredVoice != null) {
                    voice = desiredVoice
                }
            }

            val id = System.currentTimeMillis().toString()
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String?) {
                    done.complete(Unit)
                }
                override fun onError(utteranceId: String?) {
                    done.complete(Unit)
                }
                override fun onStart(utteranceId: String?) {}
            })
            speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
            done.await()
        }
    }

    // 1) original text in dynamic sourceLang
    awaitSpeak(card.text, sourceLang, preferredVoiceName)

    kotlinx.coroutines.delay(1000)

    // 2) translations always in English
    card.translations.forEach { translationText ->
        awaitSpeak(translationText, "en-US", "en-us-x-iol-local")
    }
}