// package tourbooking.vietvivu;
//
// import lombok.AccessLevel;
// import lombok.AllArgsConstructor;
// import lombok.RequiredArgsConstructor;
// import lombok.experimental.FieldDefaults;
// import lombok.extern.slf4j.Slf4j;
// import net.datafaker.Faker;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;
// import tourbooking.vietvivu.entity.*;
// import tourbooking.vietvivu.repository.*;
//
// import java.time.LocalDate;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Random;
//
// @Component
// @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
// @Slf4j
// public class GenData implements CommandLineRunner {
//    UserRepository userRepository;
//    TourRepository tourRepository;
//    RoleRepository roleRepository;
//    ReviewRepository reviewRepository;
//    PromotionRepository promotionRepository;
//    PermissionRepository permissionRepository;
//    MessageRepository messageRepository;
//    InvoiceRepository invoiceRepository;
//    ImageRepository imageRepository;
//    HistoryRepository historyRepository;
//    ConversationRepository conversationRepository;
//    CheckoutRepository checkoutRepository;
//    BookingRepository bookingRepository;
//
//    public GenData(UserRepository userRepository,
//                   TourRepository tourRepository,
//                   RoleRepository roleRepository,
//                   ReviewRepository reviewRepository,
//                   PromotionRepository promotionRepository,
//                   PermissionRepository permissionRepository,
//                   MessageRepository messageRepository,
//                   InvoiceRepository invoiceRepository,
//                   ImageRepository imageRepository,
//                   HistoryRepository historyRepository,
//                   ConversationRepository conversationRepository,
//                   CheckoutRepository checkoutRepository,
//                   BookingRepository bookingRepository) {
//        this.userRepository = userRepository;
//        this.tourRepository = tourRepository;
//        this.roleRepository = roleRepository;
//        this.reviewRepository = reviewRepository;
//        this.promotionRepository = promotionRepository;
//        this.permissionRepository = permissionRepository;
//        this.messageRepository = messageRepository;
//        this.invoiceRepository = invoiceRepository;
//        this.imageRepository = imageRepository;
//        this.historyRepository = historyRepository;
//        this.conversationRepository = conversationRepository;
//        this.checkoutRepository = checkoutRepository;
//        this.bookingRepository = bookingRepository;
//    }
//
//    Faker faker = new Faker();
//    Random random = new Random();
//
//    @Override
//    public void run(String... args) throws Exception {
//        seedsPermissions();
//        seedRoles();
//        seedUsers();
//        seedTours();
//        seedPromotions();
//        seedsBooking();
//        seedImages();
//        seedReviews();
//        seedConversations();
//        seedMessages();
//        seedsCheckouts();
//        seedInvoices();
//        seedHistory();
//    }
//
//    private void seedsPermissions() {
//        if(permissionRepository.count() == 0) {
//            permissionRepository.saveAll(List.of(
//                    new Permission("READ", "Permission to read tour details"),
//                    new Permission("CREATE", "Permission to create a new tour"),
//                    new Permission("UPDATE", "Permission to update an existing tour"),
//                    new Permission("DELETE", "Permission to delete a tour")
//            ));
//            System.out.println("Permissions seeded successfully!");
//        }
//    }
//
//    private void seedRoles() {
//        if(roleRepository.count() == 0) {
//            var permissions = permissionRepository.findAll();
//
//            Role adminRole = new Role("ADMIN", "Administrator role with full permissions");
//            adminRole.setPermissions(new HashSet<>(permissions));
//            roleRepository.saveAll(List.of(
//                    adminRole
//            ));
//            System.out.println("Roles seeded successfully!");
//        }
//    }
//
//    private void seedUsers(){
//        if(userRepository.count() == 0){
//            for(int i = 0; i < 10; i++) {
//                User user = new User();
//                user.setUsername(faker.name().username());
//                user.setPassword(faker.internet().password());
//                user.setName(faker.name().firstName());
//                user.setEmail(faker.internet().emailAddress());
//                user.setAddress(faker.address().fullAddress());
//                user.setPhoneNumber(faker.phoneNumber().phoneNumber());
//                user.setIsActive(random.nextBoolean());
//                user.setCreatedDate(LocalDate.now());
//                user.setRoles(new HashSet<>(roleRepository.findAll()));
//
//                userRepository.save(user);
//            }
//            System.out.println("Users seeded successfully!");
//        }
//    }
//
//    private void seedTours() {
//        if(tourRepository.count() == 0) {
//            for (int i = 0; i < 10; i++) {
//                Tour tour = new Tour();
//                tour.setTitle(faker.lorem().sentence(3));
//                tour.setDescription(faker.lorem().paragraph());
//                tour.setQuantity(random.nextInt(1, 100));
//                tour.setPriceAdult(random.nextDouble(100, 1000));
//                tour.setPriceChild(random.nextDouble(50, 500));
//                tour.setDuration("3N-2D");
//                tour.setDestination(faker.address().cityName());
//                tour.setAvailability(random.nextBoolean());
//                tour.setItinerary(List.of(
//                        "Day 1: " + faker.lorem().sentence(5),
//                        "Day 2: " + faker.lorem().sentence(5),
//                        "Day 3: " + faker.lorem().sentence(5)
//                ));
//
//                tourRepository.save(tour);
//            }
//            System.out.println("Tours seeded successfully!");
//        }
//    }
//
//
//
//    private void seedsBooking() {
//        if(bookingRepository.count() == 0) {
//            for (int i = 0; i < 3; i++) {
//                Booking booking = new Booking();
//                booking.setUser(userRepository.findAll().get(1));
//                booking.setTour(tourRepository.findAll().get(1));
//                booking.setBookingDate(LocalDate.now());
//                booking.setNumAdults(random.nextInt(1, 5));
//                booking.setNumChildren(random.nextInt(0, 3));
//                booking.setTotalPrice(booking.getNumAdults() * booking.getTour().getPriceAdult() +
//                        booking.getNumChildren() * booking.getTour().getPriceChild());
//                booking.setPaymentStatus(random.nextBoolean() ? "PAID" : "PENDING");
//                booking.setBookingStatus(random.nextBoolean() ? "CONFIRMED" : "CANCELLED");
//
//
//                bookingRepository.save(booking);
//            }
//            System.out.println("Bookings seeded successfully!");
//        }
//    }
//
//    private void seedImages() {
//        if(imageRepository.count() == 0) {
//            for (int i = 0; i < 10; i++) {
//                Image image = new Image();
//                image.setImageUrl(faker.internet().image());
//                image.setTour(tourRepository.findAll().get(1));
//                image.setDescription(faker.lorem().sentence(5));
//                image.setUploadDate(LocalDate.now());
//                imageRepository.save(image);
//            }
//            System.out.println("Images seeded successfully!");
//        }
//    }
//
//    private void seedReviews() {
//        if(reviewRepository.count() == 0) {
//            for (int i = 0; i < 1; i++) {
//                Review review = new Review();
//                review.setUser(userRepository.findAll().get(1));
//                review.setTour(tourRepository.findAll().get(1));
//                review.setBooking(bookingRepository.findAll().get(0));
//                review.setRating(random.nextInt(1, 6));
//                review.setComment(faker.lorem().paragraph());
//                review.setTimestamp(LocalDate.now());
//
//                reviewRepository.save(review);
//            }
//            System.out.println("Reviews seeded successfully!");
//        }
//    }
//
//    private void seedPromotions() {
//        if(promotionRepository.count() == 0) {
//            for (int i = 0; i < 5; i++) {
//                Promotion promotion = new Promotion();
//                promotion.setPromotionId(faker.idNumber().valid());
//                promotion.setDescription(faker.lorem().sentence(5));
//                promotion.setDiscount(random.nextDouble(5, 50));
//                promotion.setStartDate(LocalDate.now());
//                promotion.setEndDate(LocalDate.now().plusDays(random.nextInt(1, 30)));
//                promotion.setStatus(true);
//                promotion.setQuantity(random.nextInt(1, 100));
//
//
//                promotionRepository.save(promotion);
//            }
//            System.out.println("Promotions seeded successfully!");
//        }
//    }
//
//    private void seedConversations() {
//        if(conversationRepository.count() == 0) {
//            for (int i = 0; i < 5; i++) {
//                Conversation conversation = new Conversation();
//                conversation.setUser(userRepository.findAll().get(1));
//                conversation.setAdmin(userRepository.findAll().get(0));
//                conversation.setReplyStatus(false);
//                conversation.setCreatedDate(LocalDate.now());
//
//                conversationRepository.save(conversation);
//            }
//            System.out.println("Conversations seeded successfully!");
//        }
//    }
//
//    private void seedMessages() {
//        if(messageRepository.count() == 0) {
//            for (int i = 0; i < 10; i++) {
//                Message message = new Message();
//                message.setSender(userRepository.findAll().get(1));
//                message.setConversation(conversationRepository.findAll().get(1));
//                message.setContent(faker.lorem().sentence(10));
//                message.setCreatedDate(LocalDate.now());
//
//                messageRepository.save(message);
//            }
//            System.out.println("Messages seeded successfully!");
//        }
//    }
//
//    private void seedsCheckouts() {
//        if(checkoutRepository.count() == 0) {
//            for (int i = 0; i < 1; i++) {
//                Checkout checkout = new Checkout();
//                checkout.setBooking(bookingRepository.findAll().get(0));
//                checkout.setPaymentMethod("CREDIT_CARD");
//                checkout.setPaymentDate(LocalDate.now());
//                checkout.setAmount(bookingRepository.findAll().get(0).getTotalPrice());
//                checkout.setPaymentStatus(random.nextBoolean() ? "SUCCESS" : "FAILED");
//                checkout.setTransactionId(faker.idNumber().valid());
//
//                checkoutRepository.save(checkout);
//            }
//            System.out.println("Checkouts seeded successfully!");
//        }
//    }
//
//    private void seedInvoices() {
//        if(invoiceRepository.count() == 0) {
//            for (int i = 0; i < 1; i++) {
//                Invoice invoice = new Invoice();
//                invoice.setBooking(bookingRepository.findAll().get(0));
//                invoice.setCheckout(checkoutRepository.findAll().get(0));
//                invoice.setAmount(bookingRepository.findAll().get(0).getTotalPrice());
//                invoice.setDateIssued(LocalDate.now());
//                invoice.setDetails("Invoice for booking ID: " + bookingRepository.findAll().get(1).getBookingId());
//
//                invoiceRepository.save(invoice);
//            }
//            System.out.println("Invoices seeded successfully!");
//        }
//    }
//
//    private void seedHistory() {
//        if(historyRepository.count() == 0) {
//            for (int i = 0; i < 10; i++) {
//                History history = new History();
//                history.setUser(userRepository.findAll().get(1));
//                history.setTourId(tourRepository.findAll().get(1).getTourId());
//                history.setInvalidToken(faker.lorem().word());
//                history.setActionType("VIEWED");
//                history.setTimestamp(LocalDate.now());
//                history.setExpiryTime(LocalDate.now().plusDays(random.nextInt(1, 30)));
//
//                historyRepository.save(history);
//            }
//            System.out.println("History seeded successfully!");
//        }
//    }
// }
