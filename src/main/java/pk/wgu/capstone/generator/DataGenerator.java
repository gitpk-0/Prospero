package pk.wgu.capstone.generator;

import com.vaadin.flow.spring.annotation.SpringComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pk.wgu.capstone.data.entity.*;
import pk.wgu.capstone.data.repository.BudgetRepository;
import pk.wgu.capstone.data.repository.CategoryRepository;
import pk.wgu.capstone.data.repository.TransactionRepository;
import pk.wgu.capstone.data.repository.UserRepository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(TransactionRepository transactionRepository,
                                      CategoryRepository categoryRepository,
                                      UserRepository userRepository,
                                      BudgetRepository budgetRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());

            if (transactionRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }

            logger.info("Generating demo data:");
            logger.info("Generating categories...");
            List<Category> categories = new ArrayList<>();
            // default income categories
            categories.add(new Category("Paycheck", Type.INCOME)); // 0
            categories.add(new Category("Bonus", Type.INCOME));
            categories.add(new Category("Savings", Type.INCOME));
            categories.add(new Category("Investment", Type.INCOME));
            categories.add(new Category("Commission", Type.INCOME));
            categories.add(new Category("Retirement", Type.INCOME));
            categories.add(new Category("Social Security", Type.INCOME));
            categories.add(new Category("Rental Property", Type.INCOME));
            categories.add(new Category("Gift", Type.INCOME));
            categories.add(new Category("Other", Type.INCOME)); // 9
            // default expense categories
            categories.add(new Category("Rent", Type.EXPENSE)); // 10
            categories.add(new Category("Car", Type.EXPENSE));
            categories.add(new Category("Food", Type.EXPENSE));
            categories.add(new Category("Medical", Type.EXPENSE));
            categories.add(new Category("Gift", Type.EXPENSE));
            categories.add(new Category("Utilities", Type.EXPENSE));
            categories.add(new Category("Travel", Type.EXPENSE));
            categories.add(new Category("Transportation", Type.EXPENSE));
            categories.add(new Category("Entertainment", Type.EXPENSE));
            categories.add(new Category("Other", Type.EXPENSE)); // 19
            categories.forEach(category -> category.setDefault(true));
            categories.forEach(category -> category.setUserIdsCsv("default"));


            List<Type> types = Arrays.asList(Type.values());
            logger.info("Types: ");
            types.forEach(type -> logger.info(type.toString()));

            List<User> users = new ArrayList<>();
            User user1 = new User();
            user1.setFirstName("user1");
            user1.setLastName("Kell");
            user1.setEmail("patrick.kell1@pm.me");
            user1.setPassword(passwordEncoder().encode("easypass"));
            user1.setAllowsMarketingEmails(true);
            user1.setId(1151L);
            user1.setRole(Role.USER);

            User user2 = new User();
            user2.setFirstName("user2");
            user2.setLastName("Kell");
            user2.setEmail("patrick.kell11@pm.me");
            user2.setPassword(passwordEncoder().encode("easypass1"));
            user2.setAllowsMarketingEmails(true);
            user2.setId(1152L);
            user2.setRole(Role.USER);

            User user3 = new User();
            user3.setFirstName("user3");
            user3.setLastName("Kell");
            user3.setEmail("patrick.kell111@pm.me");
            user3.setPassword(passwordEncoder().encode("easypass11"));
            user3.setAllowsMarketingEmails(true);
            user3.setId(1153L);
            user3.setRole(Role.USER);

            users.add(user1);
            users.add(user2);
            users.add(user3);

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
                    transaction.setCategory(categories.get(r.nextInt(10)));
                } else {
                    transaction.setDescription(randomExpenseDescription());
                    transaction.setCategory(categories.get(r.nextInt(categories.size() - 10) + 10));
                }

                logger.info(transaction.toString());
                transactions.add(transaction);
            }

            List<Budget> budgets = new ArrayList<>();
            budgets.add(new Budget(
                    "June Budget - oldest",
                    Date.valueOf(LocalDate.now().minusDays(1)),
                    Date.valueOf(LocalDate.now().plusDays(3)),
                    BigDecimal.valueOf(500L),
                    "first budget",
                    user1.getId()
            ));


            budgets.add(new Budget(
                    "July Budget - mid",
                    Date.valueOf(LocalDate.now().plusMonths(1)),
                    Date.valueOf(LocalDate.now().plusMonths(1).plusDays(3)),
                    BigDecimal.valueOf(700L),
                    "second budget",
                    user1.getId()
            ));

            budgets.add(new Budget(
                    "August Budget - youngest",
                    Date.valueOf(LocalDate.now().plusMonths(2)),
                    Date.valueOf(LocalDate.now().plusMonths(2).plusDays(3)),
                    BigDecimal.valueOf(900L),
                    "third budget",
                    user1.getId()
            ));

            int i = 0;
            for (Budget b : budgets) {
                b.setDateCreated(LocalDateTime.now().minusDays(50).plusDays(i));
                i++;
                i++;
            }

            budgetRepository.saveAll(budgets);
            categoryRepository.saveAll(categories);
            transactionRepository.saveAll(transactions);
            userRepository.saveAll(users);


            logger.info("Generated demo data");
        };
    }


    static Date generateRandomDate() {
        LocalDate minDay = LocalDate.now().minusYears(2);
        LocalDate maxDay = LocalDate.now();

        long minEpochDay = minDay.toEpochDay();
        long maxEpochDay = maxDay.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minEpochDay, maxEpochDay + 1);

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

    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
