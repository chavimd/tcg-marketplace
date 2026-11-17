package com.tcg.catalogservice.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class CartaCatalogoTests {

    @Test
    void storesMetadataWithoutSharingTheCallerMap() {
        var metadata = new java.util.HashMap<String, Object>();
        metadata.put("element", "water");

        CartaCatalogo carta = new CartaCatalogo(
                new JuegoTcg("Pokemon", null),
                "Squirtle",
                "BASE-007",
                "Base Set",
                "Common",
                "Water",
                "Blue",
                null,
                null,
                metadata);
        metadata.put("mutated", true);

        assertEquals(Map.of("element", "water"), carta.getMetadataExtra());
        assertNotSame(metadata, carta.getMetadataExtra());
    }
}
