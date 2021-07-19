package me.brennan.divvy.http;

import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class JsonBody extends RequestBody {
    private final JsonObject jsonObject;

    public JsonBody(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public long contentLength() throws IOException {
        return jsonObject.toString().length();
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse("application/json; charset=utf-8");
    }

    @Override
    public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
        final byte[] bytes = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        bufferedSink.write(bytes, 0, bytes.length);
    }
}
