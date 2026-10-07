package com.AcolheSP.bff.util;

import java.util.HashMap;
import java.util.Map;

public class DataSanitizer {
    
    // Limpa texto simples
    public static String limparTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        // Faz a limpeza dos dados
        return texto
            .replaceAll("<[^>]*>", "")  // Remove HTML
            .replace(";", "") // Remove ; 
            .replace("--", "") // Remove  --
            .replaceAll("['\"]", "")     // Remove aspas simples e duplas
            .trim();
    }

    // Limpa um Map (JSON genérico transformado pelo Spring)
    public static Map<String, Object> limparMap(Map<String, Object> dados) {
        Map<String, Object> limpo = new HashMap<>();

        // Passa por cada elemento do JSON limpando
        // Se for String limpa o texto, se for JSON limpa o texto escrito e se não for texto, só insere
        // no JSON limpo já
        for (Map.Entry<String, Object> entry : dados.entrySet()) {
            Object valor = entry.getValue();

            if (valor instanceof String) {
                limpo.put(entry.getKey(), limparTexto((String) valor));
            } else if (valor instanceof Map) {
                limpo.put(entry.getKey(), limparMap((Map<String, Object>) valor));
            } else {
                limpo.put(entry.getKey(), valor);
            }
        }
        return limpo;
    }
}
