package com.shuking.serviceconsumer;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TestServiceImplTest {

    @Resource
    private TestServiceImpl testService;

    @Test
    void test1() {
        testService.test();
    }
}