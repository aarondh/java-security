package org.daisleyharrison.security.services.datastore.jsondb.models;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.daisleyharrison.security.common.models.datastore.DatastoreCursor;

public class DatastoreCursorImpl<T> implements DatastoreCursor<T> {
    private DatastoreCollectionImpl<T> collection;
    private Stream<T> data;
    public DatastoreCursorImpl(DatastoreCollectionImpl<T> collection, Stream<T> data){
        this.collection = collection;
        this.data = data;
    }
    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        return this.data.filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return this.data.map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return this.data.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return this.data.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return this.data.mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return this.data.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return this.data.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return this.data.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return this.data.flatMapToDouble(mapper);
    }

    @Override
    public Stream<T> distinct() {
        return this.data.distinct();
    }

    @Override
    public Stream<T> sorted() {
        return this.data.sorted();
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
        return this.sorted(comparator);
    }

    @Override
    public Stream<T> peek(Consumer<? super T> action) {
        return this.data.peek(action);
    }

    @Override
    public Stream<T> limit(long maxSize) {
        return this.data.limit(maxSize);
    }

    @Override
    public Stream<T> skip(long n) {
        return this.data.skip(n);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        this.data.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        this.data.forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return this.data.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return this.data.toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return this.data.reduce(identity, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return this.data.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return this.data.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return this.data.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return this.data.collect(collector);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return this.data.min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return this.data.max(comparator);
    }

    @Override
    public long count() {
        return this.data.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return this.data.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return this.data.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return this.data.noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return this.data.findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return this.data.findAny();
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public Spliterator<T> spliterator() {
        return this.data.spliterator();
    }

    @Override
    public boolean isParallel() {
        return this.data.isParallel();
    }

    @Override
    public Stream<T> sequential() {
        return this.data.sequential();
    }

    @Override
    public Stream<T> parallel() {
        return this.data.parallel();
    }

    @Override
    public Stream<T> unordered() {
        return this.data.unordered();
    }

    @Override
    public Stream<T> onClose(Runnable closeHandler) {
        return this.data.onClose(closeHandler);
    }

    @Override
    public void close() {
        this.data.close();
    }
}