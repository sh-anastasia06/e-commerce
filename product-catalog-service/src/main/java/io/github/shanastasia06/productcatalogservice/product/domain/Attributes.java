package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * values can store:
 * - boolean
 * - string
 * - numbers: integers or float-pointing values
 */
public record Attributes(Map<String, Object> values) {
     static final int MAX_ENTRIES = 50;
     static final int MAX_KEY_LENGTH = 50;
     static final int MAX_STRING_VALUE_LENGTH = 50;

     private static final Attributes EMPTY = new Attributes(null);

     public Attributes {
         if (values == null) {
             values = Map.of();
         } else {
             if (values.size() > MAX_ENTRIES) {
                 throw new DomainValidationException("Attributes must not contain more than " + MAX_ENTRIES + " entries");
             }
             // keeps insertion order, unmodifiable
             Map<String, Object> copy = new LinkedHashMap<>();
             for (Map.Entry<String, Object> entry : values.entrySet()) {
                 String key = normalizeKey(entry.getKey());
                 validateKey(key);
                 validateValue(key, entry.getValue());
                 copy.put(key, entry.getValue());
             }
             values = Collections.unmodifiableMap(copy);
         }
     }

    private static String normalizeKey(String key) {
         return key == null ? null : key.strip();
    }

    public static Attributes empty() {
         return EMPTY;
    }

    private static void validateKey(String key) {
         if (key == null) {
             throw new DomainValidationException("Attribute key must not be null");
         }
         if (key.isBlank()) {
             throw new DomainValidationException("Attribute key must not be blank");
         }
         if (key.length() > MAX_KEY_LENGTH) {
             throw new DomainValidationException(
                     "Attribute '" + key + "' key must not exceed " + MAX_KEY_LENGTH + " characters");
         }
         // MongoDB restriction
         if (key.indexOf('.') >= 0) {
             throw new DomainValidationException("Attribute '" + key + "' key must not contain '.'");
         }
         if (key.indexOf('$') >= 0) {
             throw new DomainValidationException("Attribute '" + key + "' key must not contain '$'");
         }
     }

    private static void validateValue(String key, Object value) {
         switch (value) {
             case null -> throw new DomainValidationException("Attribute '" + key + "' value must not be null");
             case String s -> {
                 if (s.length() > MAX_STRING_VALUE_LENGTH) {
                     throw new DomainValidationException(
                             "Attribute '" + key + "' value must not exceed "
                                     + MAX_STRING_VALUE_LENGTH + " characters");
                 }
             }
             case Boolean b -> {
                 // accepted
             }
             case Number n -> {
                 // restrict data types, so there is no BigDecimal, due to wrong data type conversion
                 if (!(n instanceof Integer || n instanceof Long || n instanceof Double)) {
                     throw new DomainValidationException(
                             "Attribute '" + key + "' value must be an Integer, Long, or Double");
                 }
                 // NaN and infinity cannot be represented in JSON
                 if (n instanceof Double d && (d.isNaN() || d.isInfinite())) {
                     throw new DomainValidationException(
                             "Attribute '" + key + "' value must be a finite number");
                 }
             }
             default -> throw new DomainValidationException(
                     "Attribute '" + key + "' value has unsupported type "
                             + value.getClass().getName()
                             + " (allowed: String, Boolean, Number)");
         }
    }

    @Override
    public String toString() {
         return values.toString();
    }
}
