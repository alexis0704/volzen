package com.app.venus.modules.user.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DemoCurrentUserServiceTests {
    @Autowired
    private DemoCurrentUserService demoCurrentUserService;

    @Test
    void demoDriverAndProviderAreDistinctUsers() {
        var driver = demoCurrentUserService.currentDriver();
        var provider = demoCurrentUserService.currentProvider();

        assertThat(driver.getId()).isEqualTo(DemoCurrentUserService.DEMO_DRIVER_ID);
        assertThat(provider.getId()).isEqualTo(DemoCurrentUserService.DEMO_PROVIDER_ID);
        assertThat(driver.getId()).isNotEqualTo(provider.getId());
        assertThat(driver.getRole().getValue()).isEqualTo("driver");
        assertThat(provider.getRole().getValue()).isEqualTo("provider");
    }
}
