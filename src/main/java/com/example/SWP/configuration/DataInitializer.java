package com.example.SWP.configuration;

import com.example.SWP.entity.*;
import com.example.SWP.entity.escrow.Escrow;
import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.enums.*;
import com.example.SWP.repository.*;
import com.example.SWP.repository.escrow.EscrowRepository;
import com.example.SWP.repository.wallet.WalletRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataInitializer implements CommandLineRunner {

    final SellerPackageRepository packageRepository;
    final PriorityPackageRepository priorityPackageRepository;
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    final PostRepository postRepository;
    final OrderRepository orderRepository;
    final ContractRepository contractRepository;
    final InvoiceRepository invoiceRepository;
    final OrderDeliveryRepository orderDeliveryRepository;
    final WalletRepository walletRepository;
    final AppConfigRepository appConfigRepository;
    final EscrowRepository escrowRepository;

    @Value("${seller-package.basic.price}")
    BigDecimal basicPrice_sellerPackage;


    @Value("${seller-package.basic.maxPosts}")
    int basicMaxPosts_sellerPackage;

    @Value("${seller-package.premium.price}")
    BigDecimal premiumPrice_sellerPackage;

    @Value("${seller-package.premium.maxPosts}")
    int premiumMaxPosts_sellerPackage;

    @Value("${priority-package.basic.price}")
    BigDecimal basicPrice_priorityPackage;

    @Value("${priority-package.basic.durationDays}")
    int basicDurationDays_priorityPackage;

    @Value("${priority-package.premium.price}")
    BigDecimal premiumPrice_priorityPackage;

    @Value("${priority-package.premium.durationDays}")
    int premiumDurationDays_priorityPackage;

    @Value("${deposit-percentage}")
    BigDecimal depositPercentage;

    @Override
    public void run(String... args) {

        //Tao package
        if (packageRepository.count() == 0) {
            SellerPackage basic = SellerPackage.builder()
                    .type(SellerPackageType.BASIC)
                    .price(basicPrice_sellerPackage)
                    .description("Gói cơ bản tăng lượng đăng bài cho người bán")
                    .postLimit(basicMaxPosts_sellerPackage)
                    .build();

            SellerPackage premium = SellerPackage.builder()
                    .type(SellerPackageType.PREMIUM)
                    .price(premiumPrice_sellerPackage)
                    .description((("Gói Premium tăng lượng đăng bài cho người bán " +
                            "và có thể yêu cầu kiểm duyệt - nhán dãn, nhằm tăng uy tín cho sản phẩm")))
                    .postLimit(premiumMaxPosts_sellerPackage)
                    .build();

            packageRepository.saveAll(List.of(basic, premium));
            log.info("Seller packages (BASIC, PREMIUM) loaded from application.properties");
        } else {
            log.info("Seller packages already exist, skipping initialization");
        }

        if (priorityPackageRepository.count() == 0) {

            PriorityPackage basicPriority = PriorityPackage.builder()
                    .type(PriorityPackageType.BASIC)
                    .price(basicPrice_priorityPackage)
                    .durationDays(basicDurationDays_priorityPackage)
                    .build();

            PriorityPackage premiumPriority = PriorityPackage.builder()
                    .type(PriorityPackageType.PREMIUM)
                    .price(premiumPrice_priorityPackage)
                    .durationDays(premiumDurationDays_priorityPackage)
                    .build();

            priorityPackageRepository.saveAll(List.of(basicPriority, premiumPriority));
            log.info("Priority packages (BASIC, PREMIUM) loaded from application.properties");
        } else {
            log.info("Priority packages already exist, skipping initialization");
        }

        //Tao admin
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = User.builder()
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin@"))
                    .fullName("Admin")
                    .status(true)
                    .role(Role.ADMIN)
                    .provider(AuthProvider.MANUAL)
                    .build();

            userRepository.save(admin);
            log.warn("Admin user created with email 'admin@gmail.com' and password 'admin'. Please change the password after first login.");
        }

        //Test
        if (!userRepository.existsByRole(Role.BUYER)) {
            User user = User.builder()
                    .email("buyer@gmail.com")
                    .password(passwordEncoder.encode("buyer"))
                    .fullName("Buyer")
                    .status(true)
                    .role(Role.BUYER)
                    .provider(AuthProvider.MANUAL)
                    .address("tui hong co dia chi")
                    .streetAddress("tui hong co luon")
                    .provinceId(216)
                    .districtId(1724)
                    .wardCode("500718")
                    .phone("0386158643")
                    .build();

            userRepository.save(user);
            log.warn("Buyer user created with email 'buyer@gmail.com' and password 'buyer'. Please change the password after first login.");
        }

        //Test
        if (!userRepository.existsByRole(Role.SELLER)) {
            User user = User.builder()
                    .email("seller@gmail.com")
                    .password(passwordEncoder.encode("seller"))
                    .fullName("Seller")
                    .status(true)
                    .remainingBasicPosts(1000)
                    .remainingPremiumPosts(1000)
                    .address("292 to 9, Xã Thiện Trí, Huyện Cái Bè, Tiền Giang")
                    .streetAddress("292 to 9")
                    .provinceId(212)
                    .districtId(1900)
                    .wardCode("530324")
                    .phone("0837511721")
                    .storeName("Momo")
                    .storeDescription("Momo chan ga dai suki")
                    .socialMedia("momo.chan")
                    .role(Role.SELLER)
                    .provider(AuthProvider.MANUAL)
                    .build();

            userRepository.save(user);
            log.warn("Seller user created with email 'seller@gmail.com' and password 'seller'. Please change the password after first login.");
        }

        if (userRepository.findByEmail("minedungytb@gmail.com").isEmpty()) {
            User user = User.builder()
                    .email("minedungytb@gmail.com")
                    .password(passwordEncoder.encode("Dung1234*"))
                    .fullName("Dung Seller")
                    .status(true)
                    .remainingBasicPosts(1000)
                    .remainingPremiumPosts(1000)
                    .address("555, Phường Cầu Ông Lãnh, Quận 1, Hồ Chí Minh")
                    .streetAddress("555")
                    .provinceId(1921)
                    .districtId(1442)
                    .wardCode("710000")
                    .phone("0907771804")
                    .storeName("Momo")
                    .storeDescription("Momo chan ga dai suki")
                    .socialMedia("momo.chan")
                    .role(Role.SELLER)
                    .provider(AuthProvider.MANUAL)
                    .build();

            userRepository.save(user);
            log.warn("Seller user created with email 'minedungytb@gmail.com' and password 'Dung1234*'. Please change the password after first login.");
        }

        if (userRepository.findByEmail("minedung2005@gmail.com").isEmpty()) {
            User user = User.builder()
                    .email("minedung2005@gmail.com")
                    .password(passwordEncoder.encode("Dung1234*"))
                    .fullName("Zun Buyer")
                    .status(true)
                    .remainingBasicPosts(0)
                    .remainingPremiumPosts(0)
                    .address("234, Phường 6, Quận 3, Hồ Chí Minh")
                    .streetAddress("555")
                    .provinceId(1809)
                    .districtId(1440)
                    .wardCode("722700")
                    .phone("0935675866")
                    .storeName(null)
                    .storeDescription(null)
                    .socialMedia(null)
                    .role(Role.BUYER)
                    .provider(AuthProvider.MANUAL)
                    .build();

            userRepository.save(user);
            log.warn("Seller user created with email 'minedung2005@gmail.com' and password 'Dung1234*'. Please change the password after first login.");
        }


        // Demo order -> contract -> etc
        if (postRepository.count() == 0) {
            Optional<User> user = userRepository.findByEmail("seller@gmail.com");
            if (user.isEmpty()) {
                log.error("Seller user not found, cannot create posts.");
                return;
            }

            Set<DeliveryMethod> deliveryMethods = new HashSet<>(Arrays.asList(DeliveryMethod.values()));
            Set<PaymentType> paymentTypes = new HashSet<>(Arrays.asList(PaymentType.values()));

            Post post = Post.builder()
                    .user(user.get())
                    .productType(ProductType.VEHICLE)
                    .title("Umamusume")
                    .description("Umamusume are humanoid girls who possess horse traits and features. " +
                            "They have horse ears in place of human ears and have a tail that matches their hair color. " +
                            "They possess incredible speed and stamina, far beyond that of a human and comparable with that of a real-life horse.")
                    .price(new BigDecimal("1000000"))
                    .postDate(LocalDateTime.now())
                    .updateDate(null)
                    .expiryDate(LocalDateTime.now().plusDays(1))
                    .viewCount(0)
                    .likeCount(0)
                    .deliveryMethods(deliveryMethods)
                    .paymentTypes(paymentTypes)
                    .isTrusted(false)
                    .priorityPackageId(null)
                    .priorityExpire(null)
                    .status(PostStatus.POSTED)
                    .vehicleBrand("Narita")
                    .model("Narita Top Road")
                    .yearOfManufacture(2024)
                    .color("Yellow")
                    .mileage(100000)
                    .batteryType(null)
                    .capacity(null)
                    .voltage(null)
                    .batteryBrand(null)
                    .weight(null)
                    .build();
            postRepository.save(post);
            log.warn("Post created for demo purpose.");
        }

        if (postRepository.count() > 0) {
            Optional<Post> post = postRepository.getPostByVehicleBrand("Narita");
            if (post.isPresent()) {
                Post p = post.get();
                Optional<User> user = userRepository.findByEmail("minedung2005@gmail.com");
                if (user.isEmpty()) {
                    log.error("Buyer user not found, cannot create order.");
                    return;
                }
                int existedOrder = orderRepository.countOrderByPost_Id(p.getId());
                if (existedOrder > 0) {
                    log.warn("Order already exist for post with id " + p.getId() + ", skipping.");
                } else {
                    User u = user.get();
                    Order order = Order.builder()
                            .post(p)
                            .seller(p.getUser())
                            .buyer(u)
                            .deliveryMethod(DeliveryMethod.BUYER_PICKUP)
                            .paymentType(PaymentType.PLATFORM)
                            .serviceTypeId(null)
                            .shippingFee(BigDecimal.valueOf(200000.0))
                            .depositPercentage(null)
                            .wantDeposit(false)
                            .status(OrderStatus.APPROVED)
                            .createdAt(LocalDateTime.now().minusDays(7))
                            .build();
                    orderRepository.save(order);

                    BigDecimal totalFee = p.getPrice().add(order.getShippingFee());
                    Contract contract = Contract.builder()
                            .order(order)
                            .contractCode("UMA-0001")
                            .content("Just don't send her to glue factory")
                            .totalFee(totalFee)
                            .sellerSigned(true)
                            .buyerSigned(true)
                            .sellerSignedAt(LocalDateTime.now().plusDays(2))
                            .buyerSignedAt(LocalDateTime.now().plusDays(7))
                            .status(ContractStatus.SIGNED)
                            .build();
                    contractRepository.save(contract);

                    Invoice invoice = Invoice.builder()
                            .contract(contract)
                            .invoiceNumber("UMAPYOI-001")
                            .totalPrice(totalFee)
                            .createdAt(LocalDateTime.now().plusDays(7))
                            .dueDate(LocalDateTime.now().plusDays(14))
                            .paidAt(LocalDateTime.now().plusDays(12))
                            .status(InvoiceStatus.PAID)
                            .build();

                    invoiceRepository.save(invoice);

                    OrderDelivery orderDelivery = OrderDelivery.builder()
                            .order(order)
                            .deliveryProvider(null)
                            .deliveryTrackingNumber(null)
                            .deliveryDate(LocalDateTime.now().plusDays(14))
                            .status(DeliveryStatus.RECEIVED)
                            .createdAt(LocalDateTime.now().plusDays(7))
                            .build();

                    orderDeliveryRepository.save(orderDelivery);

                    Escrow escrow = Escrow.builder()
                            .depositAmount(BigDecimal.ZERO)
                            .order(order)
                            .paymentAmount(p.getPrice())
                            .createdAt(invoice.getPaidAt())
                            .sellerId(p.getUser().getId())
                            .buyerId(u.getId())
                            .status(EscrowStatus.LOCKED)
                            .build();

                    escrow.setTotalAmount(escrow.getDepositAmount().add(escrow.getPaymentAmount()));
                    escrowRepository.save(escrow);
                }

            } else {
                log.error("Post not found, cannot create order.");
                return;
            }
        }

        if (!walletRepository.existsByUser_Email("minedung2005@gmail.com")) {
            Optional<User> userOpt = userRepository.findByEmail("minedung2005@gmail.com");
            if (userOpt.isEmpty()) {
                log.error("User not found, cannot create wallet.");
                return;
            }
            User user = userOpt.get();
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .balance(BigDecimal.valueOf(9999999.0))
                    .build();

            walletRepository.save(wallet);
            log.warn("Wallet created for demo purpose.");
        } else {
            log.warn("Wallet already exist, skipping initialization");
        }

        if (!postRepository.existsByVehicleBrand("Mejiro")) {
            Optional<User> user = userRepository.findByEmail("seller@gmail.com");
            if (user.isEmpty()) {
                log.error("Seller user not found, cannot create posts.");
                return;
            }

            Set<DeliveryMethod> deliveryMethods = new HashSet<>(Arrays.asList(DeliveryMethod.values()));
            Set<PaymentType> paymentTypes = new HashSet<>(Arrays.asList(PaymentType.values()));

            Post post = Post.builder()
                    .user(user.get())
                    .productType(ProductType.VEHICLE)
                    .title("Umamusume")
                    .description("Umamusume are humanoid girls who possess horse traits and features. " +
                            "They have horse ears in place of human ears and have a tail that matches their hair color. " +
                            "They possess incredible speed and stamina, far beyond that of a human and comparable with that of a real-life horse.")
                    .price(new BigDecimal("1000000"))
                    .postDate(LocalDateTime.now())
                    .updateDate(null)
                    .expiryDate(LocalDateTime.now().plusDays(1))
                    .viewCount(0)
                    .likeCount(0)
                    .deliveryMethods(deliveryMethods)
                    .paymentTypes(paymentTypes)
                    .isTrusted(false)
                    .priorityPackageId(null)
                    .priorityExpire(null)
                    .status(PostStatus.POSTED)
                    .vehicleBrand("Mejiro")
                    .model("Mejiro Mcqueen")
                    .yearOfManufacture(1987)
                    .color("Purple")
                    .mileage(100000)
                    .batteryType(null)
                    .capacity(null)
                    .voltage(null)
                    .batteryBrand(null)
                    .weight(null)
                    .build();
            postRepository.save(post);
            log.warn("Post Mejiro created for demo purpose.");
        } else {
            log.warn("Post Mejiro already exist, skipping initialization");
        }

        if (!appConfigRepository.existsByConfigKey("DEPOSIT_PERCENTAGE_KEY")) {

            AppConfig depositConfig = AppConfig.builder()
                    .configKey("DEPOSIT_PERCENTAGE_KEY")
                    .configValue("0.20")
                    .description("Tỉ lệ phần trăm đặt cọc bắt buộc cho các giao dịch.")
                    .build();

            appConfigRepository.save(depositConfig);
        }
    }
}
