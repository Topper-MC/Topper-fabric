package me.hsgamer.topper.fabric.config.converter;

public class StringObjectMapConverter extends StringMapConverter<Object> {
    @Override
    protected Object toValue(Object value) {
        return value;
    }

    @Override
    protected Object toRawValue(Object value) {
        return value;
    }
}
