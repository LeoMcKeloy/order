package lev.pyryanov.microservice.order.controller;

import lev.pyryanov.microservice.order.domain.Order;
import lev.pyryanov.microservice.order.repository.OrderRepository;
import lev.pyryanov.microservice.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping ("/api/v1")
@Slf4j
public class OrderResource {

    private static final String ENTITY_NAME = "order";

    @Value ("${spring.application.name}")
    private String applicationName;

    private final OrderRepository orderRepository;

    private final OrderService orderService;

    public OrderResource(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @PostMapping ("/orders")
    @Transactional
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) throws URISyntaxException {
        log.debug("REST request to save Order : {}", order);
        if (order.getId() != null) {
            throw new ResponseStatusException (HttpStatus.CONFLICT, "A new order cannot already have an ID");
        }
        final Order result = orderRepository.save(order);
        orderService.createOrder(result);

        HttpHeaders headers = new HttpHeaders();
        String message = String.format("A new %s is created with identifier %s", ENTITY_NAME, result.getId());
        headers.add("X-" + applicationName + "-alert", message);
        headers.add("X-" + applicationName + "-params", result.getId().toString());
        return ResponseEntity.created(new URI ("/api/orders/" + result.getId())).headers(headers).body(result);
    }

}
