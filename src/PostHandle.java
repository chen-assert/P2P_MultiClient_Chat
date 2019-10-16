package src;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostHandle {
    static class RequestFailException extends Exception {

    }

    public static void main(String[] args) throws IOException, RequestFailException {
        String z = PostRequest(1, "加密测试test", "some key");
        System.out.println(PostRequest(1, "加密测试test", "some key"));
    }

    //type:1-encrypt,2-decrypt
    public static String PostRequest(int type, String message, String key) throws IOException, RequestFailException {
        if (key.length() < 8) {
            //return "key too short";
        }
        final String POST_PARAMS = "{\"message\":\"" + message + "\",\"key\":\"" + key + "\"}";
        //System.out.println(POST_PARAMS);
        String url = "encrypt";
        if (type == 2) {
            url = "decrypt";
        }
        URL obj = new URL("http://127.0.0.1:9541/des/" + url);
        HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
        postConnection.setRequestMethod("POST");
        postConnection.setRequestProperty("Content-Type", "application/json");
        postConnection.setDoOutput(true);
        OutputStream os = postConnection.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();
        int responseCode = postConnection.getResponseCode();
        //System.out.println("POST Response Code :  " + responseCode);
        //System.out.println("POST Response Message : " + postConnection.getResponseMessage());
        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    postConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            return response.toString();
        } else {
            throw new RequestFailException();
            //System.out.println("POST NOT WORKED");
        }
    }
}