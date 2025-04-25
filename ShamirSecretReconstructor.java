import org.json.JSONObject;
import java.math.BigInteger;
import java.util.*;

public class ShamirSecretReconstructor {

    public static void main(String[] args) {

        // === Test Case 1 ===
        String json1 = """
        {
          "keys": {"n": 4, "k": 4},
          "1": {"base": "10", "value": "4"},
          "2": {"base": "2", "value": "111"},
          "3": {"base": "10", "value": "12"},
          "6": {"base": "4", "value": "213"}
        }
        """;

        // === Test Case 2 ===
        String json2 = """
        {
          "keys": {"n": 10, "k": 10},
          "1": {"base": "6", "value": "13444211440455345511"},
          "2": {"base": "15", "value": "aed7015a346d63"},
          "3": {"base": "15", "value": "6aeeb69631c227c"},
          "4": {"base": "16", "value": "e1b5e05623d881f"},
          "5": {"base": "8", "value": "316034514573652620673"},
          "6": {"base": "3", "value": "2122212201122002221120200210011020220200"},
          "7": {"base": "3", "value": "20120221122211000100210021102001201112121"},
          "8": {"base": "6", "value": "20220554335330240002224253"},
          "9": {"base": "12", "value": "45153788322a1255483"},
          "10": {"base": "7", "value": "1101613130313526312514143"}
        }
        """;

        // Process and print secrets
        BigInteger secret1 = findSecretFromJson(json1);
        BigInteger secret2 = findSecretFromJson(json2);

        // Final output
        System.out.println(secret1);
        System.out.println(secret2);
    }

    public static BigInteger findSecretFromJson(String jsonData) {
        JSONObject obj = new JSONObject(jsonData);

        // Extract all x and y points from JSON
        List<Integer> xList = new ArrayList<>();
        List<BigInteger> yList = new ArrayList<>();

        for (String key : obj.keySet()) {
            if (key.equals("keys")) continue;

            JSONObject point = obj.getJSONObject(key);
            int x = Integer.parseInt(key);
            int base = Integer.parseInt(point.getString("base"));
            String valueStr = point.getString("value");

            try {
                BigInteger y = new BigInteger(valueStr, base);
                xList.add(x);
                yList.add(y);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing value for share " + key + ": " + valueStr);
                return BigInteger.ZERO;  // Return a default error value (can be adjusted as needed)
            }
        }

        return lagrangeInterpolationAtZero(xList, yList);
    }

    public static BigInteger lagrangeInterpolationAtZero(List<Integer> xList, List<BigInteger> yList) {
        int k = xList.size();
        BigInteger result = BigInteger.ZERO;

        for (int j = 0; j < k; j++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int i = 0; i < k; i++) {
                if (i == j) continue;
                numerator = numerator.multiply(BigInteger.valueOf(-xList.get(i)));
                denominator = denominator.multiply(
                    BigInteger.valueOf(xList.get(j) - xList.get(i))
                );
            }

            BigInteger term = yList.get(j).multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }
}
