package com.example.memory.ui.components

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
        TextToSpeech(context) { /* you can handle onInit if you need to */ }
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
    sourceLang: String
) {
    // inner helper unchanged
    suspend fun TextToSpeech.awaitSpeak(text: String, langTag: String) {
        withContext(Dispatchers.Main) {
            val done = CompletableDeferred<Unit>()
            language = Locale.forLanguageTag(langTag)
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
    awaitSpeak(card.text, sourceLang)

    kotlinx.coroutines.delay(1000)

    // 2) translations always in English
    card.translations.forEach { translationText ->
        awaitSpeak(translationText, "en-US")
    }
}