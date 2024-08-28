import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Random;
import org.json.JSONTokener;

public class DestinationHashGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SALT_LENGTH = 8;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN Number> <path to JSON file>");
            return;
        }

        String prnNumber = args[0];
        String jsonFilePath = args[1];

        try {
            String destinationValue = findDestinationValue(jsonFilePath);
            if (destinationValue != null) {
                String randomString = generateRandomString(SALT_LENGTH);
                String concatenatedString = prnNumber + destinationValue + randomString;
                String md5Hash = generateMD5Hash(concatenatedString);
                System.out.println(md5Hash + ";" + randomString);
            } else {
                System.out.println("Key 'destination' not found in the JSON file.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String findDestinationValue(String filePath) throws IOException {
        FileReader reader = new FileReader(filePath, StandardCharsets.UTF_8);
        JSONTokener tokener = new JSONTokener(reader);
        JSONObject jsonObject = new JSONObject(tokener);
        return findDestinationValueRecursive(jsonObject);
    }

    private static String findDestinationValueRecursive(JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if ("destination".equals(key)) {
                return jsonObject.getString(key);
            }
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                String result = findDestinationValueRecursive((JSONObject) value);
                if (result != null) {
                    return result;
                }
            } else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        String result = findDestinationValueRecursive((JSONObject) item);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
