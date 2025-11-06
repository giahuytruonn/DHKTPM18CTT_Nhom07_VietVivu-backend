package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tourbooking.vietvivu.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, String> {}
