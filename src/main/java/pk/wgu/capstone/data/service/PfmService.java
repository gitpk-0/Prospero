package pk.wgu.capstone.data.service;

import org.springframework.security.core.parameters.P;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.repository.CategoryRepository;
import pk.wgu.capstone.data.repository.TransactionRepository;

import java.util.List;

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
}
