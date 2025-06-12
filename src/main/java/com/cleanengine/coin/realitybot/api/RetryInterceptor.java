package com.cleanengine.coin.realitybot.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
public class RetryInterceptor implements Interceptor {
    private final int maxRetries;
    private final long initialDelayMs;
    private final long maxDelay;

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        //서버 통신 일어나기 직전-후 가로채기
        Request request = chain.request();//가로챈거
        IOException lastException = null;
        long delay = initialDelayMs;
        int i;
        for (i = 0; i < maxRetries; i++) {
            log.debug("Retry 시도 {}/{}: delay={}ms", i + 1, maxRetries, delay);

            try{
                Response response = chain.proceed(request);//응답 진행
//                throw new IOException("강제 실패"); //잘 작동
                if (response.isSuccessful()) {
                    return response;
                }
                response.close();
            } catch (IOException e){
                lastException = e;
            }
            if (i < maxRetries) {
                try {
                    Thread.sleep(Math.min(delay, maxDelay));
                    delay *= 2; // 지연후 재실행
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw lastException != null ? lastException : new IOException("Interrupted during retry", e);
                }
            }
        }
        throw lastException != null ? lastException : new IOException("All retries failed");
    }
}
