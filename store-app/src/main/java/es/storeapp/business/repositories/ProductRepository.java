package es.storeapp.business.repositories;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.stereotype.Repository;

import es.storeapp.business.entities.Product;
import jakarta.persistence.Query;

@Repository
public class ProductRepository extends AbstractRepository<Product> {
    
    private static final String FIND_BY_CATEGORY_QUERY = 
            "SELECT p FROM Product p WHERE p.category.name = :category ORDER BY p.price";
    
    @SuppressWarnings("unchecked")
    public List<Product> findByCategory(String category, String orderColumn) {
        Query query = entityManager.createQuery(MessageFormat.format(FIND_BY_CATEGORY_QUERY, 
                category, orderColumn));
        query.setParameter("category", category);
        return (List<Product>) query.getResultList();
    }
    
}
