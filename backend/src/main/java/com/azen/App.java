package com.azen;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        System.out.println("Azen backend is running on Java " + Runtime.version().feature());
    }

    public static String greeting() {
        return "Hello from Azen";
    }
}