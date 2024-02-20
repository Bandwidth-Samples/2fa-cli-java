package com.bandwidth;

import com.bandwidth.sdk.ApiResponse;
import com.bandwidth.sdk.ApiException;
import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.auth.HttpBasicAuth;
import com.bandwidth.sdk.model.*;
import com.bandwidth.sdk.api.*;
import com.bandwidth.sdk.Configuration;

import java.math.BigDecimal;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {

    private static final String username = System.getenv("BW_USERNAME");
    private static final String password = System.getenv("BW_PASSWORD");
    private static final String accountId = System.getenv("BW_ACCOUNT_ID");
    private static final String voiceApplicationId = System.getenv("BW_VOICE_APPLICATION_ID");
    private static final String messagingApplicationId = System.getenv("BW_MESSAGING_APPLICATION_ID");
    private static final String bandwidthNumber = System.getenv("BW_NUMBER");

    public static ApiClient defaultClient = Configuration.getDefaultApiClient();
    public static HttpBasicAuth Basic = (HttpBasicAuth) defaultClient.getAuthentication("Basic");
    public static MfaApi api = new MfaApi(defaultClient);

    public static void main(String[] args) throws IOException {

        CodeRequest request = new CodeRequest();
        Scanner scanner = new Scanner(new InputStreamReader(System.in));

        System.out.println("Send the code to the following phone number (E164 format: +13330004444): ");
        String toNumber = scanner.nextLine();

        System.out.println("Text or Voice? ");
        String option = scanner.nextLine();

        boolean success;
        String applicationId = null;
        if ("text".equalsIgnoreCase(option)) {
            success = sendMessageCode(toNumber);
        } else if ("voice".equalsIgnoreCase(option)) {
            success = sendVoiceCode(toNumber);
        } else {
            System.out.println("Must be 'Voice' or 'text'");
            return;
        }

        if (!success) {
            System.out.println("There was an error during code creation.");
            return;
        }

        System.out.println("What is the Code? ");
        String code = scanner.nextLine();

        BigDecimal expirationTime = new BigDecimal(3);

        VerifyCodeRequest verifyRequest = new VerifyCodeRequest();
	verifyRequest.setCode(code);
	verifyRequest.setTo(toNumber);
	verifyRequest.setScope("mfa");
	verifyRequest.setExpirationTimeInMinutes(expirationTime);
        ApiResponse<VerifyCodeResponse> response = null; 
        try {
            response = api.verifyCodeWithHttpInfo(accountId, verifyRequest);

        } catch (ApiException e) {
            System.out.println(e.getMessage());
            return;
        }

        if (Boolean.TRUE.equals(response.getData().getValid())) {
            System.out.println("Valid Code!");
        } else {
            System.out.println("Invalid Code");
        }

	scanner.close();
	return;
    }

    public static boolean sendMessageCode(String toNumber) throws IOException {

	Basic.setUsername(username);
	Basic.setPassword(password);
        ApiResponse<MessagingCodeResponse> response = null;
        CodeRequest request = new CodeRequest();
        request.setTo(toNumber);
        request.setFrom(bandwidthNumber);
        request.setApplicationId(messagingApplicationId);
        request.setScope("mfa");
        request.setMessage("Your temporary {NAME} {SCOPE} code is {CODE}");
        request.setDigits(5);

        try {
	    response = api.generateMessagingCodeWithHttpInfo(accountId, request);
        } catch (ApiException e) {
            System.out.println(e.getMessage());
            return false;
        }

        if (response == null ) {
            System.out.println("Response was null");
            return false;
        }

        return true;
    }

    public static boolean sendVoiceCode(String toNumber) throws IOException {

	Basic.setUsername(username);
	Basic.setPassword(password);
        ApiResponse<VoiceCodeResponse> response = null; 
        CodeRequest request = new CodeRequest();
        request.setTo(toNumber);
        request.setFrom(bandwidthNumber);
        request.setApplicationId(voiceApplicationId);
        request.setScope("mfa");
        request.setMessage("This is your {CODE}");
        request.setDigits(5);

        try {
            response = api.generateVoiceCodeWithHttpInfo(accountId, request);
        } catch (ApiException e) {
            System.out.println(e.getMessage());
            return false;
        }

        if (response == null ) {
            System.out.println("Response was null");
            return false;
        }

        return true;
    }





}
