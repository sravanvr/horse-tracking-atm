import java.util.*;

public class HorseTrackingATM {
    private Map<Integer, Bill> inventory;
    private List<Horse> horses;
    private int winningHorseNumber;
    private static final String WHITESPACE_PATTERN = "\\s+";

    public HorseTrackingATM() {
        initializeInventory();
        initializeHorses();
        // Default winning horse
        winningHorseNumber = 1;
    }

    private void initializeInventory() {
        inventory = new LinkedHashMap<>();
        inventory.put(1, new Bill(1, 10));
        inventory.put(5, new Bill(5, 10));
        inventory.put(10, new Bill(10, 10));
        inventory.put(20, new Bill(20, 10));
        inventory.put(100, new Bill(100, 10));
    }

    private void initializeHorses() {
        horses = new ArrayList<>();
        horses.add(new Horse(1, "That Darn Gray Cat", 5));
        horses.add(new Horse(2, "Fort Utopia", 10));
        horses.add(new Horse(3, "Count Sheep", 9));
        horses.add(new Horse(4, "Ms Traitour", 4));
        horses.add(new Horse(5, "Real Princess", 3));
        horses.add(new Horse(6, "Pa Kettle", 5));
        horses.add(new Horse(7, "Gin Stinger", 6));

        // Set initial winning horse
        updateWinningHorse(1);
    }

    public void restockInventory() {
        for (Bill bill : inventory.values()) {
            bill.setQuantity(10);
        }
    }

    public void updateWinningHorse(int horseNumber) {
        if (horseNumber < 1 || horseNumber > horses.size()) {
            System.out.println("Invalid Horse Number: " + horseNumber);
            return;
        }

        for (Horse horse : horses) {
            horse.setWinner(horse.getNumber() == horseNumber);
        }

        winningHorseNumber = horseNumber;
    }

    public void processBet(int horseNumber, double betAmount) {
        // Check if horse number is valid
        if (horseNumber < 1 || horseNumber > horses.size()) {
            System.out.println("Invalid Horse Number: " + horseNumber);
            displayStatus();
            return;
        }

        // Check if bet amount is valid - must be a whole dollar amount
        if (betAmount != Math.floor(betAmount) || betAmount <= 0) {
            System.out.println("Invalid Bet: " + betAmount);
            displayStatus();
            return;
        }

        Horse selectedHorse = getHorseByNumber(horseNumber);

        // Check if selected horse won
        if (!selectedHorse.isWinner()) {
            System.out.println("No Payout: " + selectedHorse.getName());
            displayStatus();
            return;
        }

        // Calculate winnings
        int winnings = (int) (betAmount * selectedHorse.getOdds());

        // Try to dispense winnings
        Map<Integer, Integer> payoutBills = calculatePayout(winnings);

        if (payoutBills == null) {
            System.out.println("Insufficient Funds: " + winnings);
            displayStatus();
            return;
        }

        // Process payout and update inventory
        System.out.println("Payout: " + selectedHorse.getName() + ",$" + winnings);
        System.out.println("Dispensing:");

        for (int denomination : new int[]{1, 5, 10, 20, 100}) {
            int count = payoutBills.getOrDefault(denomination, 0);
            System.out.println("$" + denomination + "," + count);

            // Update inventory
            Bill bill = inventory.get(denomination);
            bill.setQuantity(bill.getQuantity() - count);
        }

        displayStatus();
    }

    private Map<Integer, Integer> calculatePayout(int amount) {
        Map<Integer, Integer> result = new HashMap<>();
        int[] denominations = {100, 20, 10, 5, 1};
        int remainingAmount = amount;

        for (int denomination : denominations) {
            Bill bill = inventory.get(denomination);
            int billsNeeded = remainingAmount / denomination;
            int billsToUse = Math.min(billsNeeded, bill.getQuantity());

            if (billsToUse > 0) {
                result.put(denomination, billsToUse);
                remainingAmount -= billsToUse * denomination;
            }
        }

        // If there's still an amount remaining, we don't have enough bills
        if (remainingAmount > 0) {
            return null;
        }

        return result;
    }

    private Horse getHorseByNumber(int number) {
        for (Horse horse : horses) {
            if (horse.getNumber() == number) {
                return horse;
            }
        }
        return null;
    }

    public void displayStatus() {
        // Display inventory
        System.out.println("Inventory:");
        for (int denomination : new int[]{1, 5, 10, 20, 100}) {
            Bill bill = inventory.get(denomination);
            System.out.println("$" + bill.getDenomination() + "," + bill.getQuantity());
        }

        // Display horses
        System.out.println("Horses:");
        for (Horse horse : horses) {
            System.out.println(horse.getNumber() + "," + horse.getName() + "," +
                    horse.getOdds() + "," + (horse.isWinner() ? "won" : "lost"));
        }
    }

    /**
     * 1. Parse the input
     * 2. Handle commands R, Q, W, and bet commands.
     *
     */
    public static void main(String[] args) {
        HorseTrackingATM machine = new HorseTrackingATM();
        machine.displayStatus();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            // Process commands
            if (input.equalsIgnoreCase("R")) {
                machine.restockInventory();
                machine.displayStatus();
            } else if (input.equalsIgnoreCase("Q")) {
                break;
            } else if (input.toLowerCase().startsWith("w")) {
                String[] parts = input.split(WHITESPACE_PATTERN);
                if (parts.length == 2) {
                    try {
                        int horseNumber = Integer.parseInt(parts[1]);
                        machine.updateWinningHorse(horseNumber);
                        machine.displayStatus();
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid Command: " + input);
                        machine.displayStatus();
                    }
                } else {
                    System.out.println("Invalid Command: " + input);
                    machine.displayStatus();
                }
            } else {
                // Try to parse as bet
                String[] parts = input.split(WHITESPACE_PATTERN);
                if (parts.length == 2) {
                    try {
                        int horseNumber = Integer.parseInt(parts[0]);
                        double betAmount = Double.parseDouble(parts[1]);
                        machine.processBet(horseNumber, betAmount);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid Command: " + input);
                        machine.displayStatus();
                    }
                } else {
                    System.out.println("Invalid Command: " + input);
                    machine.displayStatus();
                }
            }
        }

        scanner.close();
    }
}

// Domain models - Manage these in required data structures
class Bill {
    private int denomination;
    private int quantity;

    public Bill(int denomination, int quantity) {
        this.denomination = denomination;
        this.quantity = quantity;
    }

    public int getDenomination() {
        return denomination;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

class Horse {
    private int number;
    private String name;
    private int odds;
    private boolean winner;

    public Horse(int number, String name, int odds) {
        this.number = number;
        this.name = name;
        this.odds = odds;
        this.winner = false;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public int getOdds() {
        return odds;
    }

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }
}