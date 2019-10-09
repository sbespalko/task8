package com.sbt.hakaton.task8;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Task8ApplicationTests {
    private static final Logger LOG = LoggerFactory.getLogger(Task8ApplicationTests.class);

    @Test
    void contextLoads() {
        LOG.info("info");
        LOG.debug("debug");

    }

}
