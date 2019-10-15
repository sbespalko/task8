package com.sbt.hakaton.task8;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Task8ApplicationTests.class)
class Task8ApplicationTests {
    private static final Logger LOG = LogManager.getLogger();

    @Test
    void contextLoads() {
        LOG.info("info");
        LOG.debug("debug");

    }

}
