package com.azen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppTest {
    @Test
    void returnsGreeting() {
        assertEquals("Hello from Azen", App.greeting());
    }
}