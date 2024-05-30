package br.com.ccs.uuidvslong.services;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(UuidVsLongTest.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
