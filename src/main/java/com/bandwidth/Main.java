package com.bandwidth;


import com.bandwidth.exceptions.ApiException;
import com.bandwidth.http.response.ApiResponse;
import com.bandwidth.twofactorauth.controllers.APIController;
import com.bandwidth.twofactorauth.models.TwoFactorCodeRequestSchema;
import com.bandwidth.twofactorauth.models.TwoFactorMessagingResponse;
import com.bandwidth.twofactorauth.models.TwoFactorVerifyCodeResponse;
import com.bandwidth.twofactorauth.models.TwoFactorVerifyRequestSchema;
import com.bandwidth.twofactorauth.models.TwoFactorVoiceResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {


    private static final String username = System.getenv("BANDWIDTH_USERNAME");
    private static final String password = System.getenv("BANDWIDTH_PASSWORD");
    private static final String accountId = System.getenv("BANDWIDTH_ACCOUNT_ID");
    private static final String voiceApplicationId = System.getenv("BANDWIDTH_VOICE_APPLICATION_ID");
    private static final String messagingApplicationId = System.getenv("BANDWIDTH_MESSAGING_APPLICATION_ID");
    private static final String bandwidthNumber = System.getenv("BANDWIDTH_PHONE_NUMBER");

    private static final BandwidthClient client = new BandwidthClient.Builder()
        .twoFactorAuthBasicAuthCredentials(username, password)
        .environment(Environment.PRODUCTION)
        .build();

    private static final APIController controller = client.getTwoFactorAuthClient().getAPIController();

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(new InputStreamReader(System.in));

        System.out.println("Send the code to the following phone number (E164 format: +13330004444): ");
        String toNumber = scanner.nextLine();

        System.out.println("Text or Voice? ");
        String option = scanner.nextLine();

        boolean success = false;
        String applicationId = null;
        if ("text".equalsIgnoreCase(option)) {
            success = sendMessageCode(toNumber);
            applicationId = messagingApplicationId;
        } else if ("voice".equalsIgnoreCase(option)) {
            success = sendVoiceCode(toNumber);
            applicationId = voiceApplicationId;
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

        ApiResponse<TwoFactorVerifyCodeResponse> verifyResponse = null;
        try {
            verifyResponse = controller.createVerifyTwoFactor(accountId, new TwoFactorVerifyRequestSchema().toBuilder()
                .applicationId(applicationId)
                .code(code)
                .to(toNumber)
                .from(bandwidthNumber)
                .digits(5)
                .scope("sample")
                .expirationTimeInMinutes(3)
                .build()
            );
        } catch (ApiException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getResponseCode());
            return;
        }

        if (Boolean.TRUE.equals(verifyResponse.getResult().getValid())) {
            System.out.println("Valid Code!");
        } else {
            System.out.println("Invalid Code");
        }


    }

    public static boolean sendMessageCode(String toNumber) throws IOException {
        ApiResponse<TwoFactorMessagingResponse> response = null;
        try {
            response = controller.createMessagingTwoFactor(accountId,
                new TwoFactorCodeRequestSchema.Builder()
                    .applicationId(messagingApplicationId)
                    .from(bandwidthNumber)
                    .to(toNumber)
                    .digits(5)
                    .message("Hello World this is your code: {CODE}") // {CODE} is required
                    .scope("sample")
                    .build()
            );
        } catch (ApiException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getResponseCode());
            return false;
        }

        if (response == null ) {
            System.out.println("Response was null");
            return false;
        }

        return true;
    }

    public static boolean sendVoiceCode(String toNumber) throws IOException {
        ApiResponse<TwoFactorVoiceResponse> response = null;
        try {
            response = controller.createVoiceTwoFactor(accountId,
                new TwoFactorCodeRequestSchema.Builder()
                    .applicationId(voiceApplicationId)
                    .from(bandwidthNumber)
                    .to(toNumber)
                    .digits(5)
                    .message("Hello World this is your code: {CODE}") // {CODE} is required
                    .scope("sample")
                    .build()
            );
        } catch (ApiException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getResponseCode());
            return false;
        }

        if (response == null ) {
            System.out.println("Response was null");
            return false;
        }

        return true;
    }





}
