package eu.mikroskeem.nuvotifier.java;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

public final class Votifier2 {
    /* Calculate SHA256 HMAC from string */
    public static String calculateSHA256HMACDigest(String key, String content)
            throws NoSuchAlgorithmException, InvalidKeyException{
        Mac SHA256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        SHA256HMAC.init(keySpec);
        return new String(Base64.getEncoder().encode(SHA256HMAC.doFinal(content.getBytes())));
    }

    /* Generate new vote */
    public static Vote newVoteObject(String voterName, String serviceName){
        try {
            return new Vote(voterName, InetAddress.getByName("127.0.0.1"), Instant.now().toEpochMilli(), serviceName);
        }
        catch(UnknownHostException ignored){}
        return null;
    }

    /* Encode vote into JSON string */
    public static String encodeVote(Vote vote, String challengeToken){
        JSONObject voteObject = new JSONObject();
        voteObject.put("challenge", challengeToken);
        voteObject.put("username", vote.getVoterName());
        voteObject.put("address", vote.getVoteOrigin().getHostAddress());
        voteObject.put("timestamp", vote.getTimestamp());
        voteObject.put("serviceName", vote.getServiceName());
        return voteObject.toString();
    }

    /* Get fully encoded vote */
    public static byte[] encodeMessage(Vote vote, String challengeToken, String key) throws Exception {
        String payload = encodeVote(vote, challengeToken);
        String signature = calculateSHA256HMACDigest(key, payload);
        JSONObject message = new JSONObject();
        message.put("payload", payload);
        message.put("signature", signature);
        String finalMessage = message.toString();

        ByteBuffer bb = ByteBuffer.allocate(finalMessage.length()+4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putShort(toUint16(0x733a));
        bb.putShort(toUint16(finalMessage.length()));
        for(char c : finalMessage.toCharArray()) bb.put((byte)c);
        return bb.array();
    }

    private static short toUint16(int in){
        /*
         * Java doesn't support unsigned types natively
         * http://stackoverflow.com/a/28707472
         */
        return (short)(in & 0xFFFF);
    }

    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class Vote {
        private final String voterName;
        private final InetAddress voteOrigin;
        private final long timestamp;
        private final String serviceName;
        //private final UUID uuid;
    }
}
