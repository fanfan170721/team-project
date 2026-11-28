package com.wordtrail.usecase.port;

import java.io.IOException;
import com.wordtrail.entity.PronunciationClip;

public interface PronunciationClient {
    PronunciationClip fetch(String word, String preferredAccent) throws IOException, InterruptedException;
}
