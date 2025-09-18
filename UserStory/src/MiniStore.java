import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Mini-store application that manages a product inventory
 * using JOptionPane for all interaction.
 *
 * The following data structures are used:
 * - ArrayList<String> for product names (allows dynamic growth).
 * - double[] for prices, synchronized by index with the ArrayList of names.
 * - HashMap<String, Integer> for stock, associating each name with its quantity.
 */
public class MiniStore {

    // DATA MODEL - TASK 1
    private static ArrayList<String> names = new ArrayList<>();
    private static double[] prices = new double[0]; 
    private static HashMap<String, Integer> stock = new HashMap<>();

    private static double totalSales = 0.0;
    
    // Formatter to display numbers with thousands and decimal separators
    private static DecimalFormat currencyFormatter;

    public static void main(String[] args) {
      
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        currencyFormatter = new DecimalFormat("#.0", symbols);

        int option = -1;
        do {
            try {
                // MAIN MENU - TASK 2
                String menu = """
                    --- INVENTORY MANAGEMENT ---
                    1. Add product
                    2. List inventory
                    3. Buy product
                    4. Show statistics
                    5. Search product by name
                    0. Exit (Show final receipt)
                    """;

                String input = JOptionPane.showInputDialog(null, menu, "Main Menu", JOptionPane.PLAIN_MESSAGE);
                if (input == null) { // If the user presses Cancel
                    option = 0;
                } else {
                    option = Integer.parseInt(input);

                    // FLOW FOR EACH OPTION - TASK 3
                    switch (option) {
                        case 1:
                            addProduct();
                            break;
                        case 2:
                            listInventory();
                            break;
                        case 3:
                            buyProduct();
                            break;
                        case 4:
                            showStatistics();
                            break;
                        case 5:
                            searchProduct();
                            break;
                        case 0:
                            showFinalReceipt();
                            break;
                        default:
                            JOptionPane.showMessageDialog(null, "Invalid option. Please try again.");
                            break;
                    }
                }
            } catch (NumberFormatException e) {
                // VALIDATIONS AND MESSAGES - TASK 4
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } while (option != 0);
    }

    /**
     * Adds a new product to the inventory.
     * Asks for name, price, and stock and performs validations.
     */
    private static void addProduct() {
        String name = JOptionPane.showInputDialog("Enter the product name:");
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Do not allow duplicates
        if (stock.containsKey(name.toLowerCase())) {
            JOptionPane.showMessageDialog(null, "The product already exists in the inventory.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double price = 0.0;
        String priceStr;
        boolean validPrice = false;
        while (!validPrice) {
            priceStr = JOptionPane.showInputDialog("Enter the product price (e.g. 5,500 or 5.500):");
            if (priceStr == null) return; // Exit if the user cancels

            String priceToParse = priceStr.replace(",", "");
            
            // If the string contains a period and it looks like a thousands separator (e.g., 5.500), remove it
            if (priceToParse.matches("\\d+\\.\\d{3}")) {
                priceToParse = priceToParse.replace(".", "");
            }

            try {
                price = Double.parseDouble(priceToParse);
                if (price > 0) {
                    validPrice = true;
                } else {
                     JOptionPane.showMessageDialog(null, "The price must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid price. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        int initialStock = 0;
        String stockStr;
        boolean validStock = false;
        while (!validStock) {
            stockStr = JOptionPane.showInputDialog("Enter the initial stock:");
            if (stockStr == null) return; // Exit if the user cancels

            try {
                initialStock = Integer.parseInt(stockStr);
                if (initialStock >= 0) {
                    validStock = true;
                } else {
                    JOptionPane.showMessageDialog(null, "Stock cannot be negative. Please enter a non-negative integer.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid stock. Please enter an integer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Add to data structures
        names.add(name);
        prices = expandPrices(prices, price);
        stock.put(name.toLowerCase(), initialStock);
        
        JOptionPane.showMessageDialog(null, "Product added successfully.");
    }

    /**
     * Helper to expand the prices array.
     */
    private static double[] expandPrices(double[] original, double newPrice) {
        double[] newArray = Arrays.copyOf(original, original.length + 1);
        newArray[original.length] = newPrice;
        return newArray;
    }

    /**
     * Shows the entire inventory with name, price, and stock.
     */
    private static String getInventoryString() {
        if (names.isEmpty()) {
            return "The inventory is empty.";
        }
        
        StringBuilder list = new StringBuilder("--- INVENTORY ---\n");
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            double price = prices[i];
            int currentStock = stock.getOrDefault(name.toLowerCase(), 0);
            
            list.append(String.format("Product: %s | Price: %s | Stock: %d\n", name, currencyFormatter.format(price), currentStock));
        }
        return list.toString();
    }
    
    private static void listInventory() {
        JOptionPane.showMessageDialog(null, getInventoryString(), "Inventory", JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * Allows the user to buy a product, validating existence and stock.
     */
    private static void buyProduct() {
        if (names.isEmpty()) {
            JOptionPane.showMessageDialog(null, "The inventory is empty. Cannot buy.");
            return;
        }
        
        String name = null;
        int index = -1;
        boolean productFound = false;

        // Loop to request the product name until it is valid
        while (!productFound) {
            String inventory = getInventoryString();
            name = JOptionPane.showInputDialog(null, inventory + "\n\nEnter the name of the product to buy:", "Buy Product", JOptionPane.PLAIN_MESSAGE);
            
            if (name == null) {
                return; // Exit the method if the user cancels
            }
            if (name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "The name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            index = indexOfName(name);
            if (index == -1) {
                JOptionPane.showMessageDialog(null, "The product '" + name + "' does not exist. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                productFound = true;
            }
        }
        
        int quantity = 0;
        String quantityStr;
        boolean validQuantity = false;
        while (!validQuantity) {
            quantityStr = JOptionPane.showInputDialog("Enter the quantity to buy:");
            if (quantityStr == null) return; // Exit if the user cancels

            try {
                quantity = Integer.parseInt(quantityStr);
                String realName = names.get(index);
                int currentStock = stock.getOrDefault(realName.toLowerCase(), 0);

                if (quantity > 0 && quantity <= currentStock) {
                    validQuantity = true;
                } else if (quantity <= 0) {
                    JOptionPane.showMessageDialog(null, "The quantity must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Insufficient stock. Available stock: " + currentStock, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid quantity. Please enter an integer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Confirm and update
        String realName = names.get(index);
        int confirmation = JOptionPane.showConfirmDialog(null, "Confirm the purchase of " + quantity + " of " + realName + "?", "Confirm Purchase", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            double price = prices[index];
            double subtotal = quantity * price;
            totalSales += subtotal;
            int currentStock = stock.get(realName.toLowerCase());
            stock.put(realName.toLowerCase(), currentStock - quantity);
            
            JOptionPane.showMessageDialog(null, "Purchase successful.\nSubtotal: $" + currencyFormatter.format(subtotal));
        } else {
            JOptionPane.showMessageDialog(null, "Purchase canceled.");
        }
    }

    /**
     * Finds the position of a product in the names ArrayList.
     */
    private static int indexOfName(String name) {
        String lowercaseName = name.toLowerCase();
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).toLowerCase().equals(lowercaseName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds and displays the cheapest and most expensive products.
     */
    private static void showStatistics() {
        if (prices.length == 0) {
            JOptionPane.showMessageDialog(null, "No products to show statistics for.");
            return;
        }
        
        double minPrice = prices[0];
        double maxPrice = prices[0];
        String minProduct = names.get(0);
        String maxProduct = names.get(0);
        
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] < minPrice) {
                minPrice = prices[i];
                minProduct = names.get(i);
            }
            if (prices[i] > maxPrice) {
                maxPrice = prices[i];
                maxProduct = names.get(i);
            }
        }
        
        String message = String.format("--- STATISTICS ---\nCheapest product: %s (%s)\nMost expensive product: %s (%s)", minProduct, currencyFormatter.format(minPrice), maxProduct, currencyFormatter.format(maxPrice));
        JOptionPane.showMessageDialog(null, message, "Price Statistics", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Allows searching for products by partial name matches.
     */
    private static void searchProduct() {
        String search = JOptionPane.showInputDialog("Enter the name (or part of it) of the product to search:");
        if (search == null || search.trim().isEmpty()) {
            return;
        }

        StringBuilder results = new StringBuilder("--- SEARCH RESULTS ---\n");
        boolean found = false;
        
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if (name.toLowerCase().contains(search.toLowerCase())) {
                double price = prices[i];
                int currentStock = stock.getOrDefault(name.toLowerCase(), 0);
                results.append(String.format("Product: %s | Price: %s | Stock: %d\n", name, currencyFormatter.format(price), currentStock));
                found = true;
            }
        }

        if (!found) {
            results.append("No products were found that match the search.");
        }
        
        JOptionPane.showMessageDialog(null, results.toString(), "Search Results", JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * Shows the accumulated total sales upon exiting.
     */
    private static void showFinalReceipt() {
        JOptionPane.showMessageDialog(null, "Thank you for using the Mini-Store.\nTotal accumulated sales: $" + currencyFormatter.format(totalSales), "Final Receipt", JOptionPane.INFORMATION_MESSAGE);
    }
}