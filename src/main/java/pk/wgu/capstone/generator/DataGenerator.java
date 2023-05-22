package pk.wgu.capstone.generator;

import com.vaadin.flow.spring.annotation.SpringComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import pk.wgu.capstone.data.entity.Category;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.entity.User;
import pk.wgu.capstone.data.repository.CategoryRepository;
import pk.wgu.capstone.data.repository.TransactionRepository;
import pk.wgu.capstone.data.repository.UserRepository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(TransactionRepository transactionRepository,
                                      CategoryRepository categoryRepository, UserRepository userRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());

            if (transactionRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }

            logger.info("Generating demo data:");
            logger.info("Generating categories...");
            List<Category> categories = new ArrayList<>();
            categories.add(new Category("Income"));
            categories.add(new Category("Rent"));
            categories.add(new Category("Car"));
            categories.add(new Category("Food"));
            categories.add(new Category("Gas"));
            categories.add(new Category("Entertainment"));
            categories.add(new Category("Other"));


            List<Type> types = Arrays.asList(Type.values());
            logger.info("Types: ");
            types.forEach(type -> logger.info(type.toString()));

            List<User> users = new ArrayList<>();
            User newUser = new User();
            newUser.setFirstName("Patrick");
            newUser.setLastName("Kell");
            newUser.setEmail("patrick.kell1@pm.me");
            newUser.setPassword("easypass");
            newUser.setAllowsMarketingEmails(true);
            newUser.setId(1101L);
            users.add(newUser);



            Random r = new Random();

            logger.info("Generating transactions...");
            List<Transaction> transactions = new ArrayList<>();
            for (int i = 0; i < 40; i++) {
                Transaction transaction = new Transaction();
                transaction.setDate(generateRandomDate());
                transaction.setAmount(BigDecimal.valueOf(r.nextDouble(1200)));
                transaction.setType(types.get(r.nextInt(types.size())));
                transaction.setUserId(users.get(0).getId());
                if (transaction.getType().equals(Type.INCOME)) {
                    transaction.setDescription(randomIncomeDescription());
                    transaction.setCategory(categories.get(0));
                } else {
                    transaction.setDescription(randomExpenseDescription());
                    transaction.setCategory(categories.get(r.nextInt(categories.size() - 1) + 1)); // exclude index 0
                }

                logger.info(transaction.toString());
                transactions.add(transaction);
            }

            categoryRepository.saveAll(categories);
            transactionRepository.saveAll(transactions);
            userRepository.saveAll(users);


            logger.info("Generated demo data");
        };
    }


    static Date generateRandomDate() {
        LocalDate minDay = LocalDate.now().minusYears(1);
        LocalDate maxDay = LocalDate.now().plusYears(1);

        long minEpochDay = minDay.toEpochDay();
        long maxEpochDay = maxDay.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minEpochDay, maxEpochDay + 1);

        // System.out.println("Date+++ " + Date.valueOf(LocalDate.ofEpochDay(randomDay)));

        return Date.valueOf(LocalDate.ofEpochDay(randomDay));
    }

    static String randomExpenseDescription() {
        String[] expenseDescriptions = {
                "Groceries", "Rent", "Utilities", "Transportation",
                "Dining Out", "Clothing", "Entertainment", "Gym Membership",
                "Insurance", "Phone Bill", "Internet Bill", "Car Maintenance",
                "Travel Expenses", "Medical Expenses", "Education Fees", "Home Repairs",
                "Pet Supplies", "Hobbies", "Charity Donations", "Subscriptions",
                "Coffee", "Fast Food", "Gifts", "Haircuts",
                "Laundry", "Office Supplies", "Parking Fees", "Public Transport",
                "Books", "Movies", "Concert Tickets", "Sports Equipment",
                "Taxi Rides", "Car Rental", "Fitness Classes", "Takeout",
                "Grocery Delivery", "Online Shopping", "Netflix Subscription", "Music Streaming",
                "Restaurant Delivery", "Home Decor", "Magazine Subscription", "Home Cleaning"
        };

        Random random = new Random();
        int randomIndex = random.nextInt(expenseDescriptions.length);
        return expenseDescriptions[randomIndex];
    }

    static String randomIncomeDescription() {
        String[] incomeDescriptions = {"Salary", "Bonus", "Dividends", "Rental Income"};

        Random random = new Random();
        int randomIndex = random.nextInt(incomeDescriptions.length);
        return incomeDescriptions[randomIndex];
    }
}
