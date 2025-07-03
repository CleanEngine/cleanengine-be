package com.cleanengine.coin.base;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.configuration.JacksonConfig;
import com.cleanengine.coin.configuration.TimeConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * URL 패턴, HTTP 메서드 매칭 검증
 * JSON 직렬화, 역직렬화 검증(DTO 테스트로 넘길수 있음)
 * 사실 다른 유형의 테스트들 중 가장 덜 중요한 느낌. 시간 부족하다면 선택과 집중을..
 */
@ActiveProfiles("dev")
@Import({JacksonConfig.class, TimeConfig.class})
public abstract class ControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(
                get(url)
                        .contentType("application/json")
                        .accept("application/json")
                        .with(csrf()));
    }

    protected <T> ResultActions performPost(String url, T requestDto) throws Exception {
        return mockMvc.perform(
                post(url)
                        .contentType("application/json")
                        .accept("application/json")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()));
    }

    protected <T> T convertAs(String jsonString, Class<T> clazz) throws Exception {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, clazz);

        ApiResponse<T> apiResponse = objectMapper.readValue(jsonString, javaType);
        return apiResponse.data();
    }
}
