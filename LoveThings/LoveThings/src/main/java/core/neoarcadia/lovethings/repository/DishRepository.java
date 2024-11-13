package core.neoarcadia.lovethings.repository;

import core.neoarcadia.lovethings.models.Dish;
import core.neoarcadia.lovethings.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByUser(User user);
}
