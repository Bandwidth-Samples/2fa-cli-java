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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);

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

        logger.info("Send the code to the following phonenumber: ");
        String toNumber = scanner.nextLine();

        logger.info("Text or Voice? ");
        String option = scanner.nextLine();

        boolean success = false;
        if ("text".equalsIgnoreCase(option)) {
            success = sendMessageCode(toNumber);
        } else if ("voice".equalsIgnoreCase(option)) {
            success = sendVoiceCode(toNumber);
        } else {
            logger.error("Must be 'Voice' or 'text'");
            return;
        }

        if (!success) {
            logger.error("There was an error during code creation.");
            return;
        }

        logger.info("What is the Code? ");
        String code = scanner.nextLine();

        ApiResponse<TwoFactorVerifyCodeResponse> verifyResponse = null;
        try {
            verifyResponse = controller.createVerifyTwoFactor(accountId, new TwoFactorVerifyRequestSchema().toBuilder()
                .applicationId(voiceApplicationId)
                .code(code)
                .to("")
                .from("")
                .digits(5)
                .scope("sample")
                .expirationTimeInMinutes(3)
                .build()
            );
        } catch (ApiException e) {
            logger.error(e.getMessage());
            return;
        }

        if (Boolean.TRUE.equals(verifyResponse.getResult().getValid())) {
            logger.info("Valid Code");
        } else {
            logger.info("Invalid Code");
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
                    .message("Hello World this is your code:")
                    .scope("sample")
                    .build()
            );
        } catch (ApiException e) {
            logger.error(e.getMessage());
            return false;
        }

        if (response == null ) {
            logger.error("Response was null");
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
                    .message("Hello World this is your code:")
                    .scope("sample")
                    .build()
            );
        } catch (ApiException e) {
            logger.error(e.getMessage());
            return false;
        }

        if (response == null ) {
            logger.error("Response was null");
            return false;
        }

        return true;
    }





}
