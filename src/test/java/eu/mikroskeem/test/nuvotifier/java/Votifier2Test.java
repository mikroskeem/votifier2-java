package eu.mikroskeem.test.nuvotifier.java;

import eu.mikroskeem.nuvotifier.java.Votifier2;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Votifier2Test {
    public static final String TEST_VOTER_NAME = "mikroskeem";
    public static final String TEST_SERVICE_NAME = "Votifier2TestService";
    public static final String TEST_CHALLENGE_TOKEN = "mcjb3pl2dss7i9naqtpid243qj";
    public static final String TEST_SERVER_KEY = "gnd66gvfrnvnvkj2snf7ebkivv";

    @Test
    public void testNewVoteCreation() throws Exception {
        Votifier2.Vote vote = Votifier2.newVoteObject(TEST_VOTER_NAME, TEST_SERVICE_NAME);
        Assert.assertNotNull("Failed to create new vote!", vote);
    }

    @Test
    public void testVoteEncoding() throws Exception {
        Votifier2.Vote vote = Votifier2.newVoteObject(TEST_VOTER_NAME, TEST_SERVICE_NAME);
        Assert.assertNotNull("Failed to encode vote object!", Votifier2.encodeVote(vote, TEST_CHALLENGE_TOKEN));
    }

    @Test
    public void testVoteMessageGeneration() throws Exception {
        Votifier2.Vote vote = Votifier2.newVoteObject(TEST_VOTER_NAME, TEST_SERVICE_NAME);
        byte[] message = Votifier2.encodeMessage(vote, TEST_CHALLENGE_TOKEN, TEST_SERVER_KEY);
        Assert.assertNotNull("Failed to encode vote!", message);
    }

    @Test
    public void testVoteSending() throws Exception {
        try {
            /* Open up connection to Votifier */
            Socket socket = new Socket("127.0.0.1", 8192);

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            /* Read Votifier packet and grab challenge token */
            String in = new BufferedReader(new InputStreamReader(is)).readLine();
            String[] votifierIn = in.split(" ");
            Assert.assertEquals("Incorrect Votifier data!", 3, votifierIn.length);
            Assert.assertEquals("Votifier signature mismatch", "VOTIFIER", votifierIn[0]);
            String challengeToken = votifierIn[2];

            /* Create vote object */
            Votifier2.Vote vote = Votifier2.newVoteObject(TEST_VOTER_NAME, TEST_SERVICE_NAME);
            byte[] message = Votifier2.encodeMessage(vote, challengeToken, TEST_SERVER_KEY);

            /* Send vote object */
            os.write(message);
            os.flush();

            /* Read status */
            in = new BufferedReader(new InputStreamReader(is)).readLine();
            JSONObject result = new JSONObject(in);
            Assert.assertEquals("Votifier status was not 'ok'! Data: " + result, "ok", result.get("status"));

            /* Close connection */
            os.close();
            socket.close();
        } catch(SocketException e){
            /* Skip test */
            Assume.assumeNoException(e);
        }
    }
}
