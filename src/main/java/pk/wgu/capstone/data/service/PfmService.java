package pk.wgu.capstone.data.service;

import org.springframework.stereotype.Service;
import pk.wgu.capstone.data.entity.Category;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.repository.CategoryRepository;
import pk.wgu.capstone.data.repository.TransactionRepository;

import java.util.Arrays;
import java.util.List;

@Service
public class PfmService { // Personal Finance Management Service

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public PfmService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Transaction> findAllTransactions(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return transactionRepository.findAll();
        } else {
            return transactionRepository.search(filterText);
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

    public List<Type> findAllTypes() {
        return Arrays.asList(Type.values());
    }
}
