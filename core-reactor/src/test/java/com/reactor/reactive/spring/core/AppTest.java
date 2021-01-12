package com.reactor.reactive.spring.core;


import com.sun.source.tree.AssertTree;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;

@Slf4j
public class AppTest {

    @Test
    public void emitterProcessor() {

        var emt = EmitterProcessor.<String>create();

        this.produce(emt.sink());
        this.consumeEmitter(emt);


    }

    @Test
    public void replayProcessor() {
        var repp = ReplayProcessor.<String>create(2, false);
        produce(repp.sink());
        consumeReply(repp);
    }

    @Test
    public void transform() {
        var finished = new AtomicBoolean();
        var letters = Flux.just("A", "B", "C")
                .transform(s -> s.doFinally(s2 -> finished.set(true)))
                .doOnNext(out::println);

        StepVerifier.create(letters).expectNextCount(3).verifyComplete();

        Assert.assertTrue("the finished Boolean must be true.", finished.get());

    }

    @Test
    public void thenMany() {

        var letters = new AtomicInteger();
        var numbers = new AtomicInteger();

        var letterPub = Flux.just("a", "b", "c").doOnNext(v -> letters.incrementAndGet());
        var numberPub = Flux.just(1, 2, 3).doOnNext(n-> numbers.incrementAndGet());

        var thisBeforeThat = letterPub.thenMany(numberPub);

        StepVerifier.create(thisBeforeThat).expectNext(1,2,3).verifyComplete();
        Assert.assertEquals(letters.get(), 3);
        Assert.assertEquals(numbers.get(), 3);
    }

    @Test
    public void maps() {
        var data = Flux.just("a", "b", "c").map(String::toUpperCase);
        StepVerifier.create(data).expectNext("A", "B", "C").verifyComplete();
    }

    private void produce(FluxSink<String> sink) {
        sink.next("1");
        sink.next("2");
        sink.next("3");
        sink.complete();
    }

    private void consumeEmitter(Flux<String> publisher) {
        StepVerifier.create(publisher)
                .expectNext("1")
                .expectNext("2")
                .expectNext("3")
                .verifyComplete();
    }

    private void consumeReply(Flux<String> publisher) {
        for (int i = 0; i < 1; i++)
            StepVerifier.create(publisher).expectNext("2").expectNext("3").verifyComplete();
    }
}
