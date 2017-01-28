# votifier2-java

Votifier2 vote packet generator.

## Usage

See unit test or code below:

```java
Socket socket = new Socket("127.0.0.1", 8192);
InputStream is = socket.getInputStream();
OutputStream os = socket.getOutputStream();

/* Read Votifier packet and grab challenge token */
String in = new BufferedReader(new InputStreamReader(is)).readLine();
String[] votifierIn = in.split(" ");

String challengeToken = votifierIn[2];

Votifier2.Vote vote = Votifier2.newVoteObject("mikroskeem", "votifier2-java");
byte[] message = Votifier2.encodeMessage(vote, challengeToken, "server-key");

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
```
