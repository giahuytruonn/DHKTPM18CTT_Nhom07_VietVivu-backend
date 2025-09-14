package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tourbooking.vietvivu.entity.InvalidatedToken;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {}
