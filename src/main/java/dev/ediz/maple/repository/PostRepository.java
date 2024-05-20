package dev.ediz.maple.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import dev.ediz.maple.model.Post;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {


}
