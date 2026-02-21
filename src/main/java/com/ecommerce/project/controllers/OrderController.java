package com.ecommerce.project.controllers;

import com.ecommerce.project.dto.OrderDTO;
import com.ecommerce.project.dto.OrderItemDTO;
import com.ecommerce.project.dto.OrderRequestDTO;
import com.ecommerce.project.services.OrderService;
import com.ecommerce.project.utils.AuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod,
                                                  @RequestBody OrderRequestDTO orderRequestDTO){
        String emailId = authUtil.loggedInEmail();

        OrderDTO order = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
                );

        return new ResponseEntity<OrderDTO>(order, HttpStatus.CREATED);
    }

}
