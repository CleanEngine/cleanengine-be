package com.cleanengine.coin.order.presentation;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.order.application.OrderCommand;
import com.cleanengine.coin.order.application.OrderInfo;
import com.cleanengine.coin.order.application.OrderService;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto.CreateOrder>> createOrder(
            @RequestBody @Valid OrderRequestDto.CreateOrderRequest createOrderRequest) {
        CustomOAuth2User user = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer userId = user.getUserId();

        LocalDateTime createdAt = LocalDateTime.now();
        OrderCommand.CreateOrder createOrderCommand = createOrderRequest.toOrderCommand(userId, createdAt);
        OrderInfo<?> orderInfo = orderService.createOrder(createOrderCommand);

        return ApiResponse.success(OrderResponseDto.CreateOrder.from(orderInfo), HttpStatus.CREATED)
                .toResponseEntity();
    }
}
