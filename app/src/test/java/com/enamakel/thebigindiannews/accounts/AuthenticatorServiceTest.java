package com.enamakel.thebigindiannews.accounts;

import android.content.Intent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.util.ServiceController;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
public class AuthenticatorServiceTest {
    private AuthenticatorService service;
    private ServiceController<AuthenticatorService> controller;

    @Before
    public void setUp() {
        controller = Robolectric.buildService(AuthenticatorService.class);
        service = controller.attach().create().get();
    }

    @Test
    public void testBinder() {
        assertNotNull(service.onBind(new Intent()));
    }

    @After
    public void tearDown() {
        controller.destroy();
    }
}
