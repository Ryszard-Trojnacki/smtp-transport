package pl.rtprog.smtptransport.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Supplier;

public class JsonBodyHandler<T> implements HttpResponse.BodyHandler<Supplier<T>> {

    private static final ObjectMapper om = new ObjectMapper();
    private final Class<T> targetClass;
    private final TypeReference<T> typeReference;

    public JsonBodyHandler(Class<T> targetClass) {
        this.targetClass = targetClass;
        this.typeReference=null;
    }

    public JsonBodyHandler(TypeReference<T> typeReference) {
        this.targetClass=null;
        this.typeReference=typeReference;
    }

    public static <T> JsonBodyHandler<T> of(Class<T> targetClass) {
        return new JsonBodyHandler<>(targetClass);
    }

    public static <T> JsonBodyHandler<T> of(TypeReference<T> targetClass) {
        return new JsonBodyHandler<>(targetClass);
    }

    @Override
    public HttpResponse.BodySubscriber<Supplier<T>> apply(HttpResponse.ResponseInfo responseInfo) {
        if(typeReference!=null) return asJSON(typeReference);
        return asJSON(this.targetClass);
    }


    public static <W> HttpResponse.BodySubscriber<Supplier<W>> asJSON(Class<W> targetType) {
        HttpResponse.BodySubscriber<InputStream> upstream = HttpResponse.BodySubscribers.ofInputStream();

        return HttpResponse.BodySubscribers.mapping(
                upstream,
                inputStream -> toSupplierOfType(inputStream, targetType));
    }

    public static <W> Supplier<W> toSupplierOfType(InputStream inputStream, Class<W> targetType) {
        return () -> {
            try (InputStream stream = inputStream) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(stream, targetType);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    public static <W> HttpResponse.BodySubscriber<Supplier<W>> asJSON(TypeReference<W> targetType) {
        HttpResponse.BodySubscriber<InputStream> upstream = HttpResponse.BodySubscribers.ofInputStream();

        return HttpResponse.BodySubscribers.mapping(
                upstream,
                inputStream -> toSupplierOfType(inputStream, targetType));
    }

    public static <W> Supplier<W> toSupplierOfType(InputStream inputStream, TypeReference<W> targetType) {
        return () -> {
            try (InputStream stream = inputStream) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(stream, targetType);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}