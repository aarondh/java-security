package org.daisleyharrison.security.utilities;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.daisleyharrison.security.utilities.typegenerators.*;

public class RandomTypeGenerator {
    public static final int MINIMUM_NUMBER_OF_BYTES = 4;
    public static final int MAXIMUM_NUMBER_OF_BYTES = 256;
    private static final String PATTERN_MATCH_ALL = TypeGenerator.PATTERN_MATCH_ALL;

    private class GenericTypeGenerator<T> implements TypeGenerator<T> {
        private RandomTypeGenerator parent;

        public GenericTypeGenerator(RandomTypeGenerator parent) {
            this.parent = parent;
        }

        @Override
        public String getPattern() {
            return PATTERN_MATCH_ALL;
        }

        @Override
        public T apply(Class<T> clazz, String name) throws Exception {
            return parent.generate(clazz);
        }
    }

    public static class GeneratorsForType<T> implements TypeGenerator<T> {
        private LinkedList<TypeGenerator<T>> generators = new LinkedList<>();
        private TypeGenerator<T> fallback;
        private Class<T> clazz;

        public GeneratorsForType(Class<T> clazz) {
            this.clazz = clazz;
        }

        public TypeGenerator<T> generatorFor(String name) {
            Optional<TypeGenerator<T>> generator = generators.stream().filter(gen -> name.matches(gen.getPattern()))
                    .findFirst();
            if (generator.isPresent()) {
                return generator.get();
            }
            return fallback;
        }

        public TypeGenerator<T> getFallback() {
            return fallback;
        }

        public void setFallback(TypeGenerator<T> fallback) {
            this.fallback = fallback;
        }

        public void add(TypeGenerator<T> generator) {
            this.generators.addFirst(generator);
        }

        @Override
        public String getPattern() {
            return fallback.getPattern();
        }

        @Override
        public T apply(Class<T> type, String name) throws Exception {
            return generatorFor(name).apply(type, name);
        }

        @Override
        public String toString() {
            return "generators for " + clazz.getSimpleName();
        }

    }

    private final Map<Class<?>, GeneratorsForType<?>> generators = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> void register(Class<T> type, TypeGenerator<T> generator, boolean fallback) {
        GeneratorsForType<T> typeGenerators = (GeneratorsForType<T>) generators.get(type);
        if (typeGenerators == null) {
            typeGenerators = new GeneratorsForType<T>(type);
            generators.put(type, typeGenerators);
        }
        if (fallback) {
            typeGenerators.setFallback(generator);
        } else {
            typeGenerators.add(generator);
        }
    }

    public <T> void register(Class<T> type, TypeGenerator<T> generator) {
        register(type, generator, false);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeGenerator<T> generatorFor(Class<T> clazz, String name) {
        return (GeneratorsForType<T>) generatorForClass(clazz, name);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public TypeGenerator<Object> generatorForClass(Class clazz, String name) {
        if (generators.isEmpty()) {
            registerDefaults();
        }
        GeneratorsForType<Object> typeGenerators = (GeneratorsForType<Object>) generators.get(clazz);
        if (typeGenerators == null) {
            if (clazz.isEnum()) {
                return RandomEnumGenerator.getInstance();
            }
            TypeGenerator<Object> generator = new GenericTypeGenerator(this);
            register(clazz, generator, true);
            return generator;
        } else {
            return typeGenerators.generatorFor(name);
        }
    }

    public void registerDefaults() {
        register(String.class, new RandomStringFormatGenerator(".*", "<lorem-paragraph>[1-5]"), true);

        register(String.class, new RandomStringUUIDGenerator(".*(?i:id)"));

        register(String.class, new NullTypeGenerator<String>("_id"));

        register(String.class, new RandomStringFormatGenerator(".*(?i:email).*",
                "<first-name>.<last-name>@{outlook gmail yahoo yelp linkedin}.com"));

        register(String.class, new RandomStringFormatGenerator(".*(?i:phone).*",
                "{1-}[0.5]\\({1 2 3 4 5 6 7 8 9}<digit>[2]\\)<digit>[3]-<digit>[4]"));
        register(String.class,
                new RandomStringFormatGenerator(".*(?i:postal|zip).*", "{1 2 3 4 5 6 7 8 9}<digit>[4]"));
        register(String.class, new RandomStringFormatGenerator(".*(?i:address).*",
                "<digit>[1-6] <last-name> {Street Ave St Court Ct Avenue} {N S E W NE SE NW SW North South}[0.5]"));
        register(String.class, new RandomStringFormatGenerator(".*(?i:given|first).*(?i:name)", "<first-name>"));
        register(String.class, new RandomStringFormatGenerator(".*(?i:middle).*(?i:name)", "<first-name>[0.5]"));
        register(String.class, new RandomStringFormatGenerator(".*(?i:last|family).*(?i:name)", "<last-name>"));
        register(String.class, new RandomStringFormatGenerator(".*(?i:zone).*", "<time-zone-id>"));
        register(String.class, new RandomStringFormatGenerator(".*(?i:username).*", "<first-name:lower><digit>[0.6 1-4]"));
        register(String.class, new RandomStringFormatGenerator(".*(?i:nickname).*", "<first-name>[0.2]"));
        register(String.class, new RandomStringFormatGenerator(".*(?i:website).*",
                "{http https}://{yahoo google outlook facebook linkedin yelp}.{com org gov tv}/<last-name:lower>/<first-name:lower>"));
        register(String.class, new RandomStringFormatGenerator("country", "<country>"));
        register(String.class, new RandomStringFormatGenerator(".*(?i:locality|city).*", "<us-city>"));
        register(String.class, new RandomStringFormatGenerator(".*(?i:desc).*", "<lorem-paragraph>[1-10]"));
        register(String.class, new RandomStringFormatGenerator("password", "<alpha-mixed:digify>[8-16]"));
        register(String.class, new RandomStringFormatGenerator("gender", "{male female Male Female M F }[0.7]"));

        register(Boolean.class, new RandomBooleanGenerator(PATTERN_MATCH_ALL));

        register(boolean.class, new RandomBooleanGenerator(PATTERN_MATCH_ALL));

        register(Integer.class, new RandomIntegerGenerator(PATTERN_MATCH_ALL), true);

        register(int.class, new RandomIntegerGenerator(PATTERN_MATCH_ALL), true);

        register(Long.class, new RandomLongGenerator(PATTERN_MATCH_ALL), true);

        register(long.class, new RandomLongGenerator(PATTERN_MATCH_ALL), true);

        register(Float.class, new RandomFloatGenerator(PATTERN_MATCH_ALL), true);

        register(float.class, new RandomFloatGenerator(PATTERN_MATCH_ALL), true);

        register(Double.class, new RandomDoubleGenerator(PATTERN_MATCH_ALL), true);

        register(double.class, new RandomDoubleGenerator(PATTERN_MATCH_ALL), true);

        register(Date.class, new RandomDateGenerator(PATTERN_MATCH_ALL), true);

        register(UUID.class, new RandomUUIDGenerator(PATTERN_MATCH_ALL), true);

        register(BigInteger.class, new RandomBigIntegerGenerator(PATTERN_MATCH_ALL), true);

    }

    @SuppressWarnings("unchecked")
    public <T> T generate(Class<T> clazz) throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = field.getName();
            try {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object value = (Object) generatorForClass(fieldType, fieldName).apply((Class<Object>) fieldType,
                        fieldName);
                field.set(instance, value);
            } catch (Exception ex) {
                String message = String.format("Error generating data for field %s of class %s", fieldName,
                        clazz.getName());
                throw new IllegalArgumentException(message, ex);
            }
        }
        return instance;
    }
}