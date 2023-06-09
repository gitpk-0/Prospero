package pk.wgu.capstone.data.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pk.wgu.capstone.data.entity.*;
import pk.wgu.capstone.data.repository.BudgetRepository;
import pk.wgu.capstone.data.repository.CategoryRepository;
import pk.wgu.capstone.data.repository.TransactionRepository;
import pk.wgu.capstone.data.repository.UserRepository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;

@Service
@EnableTransactionManagement
public class PfmService { // Personal Finance Management Service

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    @Autowired
    public PfmService(TransactionRepository transactionRepository,
                      CategoryRepository categoryRepository, UserRepository userRepository,
                      BudgetRepository budgetRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
    }

    public List<Transaction> findAllTransactions(Long userId, String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return transactionRepository.findAllByUserId(userId);
        } else {
            return transactionRepository.searchByUserIdAndDescription(userId, filterText);
        }
    }

    public long countTransactionsByUser(Long userId) {
        return transactionRepository.countByUserId(userId);
    }

    public void deleteTransaction(Transaction transaction) {
        transactionRepository.delete(transaction);
    }

    public void saveTransaction(Transaction transaction) {
        if (transaction == null) {
            System.out.println("Transaction is null");
            return;
        }
        transactionRepository.save(transaction);
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public long countTransactionsByType(Long userId, Type type) {
        return transactionRepository.countByTransactionType(userId, type);
    }

    public List<Type> findAllTypes() {
        return Arrays.asList(Type.values());
    }

    public void addNewUser(User user) {
        userRepository.save(user);
    }

    public boolean userExists(String email) {
        return userRepository.findUserCountByEmail(email) > 0;
    }

    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public User findUserById(Long userId) {
        return userRepository.findUserById(userId);
    }

    public BigDecimal sumAllTransactionsByType(Long userId, Type type) {
        return transactionRepository.sumAllTransactionsByType(userId, type);
    }

    public List<Object[]> sumTransactionByCategory(Long userId, Type type) {
        return transactionRepository.sumTransactionsByCategory(userId, type);
    }

    public void addNewCategory(Category category) {
        categoryRepository.save(category);
    }

    public Category findCategoryByName(String categoryName) {
        return categoryRepository.findCategoryByName(categoryName);
    }

    @Transactional
    public void updateCustomCategoryUserIds(Long categoryId, String userId) {
        categoryRepository.updateCustomCategoryUserIds(categoryId, userId);
    }

    public List<pk.wgu.capstone.data.entity.Budget> getBudgetsByUserId(Long userId) {
        return budgetRepository.getBudgetsByUserId(userId);
    }

    public void saveBudget(Budget budget) {
        if (budget == null) {
            System.out.println("Budget is null");
            return;
        }
        budgetRepository.save(budget);
    }

    public void deleteBudget(Budget budget) {
        budgetRepository.delete(budget);
    }

    public BigDecimal getSumExpensesInDateRange(Date start, Date end, Type type, Long userId) {
        return transactionRepository.getSumExpensesInDateRange(start, end, type, userId);
    }

    public List<Object[]> sumTransactionsInDateRangeByCategory(Long userId, Type type, Date start, Date end) {
        return transactionRepository.sumTransactionsInDateRangeByCategory(userId, type, start, end);
    }
}
