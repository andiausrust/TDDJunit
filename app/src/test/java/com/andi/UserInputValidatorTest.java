package com.andi;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by AndreasMayer on Dec, 2018
 */
public class UserInputValidatorTest {

    UserInputValidator SUT;

    @Before
    public void setUp() throws Exception {
        SUT = new UserInputValidator();
    }

    @Test
    public void isValidFulName_validFullName_trueReturned() throws Exception {
        boolean result = SUT.isValidFullName("validFullName");
        assertThat(result, is(true));
    }

    @Test
    public void isValidFullName_invalidfullNume_falseReturned() throws Exception {
        boolean result = SUT.isValidFullName("");
        assertThat(result, is(false));
    }

    @Test
    public void isValidUsername_validUsername_trueReturned() throws Exception {
        boolean result = SUT.isValidUsername("validUsername");
        assertThat(result, is(true));
    }

    @Test
    public void isValidUsername_invalidUsername_falseReturned() throws Exception {
        boolean result = SUT.isValidUsername("");
        assertThat(result, is(false));
    }
}