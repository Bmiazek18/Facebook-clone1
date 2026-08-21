package com.facebook.marketplace.config;

import com.facebook.marketplace.model.Listing;
import com.facebook.marketplace.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final ListingRepository listingRepository;
    private final com.facebook.marketplace.service.ListingService listingService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking if marketplace initial seed is needed...");
        if (listingRepository.count() == 0) {
            log.info("Seeding initial test listings...");

            Listing l1 = createListing("2001 BMW series 7 E38 Individual", new BigDecimal("55900"), "Vehicles", "USED", "Piękne, klasyczne BMW E38 w wersji Individual.", 52.0689, 19.3824);
            Listing l2 = createListing("2011 BMW 5 series f10 f11 xDrive", new BigDecimal("40000"), "Vehicles", "USED", "BMW F10 xDrive w dobrym stanie technicznym.", 52.2297, 21.0122);
            Listing l3 = createListing("Acer Nitro 5 - RTX 3060, 16GB RAM", new BigDecimal("1450"), "Electronics", "USED", "Laptop gamingowy w świetnym stanie, 100% sprawny.", 52.0689, 19.3824);
            Listing l4 = createListing("Xiaomi Redmi Note 13 Pro 5G", new BigDecimal("750"), "Electronics", "NEW", "Fabrycznie nowy telefon Xiaomi, z polskiej dystrybucji.", 52.0689, 19.3824);
            Listing l5 = createListing("Dom 70 m2 na zgłoszenie całoroczny", new BigDecimal("99999"), "Property", "NEW", "Nowoczesny, drewniany domek całoroczny na zgłoszenie.", 52.0689, 19.3824);
            Listing l6 = createListing("Mieszkanie 2 pokoje na wynajem, Śródmieście", new BigDecimal("3200"), "Property", "NEW", "Przytulne dwupokojowe mieszkanie w samym centrum Warszawy.", 52.2297, 21.0122);
            Listing l7 = createListing("Rower górski Kross Hexagon", new BigDecimal("1200"), "Sporting Goods", "USED", "Rower górski w pełni sprawny, po przeglądzie.", 53.4285, 14.5528);
            Listing l8 = createListing("Hantle regulowane żeliwne 2x20kg", new BigDecimal("350"), "Sporting Goods", "NEW", "Solidne żeliwne hantle regulowane do domowej siłowni.", 52.2297, 21.0122);
            Listing l9 = createListing("Sofa dwuosobowa rozkładana szara", new BigDecimal("800"), "Home Goods", "USED", "Wygodna sofa rozkładana z pojemnikiem na pościel.", 53.4285, 14.5528);
            Listing l10 = createListing("Ekspres do kawy DeLonghi Magnifica", new BigDecimal("1100"), "Home Goods", "USED", "Automatyczny ekspres do kawy ze spieniaczem mleka.", 52.0689, 19.3824);
            Listing l11 = createListing("Wiertarka udarowa Bosch 800W", new BigDecimal("250"), "Renovation / Home Improvement", "USED", "Niezawodna wiertarka udarowa w walizce z akcesoriami.", 53.4285, 14.5528);
            Listing l12 = createListing("Zestaw narzędzi kluczy 108 el.", new BigDecimal("180"), "Renovation / Home Improvement", "NEW", "Kompletny zestaw kluczy nasadowych z dożywotnią gwarancją.", 52.2297, 21.0122);
            Listing l13 = createListing("Kosiarka spalinowa Honda z napędem", new BigDecimal("1500"), "Garden", "USED", "Mocna kosiarka spalinowa z koszem, silnik Honda.", 52.0689, 19.3824);
            Listing l14 = createListing("Grill ogrodowy węglowy z pokrywą", new BigDecimal("300"), "Garden", "NEW", "Stabilny grill węglowy z termometrem i półkami.", 53.4285, 14.5528);
            Listing l15 = createListing("Buty do biegania Nike Pegasus 40", new BigDecimal("390"), "Clothing", "NEW", "Nowe, oryginalne buty do biegania, rozmiar 43.", 52.2297, 21.0122);
            Listing l16 = createListing("Kurtka zimowa męska 4F puchowa", new BigDecimal("200"), "Clothing", "USED", "Ciepła kurtka puchowa 4F, rozmiar L, stan bardzo dobry.", 53.4285, 14.5528);
            Listing l17 = createListing("Drapak dla kota wysoki słupek", new BigDecimal("150"), "Pet Supplies", "NEW", "Wysoki drapak dla kota z domkiem i zabawką.", 52.0689, 19.3824);
            Listing l18 = createListing("Karma dla psa Royal Canin 15kg", new BigDecimal("220"), "Pet Supplies", "NEW", "Pełnoporcjowa karma dla psów dorosłych ras dużych.", 52.2297, 21.0122);
            Listing l19 = createListing("Biurko narożne białe Ikea", new BigDecimal("320"), "Office Supplies", "USED", "Praktyczne biurko narożne z szufladami i organizerem.", 52.2297, 21.0122);
            Listing l20 = createListing("Fotel biurowy obrotowy ergonomiczny", new BigDecimal("450"), "Office Supplies", "USED", "Ergonomiczny fotel biurowy z regulacją odcinka lędźwiowego.", 53.4285, 14.5528);
            Listing l21 = createListing("Konsola PlayStation 5 Slim 1TB", new BigDecimal("2100"), "Electronics", "NEW", "Najnowsza wersja PS5 Slim z napędem, gwarancja.", 53.4285, 14.5528);
            Listing l22 = createListing("Gra Wiedźmin 3 Dziki Gon Edycja Kompletna PS5", new BigDecimal("100"), "Toys & Games", "USED", "Płyta w idealnym stanie, polska wersja językowa.", 52.0689, 19.3824);
            Listing l23 = createListing("Klocki LEGO Technic Porsche 911", new BigDecimal("650"), "Toys & Games", "NEW", "Oryginalnie zamknięte pudełko klocków LEGO.", 52.2297, 21.0122);

            listingRepository.saveAll(List.of(
                l1, l2, l3, l4, l5, l6, l7, l8, l9, l10,
                l11, l12, l13, l14, l15, l16, l17, l18, l19, l20,
                l21, l22, l23
            ));
            listingService.reindexAll();
            log.info("Seeded initial test listings successfully!");
        } else {
            log.info("Marketplace database already seeded.");
        }
    }

    private Listing createListing(String title, BigDecimal price, String category, String condition, String description, double lat, double lon) {
        Coordinate coordinate = new Coordinate(lon, lat); // JTS points are (lon, lat) -> (X, Y)
        Point locationPoint = geometryFactory.createPoint(coordinate);
        return Listing.builder()
                .title(title)
                .price(price)
                .category(category)
                .condition(condition)
                .description(description)
                .location(locationPoint)
                .build();
    }
}
