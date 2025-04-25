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
        BigInteger secret1 = findSecretFromJson(json1);  // Find the secret for the first test case
        BigInteger secret2 = findSecretFromJson(json2);  // Find the secret for the second test case

        // Final output: Print the reconstructed secrets for both test cases
        System.out.println(secret1);
        System.out.println(secret2);
    }

    // Method to extract shares from the given JSON and reconstruct the secret
    public static BigInteger findSecretFromJson(String jsonData) {
        JSONObject obj = new JSONObject(jsonData);  // Parse the JSON string into a JSONObject

        // Lists to store x and y values from the JSON data (shares)
        List<Integer> xList = new ArrayList<>();
        List<BigInteger> yList = new ArrayList<>();

        // Iterate over each entry in the JSON object to extract shares
        for (String key : obj.keySet()) {
            if (key.equals("keys")) continue;  // Skip the 'keys' object

            JSONObject point = obj.getJSONObject(key);  // Get the share data for each x-coordinate
            int x = Integer.parseInt(key);  // Parse x-coordinate (key of the share)
            int base = Integer.parseInt(point.getString("base"));  // Get the base of the value
            String valueStr = point.getString("value");  // Get the value string of the share

            try {
                // Convert the value from the specified base to a BigInteger
                BigInteger y = new BigInteger(valueStr, base);
                xList.add(x);  // Add x to the list of x-coordinates
                yList.add(y);  // Add y to the list of y-values (shares)
            } catch (NumberFormatException e) {
                // If there's an error in parsing the value (invalid number), print error message
                System.out.println("Error parsing value for share " + key + ": " + valueStr);
                return BigInteger.ZERO;  // Return a default error value (adjustable)
            }
        }

        // Apply Lagrange interpolation to calculate the secret (reconstructed value at x = 0)
        return lagrangeInterpolationAtZero(xList, yList);
    }

    // Lagrange interpolation at x = 0 (reconstructing the secret)
    public static BigInteger lagrangeInterpolationAtZero(List<Integer> xList, List<BigInteger> yList) {
        int k = xList.size();  // Number of shares
        BigInteger result = BigInteger.ZERO;  // Initialize the result to 0 (secret)

        // Loop over each share and compute the Lagrange basis polynomials
        for (int j = 0; j < k; j++) {
            BigInteger numerator = BigInteger.ONE;  // Initialize numerator for Lagrange basis
            BigInteger denominator = BigInteger.ONE;  // Initialize denominator for Lagrange basis

            // Calculate the product of (x - x_i) terms for the denominator and (x - x_j) for the numerator
            for (int i = 0; i < k; i++) {
                if (i == j) continue;  // Skip when i equals j (no division by zero)
                numerator = numerator.multiply(BigInteger.valueOf(-xList.get(i)));
                denominator = denominator.multiply(BigInteger.valueOf(xList.get(j) - xList.get(i)));
            }

            // Calculate the Lagrange term and add it to the result
            BigInteger term = yList.get(j).multiply(numerator).divide(denominator);
            result = result.add(term);  // Add this term to the final secret value
        }

        return result;  // Return the reconstructed secret (value at x = 0)
    }
}
