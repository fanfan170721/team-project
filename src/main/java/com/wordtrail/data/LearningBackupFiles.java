package com.wordtrail.data;

import com.wordtrail.usecase.AppException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;

/** Bounded, checksummed JSON files. Exports never overwrite an existing file. */
final class LearningBackupFiles {
    static final int LIMIT = 16 * 1024 * 1024;
    private LearningBackupFiles() { }

    static String canonical(Object value) {
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            ArrayList<String> keys = new ArrayList<>(object.keySet());
            Collections.sort(keys);
            ArrayList<String> parts = new ArrayList<>();
            for (String key : keys) { parts.add(JSONObject.quote(key) + ":" + canonical(object.get(key))); }
            return "{" + String.join(",", parts) + "}";
        }
        if (value instanceof JSONArray) {
            ArrayList<String> parts = new ArrayList<>();
            for (Object item : (JSONArray) value) { parts.add(canonical(item)); }
            return "[" + String.join(",", parts) + "]";
        }
        return JSONObject.valueToString(value);
    }

    static String digest(byte[] bytes) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder value = new StringBuilder();
            for (byte item : hash) { value.append(String.format("%02x", item & 255)); }
            return value.toString();
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }

    static JSONObject envelope(JSONObject payload) {
        return new JSONObject().put("format", "WordTrailLearningBackup").put("version", 1)
                .put("createdAt", java.time.Instant.now().toString()).put("payload", payload)
                .put("sha256", digest(canonical(payload).getBytes(StandardCharsets.UTF_8)));
    }

    static byte[] read(Path file) throws IOException {
        if (!Files.isRegularFile(file) || Files.size(file) > LIMIT) {
            throw new IOException("Choose a regular JSON backup smaller than 16 MiB.");
        }
        try (InputStream input = Files.newInputStream(file)) {
            byte[] bytes = input.readNBytes(LIMIT + 1);
            if (bytes.length > LIMIT) { throw new IOException("The backup exceeds 16 MiB."); }
            return bytes;
        }
    }

    static JSONObject payload(byte[] bytes) {
        try {
            JSONObject document = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
            if (!document.getString("format").equals("WordTrailLearningBackup") || document.getInt("version") != 1) {
                throw new IllegalArgumentException("Unsupported backup format.");
            }
            JSONObject payload = document.getJSONObject("payload");
            validateNumbers(payload);
            if (!digest(canonical(payload).getBytes(StandardCharsets.UTF_8)).equals(document.getString("sha256"))) {
                throw new IllegalArgumentException("Backup checksum mismatch.");
            }
            return payload;
        } catch (RuntimeException | StackOverflowError invalid) {
            throw new AppException("Invalid, damaged or unsupported learning backup. Nothing was changed.", invalid);
        }
    }

    private static void validateNumbers(Object value) {
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            for (String key : object.keySet()) { validateNumbers(object.get(key)); }
        } else if (value instanceof JSONArray) {
            for (Object item : (JSONArray) value) { validateNumbers(item); }
        } else if (value instanceof Number) {
            if (!(value instanceof Integer) || ((Integer) value) < 0) {
                throw new IllegalArgumentException("Invalid backup counter.");
            }
        }
    }

    static void writeNew(Path destination, JSONObject content) throws IOException {
        Path target = destination.toAbsolutePath().normalize();
        if (Files.exists(target)) { throw new IOException("That file already exists. Choose a new backup filename."); }
        byte[] bytes = content.toString(2).getBytes(StandardCharsets.UTF_8);
        if (bytes.length > LIMIT) { throw new IOException("The learning backup exceeds 16 MiB."); }
        Path temporary = Files.createTempFile(target.getParent(), ".wordtrail-backup-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) { channel.write(buffer); }
                channel.force(true);
            }
            // No REPLACE_EXISTING: even a destination created after the picker stays intact.
            Files.move(temporary, target);
        } finally { Files.deleteIfExists(temporary); }
    }
}
