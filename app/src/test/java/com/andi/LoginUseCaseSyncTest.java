package com.andi;

import com.andi.authtoken.AuthTokenCache;
import com.andi.eventbus.EventBusPoster;
import com.andi.eventbus.LoggedInEvent;
import com.andi.networking.LoginHttpEndpointSync;
import com.andi.networking.NetworkErrorException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by AndreasMayer on Dec, 2018
 */
public class LoginUseCaseSyncTest {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String AUTH_TOKEN = "authToken";

    LoginUseCaseSync SUT;
    LoginUseCaseSync mSUT;

    // testing with Mockito
    LoginHttpEndpointSync mLoginHttpEndpointSyncMockito;
    AuthTokenCache mAuthTokenCacheMockito;
    EventBusPoster mEventBusPosterMockito;

    // testing with doubles
    LoginHttpEndpointSyncTd loginHttpEndpointSyncTd;
    AuthTokenCacheTd authTokenCacheTd;
    EventBusPosterTd eventBusPosterTd;


    @Before
    public void setUp() throws Exception {
        // setup for Mockito
        mLoginHttpEndpointSyncMockito = Mockito.mock(LoginHttpEndpointSync.class);
        mAuthTokenCacheMockito = Mockito.mock(AuthTokenCache.class);
        mEventBusPosterMockito = Mockito.mock(EventBusPoster.class);

        mSUT = new LoginUseCaseSync(mLoginHttpEndpointSyncMockito, mAuthTokenCacheMockito, mEventBusPosterMockito);

        // when is called with any String it should return ... -> should be extracted into method
        when(mLoginHttpEndpointSyncMockito.loginSync(any(String.class), any(String.class)))
                .thenReturn(new LoginHttpEndpointSync.EndpointResult(LoginHttpEndpointSync.EndpointResultStatus.SUCCESS, AUTH_TOKEN));

        // setup for Testing Doubles
        loginHttpEndpointSyncTd = new LoginHttpEndpointSyncTd();
        authTokenCacheTd = new AuthTokenCacheTd();
        eventBusPosterTd = new EventBusPosterTd();

        SUT = new LoginUseCaseSync(loginHttpEndpointSyncTd, authTokenCacheTd, eventBusPosterTd);
    }

    // #############################################################################################
    // testing with Mockito
    // #############################################################################################

    @Test
    public void m_loginSync_success_usernameAndPasswordPassedToEndpoint() throws Exception {
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        mSUT.loginSync(USERNAME, PASSWORD);
        // verify that a specific method was called on a mock, 1 times (in this example), capture the arguments
        verify(mLoginHttpEndpointSyncMockito, times(1)).loginSync(ac.capture(), ac.capture());
        List<String> captures = ac.getAllValues();
        assertThat(captures.get(0), is(USERNAME));
        assertThat(captures.get(1), is(PASSWORD));
    }



    // #############################################################################################
    // testing with manual TestingDoubles
    // #############################################################################################

    // username and password passed to the endpoint
    @Test
    public void loginSync_success_usernameAndPasswordPassedToEndpoint() throws Exception {
        SUT.loginSync(USERNAME, PASSWORD);
        Assert.assertThat(loginHttpEndpointSyncTd.mUsername, is(USERNAME));
        Assert.assertThat(loginHttpEndpointSyncTd.mPassword, is(PASSWORD));
    }

    // if login succeeds - user's auth token must be cached
    @Test
    public void loginSync_success_authThokenCached() throws NetworkErrorException {
        SUT.loginSync(USERNAME, PASSWORD);
        assertThat(authTokenCacheTd.getAuthToken(), is(AUTH_TOKEN));
    }

    // if login failed - auth token is not changed
    @Test
    public void loginSync_generalError_authTokenNotCached() throws NetworkErrorException {
        loginHttpEndpointSyncTd.mIsGeneralError = true;
        SUT.loginSync(USERNAME, PASSWORD);
        assertThat(authTokenCacheTd.getAuthToken(), is(""));
    }

    @Test
    public void loginSync_authError_authTokenNotCached() throws NetworkErrorException {
        loginHttpEndpointSyncTd.mIsAuthError = true;
        SUT.loginSync(USERNAME, PASSWORD);
        assertThat(authTokenCacheTd.getAuthToken(), is(""));
    }

    @Test
    public void loginSync_serverError_authTokenNotCached() throws NetworkErrorException {
        loginHttpEndpointSyncTd.mIsServerError = true;
        SUT.loginSync(USERNAME, PASSWORD);
        assertThat(authTokenCacheTd.getAuthToken(), is(""));
    }

    // if login succeeds - login event posted to event bus
    @Test
    public void loginSync_success_loggedInEventPosted() throws NetworkErrorException {
        SUT.loginSync(USERNAME, PASSWORD);
        assertThat(eventBusPosterTd.mEvent, is(instanceOf(LoggedInEvent.class)));
    }

    // if login fails - no login event posted
    @Test
    public void loginSync_generalError_noInteractionWithEventBusPoster() throws NetworkErrorException {
        loginHttpEndpointSyncTd.mIsServerError = true;
        SUT.loginSync(USERNAME, PASSWORD);
        assertThat(eventBusPosterTd.mInteractionCount, is(0));
    }

    // if login succeeds - success returned

    @Test
    public void loginSync_success_successReturned() throws NetworkErrorException {
        LoginUseCaseSync.UseCaseResult result = SUT.loginSync(USERNAME, PASSWORD);
        assertThat(result, is(LoginUseCaseSync.UseCaseResult.SUCCESS));
    }

    // fails - fail returned
    @Test
    public void loginSync_serverError_failureReturned() throws NetworkErrorException {
        LoginUseCaseSync.UseCaseResult result = SUT.loginSync(USERNAME, PASSWORD);
        assertThat(result, is(LoginUseCaseSync.UseCaseResult.SUCCESS));
    }

    @Test
    public void loginSync_authError_failureReturned() throws NetworkErrorException {
        LoginUseCaseSync.UseCaseResult result = SUT.loginSync(USERNAME, PASSWORD);
        assertThat(result, is(LoginUseCaseSync.UseCaseResult.SUCCESS));
    }

    // network - network error returned
    @Test
    public void loginSync_networkError_networkErrorReturned() throws NetworkErrorException {
        loginHttpEndpointSyncTd.mIsNetworkError = true;
        LoginUseCaseSync.UseCaseResult result = SUT.loginSync(USERNAME, PASSWORD);
        assertThat(result, is(LoginUseCaseSync.UseCaseResult.NETWORK_ERROR));
    }


    private static class LoginHttpEndpointSyncTd implements LoginHttpEndpointSync {
        public String mUsername;
        private String mPassword;
        public boolean mIsGeneralError;
        public boolean mIsAuthError;
        public boolean mIsServerError;
        public boolean mIsNetworkError;

        @Override
        public EndpointResult loginSync(String username, String password) throws NetworkErrorException {
            mUsername = username;
            mPassword = password;
            if (mIsGeneralError) {
                return new EndpointResult(EndpointResultStatus.GENERAL_ERROR, "");
            } else if (mIsAuthError) {
                return new EndpointResult(EndpointResultStatus.AUTH_ERROR, "");
            } else if (mIsServerError) {
                return new EndpointResult(EndpointResultStatus.SERVER_ERROR, "");
            } else if (mIsNetworkError) {
                throw new NetworkErrorException();
            } else {
                return new EndpointResult(EndpointResultStatus.SUCCESS, AUTH_TOKEN);
            }
        }
    }

    private static class AuthTokenCacheTd implements AuthTokenCache {

        String mAuthToken = "";

        @Override
        public void cacheAuthToken(String authToken) {
            mAuthToken = authToken;
        }

        @Override
        public String getAuthToken() {
            return mAuthToken;
        }
    }

    private static class EventBusPosterTd implements EventBusPoster {
        public Object mEvent;
        public int mInteractionCount;

        @Override
        public void postEvent(Object event) {
            mInteractionCount++;
            mEvent = event;
        }
    }

}