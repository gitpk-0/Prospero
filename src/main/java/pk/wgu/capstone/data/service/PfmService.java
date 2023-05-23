package pk.wgu.capstone.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pk.wgu.capstone.data.entity.Category;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.entity.User;
import pk.wgu.capstone.data.repository.CategoryRepository;
import pk.wgu.capstone.data.repository.TransactionRepository;
import pk.wgu.capstone.data.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class PfmService { // Personal Finance Management Service

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public PfmService(TransactionRepository transactionRepository,
                      CategoryRepository categoryRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<Transaction> findAllTransactions(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return transactionRepository.findAll();
        } else {
            return transactionRepository.searchByDescription(filterText);
        }
    }

    public long countTransactions() {
        return transactionRepository.count();
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

    public long countTransactionsByType(Type type) {
        return transactionRepository.countByTransactionType(type);
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

    public Optional<User> findUserByEmail(String email) {
        return Optional.ofNullable(userRepository.findUserByEmail(email));
    }
}
