package co.com.pragma.r2dbc;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


public interface MyReactiveRepository extends ReactiveCrudRepository<Object, String>, ReactiveQueryByExampleExecutor<Object> {

}
