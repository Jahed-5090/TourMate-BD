package com.tourmate.bd.data;

import com.tourmate.bd.R;
import com.tourmate.bd.models.District;
import com.tourmate.bd.models.Division;
import com.tourmate.bd.models.TouristSpot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Central data repository for TourMate BD.
 * Contains all divisions, districts, and tourist spots data.
 * Currently populated with Sylhet Division data.
 */
public class DataRepository {

    private static DataRepository instance;
    private List<Division> divisions;

    private DataRepository() {
        initData();
    }

    public static DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public accessors
    // ─────────────────────────────────────────────────────────────────────────

    public List<Division> getAllDivisions() {
        return divisions;
    }

    public Division getDivisionById(String id) {
        for (Division d : divisions) {
            if (d.getId().equals(id)) return d;
        }
        return null;
    }

    public List<District> getDistrictsByDivision(String divisionId) {
        Division div = getDivisionById(divisionId);
        if (div != null) return div.getDistricts();
        return new ArrayList<>();
    }

    public District getDistrictById(String id) {
        for (Division div : divisions) {
            for (District dist : div.getDistricts()) {
                if (dist.getId().equals(id)) return dist;
            }
        }
        return null;
    }

    public List<TouristSpot> getSpotsByDistrict(String districtId) {
        District dist = getDistrictById(districtId);
        if (dist != null && dist.getSpots() != null) return dist.getSpots();
        return new ArrayList<>();
    }

    public TouristSpot getSpotById(String id) {
        for (Division div : divisions) {
            for (District dist : div.getDistricts()) {
                if (dist.getSpots() != null) {
                    for (TouristSpot spot : dist.getSpots()) {
                        if (spot.getId().equals(id)) return spot;
                    }
                }
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data initialisation
    // ─────────────────────────────────────────────────────────────────────────

    private void initData() {
        divisions = new ArrayList<>();

        // ── Sylhet Division (fully populated) ────────────────────────────────
        Division sylhetDivision = new Division(
                "sylhet",
                "Sylhet",
                "সিলেট",
                "The land of two leaves and a bud — famous for tea gardens, haors, and waterfalls.",
                R.drawable.sylhet
        );
        sylhetDivision.setDistricts(buildSylhetDistricts());
        divisions.add(sylhetDivision);

        // ── Other divisions (placeholder — coming soon) ───────────────────────
        divisions.add(buildPlaceholderDivision("dhaka", "Dhaka", "ঢাকা", R.drawable.dhaka));
        divisions.add(buildPlaceholderDivision("chittagong", "Chattogram", "চট্টগ্রাম", R.drawable.chattogram));
        divisions.add(buildPlaceholderDivision("rajshahi", "Rajshahi", "রাজশাহী", R.drawable.rajshahi));
        divisions.add(buildPlaceholderDivision("khulna", "Khulna", "খুলনা", R.drawable.khulna));
        divisions.add(buildPlaceholderDivision("barishal", "Barishal", "বরিশাল", R.drawable.barishal));
        divisions.add(buildPlaceholderDivision("rangpur", "Rangpur", "রংপুর", R.drawable.rangpur));
        divisions.add(buildPlaceholderDivision("mymensingh", "Mymensingh", "ময়মনসিংহ", R.drawable.mymensingh));
    }

    private Division buildPlaceholderDivision(String id, String name, String nameBn, int imgRes) {
        Division div = new Division(id, name, nameBn, "Coming Soon — Content is being prepared.", imgRes);
        div.setDistricts(new ArrayList<>());
        return div;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sylhet Division — Districts
    // ─────────────────────────────────────────────────────────────────────────

    private List<District> buildSylhetDistricts() {
        List<District> districts = new ArrayList<>();

        districts.add(buildSylhetDistrict());
        districts.add(buildMoulvibazarDistrict());
        districts.add(buildHabiganjDistrict());
        districts.add(buildSunamganjDistrict());

        return districts;
    }

    // ── Sylhet District ───────────────────────────────────────────────────────

    private District buildSylhetDistrict() {
        District dist = new District(
                "sylhet_dist", "sylhet",
                "Sylhet", "সিলেট",
                "The capital of Sylhet division, home to shrines, tea gardens, and the iconic Jaflong.",
                R.drawable.dist_sylhet
        );

        List<TouristSpot> spots = new ArrayList<>();

        // 1. Hazrat Shahjalal Mazar
        TouristSpot shahjalal = new TouristSpot(
                "shahjalal_mazar", "sylhet_dist",
                "Hazrat Shahjalal (R) Mazar", "হযরত শাহজালাল (রঃ) এর মাজার",
                "Sacred shrine of the great Muslim saint Hazrat Shahjalal who brought Islam to Sylhet.",
                R.drawable.spot_shahjalal, TouristSpot.SpotCategory.RELIGIOUS
        );
        shahjalal.setFullDescription("The Dargah of Hazrat Shah Jalal is one of the most revered Islamic shrines in Bangladesh. Hazrat Shah Jalal (R) was a 14th-century Muslim missionary who conquered Sylhet. The shrine complex includes the mosque, his tomb, and the famous Gazzali fish pond. Thousands of devotees visit daily from across Bangladesh and beyond.");
        shahjalal.setHowToReach("Located in the heart of Sylhet city (Dargah Gate area). From Sylhet Railway Station or Bus Terminal: Take CNG auto-rickshaw (~৳60-80) or rickshaw (~৳30-50) to Dargah Gate. About 2-3 km from the station.");
        shahjalal.setTransportOptions("CNG Auto-rickshaw | Rickshaw | Taxi | Uber/Pathao");
        shahjalal.setApproxCostFromDhaka("Bus: ৳450-800 (AC/Non-AC) | Train: ৳200-1200 (Shuvro Express, Upaban, Parabat) | Flight: ৳2500-6000");
        shahjalal.setApproxCostFromSylhet("৳60-100 by CNG | ৳30-50 by rickshaw");
        shahjalal.setEntranceFee("Free entry for all visitors");
        shahjalal.setBestTimeToVisit("Year-round. Thursdays and Fridays have special gatherings.");
        shahjalal.setOpeningHours("Open 24 hours");
        shahjalal.setNearbyHotels("Hotel Star Pacific, Hotel Gulshan, Rose View Hotel (within 1 km)");
        shahjalal.setBudgetAccommodation("Mazar Gate area has many budget guesthouses from ৳500/night");
        shahjalal.setLocalFood("Biryani, Naan with Mutton at Mazar Gate restaurants. Try Panchvhai restaurant nearby.");
        shahjalal.setTravelTips("Dress modestly. Women should cover heads inside the shrine. Best visited in the morning (less crowded). The Gazzali fish (catfish) in the pond are considered sacred — do not harm them.");
        shahjalal.setWarnings("⚠️ Be cautious of fake 'khadems' who may demand money for prayers. Donation is purely voluntary. Watch your belongings in crowded areas.");
        shahjalal.setLatitude(24.8961);
        shahjalal.setLongitude(91.8690);
        shahjalal.setYoutubeVideoId("ScTmIPB5G4A");
        spots.add(shahjalal);

        // 2. Hazrat Shah Paran Mazar
        TouristSpot shahParan = new TouristSpot(
                "shahparan_mazar", "sylhet_dist",
                "Hazrat Shah Paran (RA) Mazar", "হযরত শাহ পরাণ (রাঃ) এর মাজার",
                "Shrine of Hazrat Shah Paran, nephew of Hazrat Shahjalal, located on a picturesque hill.",
                R.drawable.spot_shahparan, TouristSpot.SpotCategory.RELIGIOUS
        );
        shahParan.setFullDescription("Located about 8 km east of Sylhet city, this shrine sits atop a small hill surrounded by lush green. Shah Paran was the nephew and disciple of Hazrat Shahjalal. The location offers a peaceful atmosphere with panoramic views of the surrounding countryside.");
        shahParan.setHowToReach("From Sylhet city (Dargah Gate or Zindabazar): Take a CNG auto-rickshaw directly to Shah Paran Mazar (~8 km). Local buses also available from Ambarkhana area.");
        shahParan.setTransportOptions("CNG Auto-rickshaw | Local Bus | Leguna (shared mini-van)");
        shahParan.setApproxCostFromSylhet("৳100-150 by CNG (one way) | ৳15-20 by local bus");
        shahParan.setEntranceFee("Free entry");
        shahParan.setBestTimeToVisit("Year-round. Morning visits are most peaceful.");
        shahParan.setOpeningHours("Open 24 hours");
        shahParan.setTravelTips("Combine this visit with Hazrat Shahjalal Mazar as both shrines are related. The hilltop location provides a great photo opportunity.");
        shahParan.setWarnings("⚠️ CNG drivers may try to charge extra — agree on fare BEFORE boarding. Standard fare should be around ৳100-150.");
        shahParan.setLatitude(24.9108);
        shahParan.setLongitude(91.9220);
        shahParan.setYoutubeVideoId("qbCcINkiWbM");
        spots.add(shahParan);

        // 3. Ratargul Swamp Forest
        TouristSpot ratargul = new TouristSpot(
                "ratargul", "sylhet_dist",
                "Ratargul Swamp Forest", "রাতারগুল সোয়াম্প ফরেস্ট",
                "Bangladesh's only freshwater swamp forest — the Amazon of Bengal. Boat ride through submerged trees.",
                R.drawable.spot_ratargul, TouristSpot.SpotCategory.FOREST
        );
        ratargul.setFullDescription("Ratargul is the only freshwater swamp forest in Bangladesh and one of the few in the world. During monsoon season (June–October), the forest floor is submerged under 20-30 feet of crystal-clear water, creating a magical ethereal experience. You navigate through the forest on small wooden boats, passing through the canopy of trees rising from the water.");
        ratargul.setHowToReach("From Sylhet city: Take a CNG or local bus to Gowainghat Upazila (25 km, ~45 min). Then hire a local boat from Ratargul Ghat. From Sylhet Ambarkhana: Direct CNG or Leguna available.");
        ratargul.setTransportOptions("CNG to Gowainghat | Boat from the ghat");
        ratargul.setApproxCostFromSylhet("CNG: ৳300-400 (one way) | Boat hire: ৳400-600 for 2-3 hours (fixed price from forest office)");
        ratargul.setEntranceFee("Forest entry: ৳20 per person | Boat: ৳400-600 per boat");
        ratargul.setBestTimeToVisit("June to October (monsoon) for the flooded forest experience. July-August is peak — most of the forest is submerged.");
        ratargul.setOpeningHours("6:00 AM – 6:00 PM");
        ratargul.setNearbyHotels("No hotels inside; stay in Sylhet city and make a day trip.");
        ratargul.setTravelTips("Go early morning for the best light and fewer crowds. Wear clothes you don't mind getting wet. Carry insect repellent. The official boat ghat is managed by the Forest Department — use only official boats.");
        ratargul.setWarnings("⚠️ IMPORTANT: Only use boats from the official Forest Department ghat. Unofficial boatmen will overcharge. The official rate board is displayed at the ghat — insist on the posted rates. Do NOT go alone — always hire a guide. Avoid the plastic pollution from boats — some operators are irresponsible.");
        ratargul.setLatitude(24.9975);
        ratargul.setLongitude(91.9419);
        ratargul.setYoutubeVideoId("E8R7aiTXT8E");
        ratargul.setGalleryImages(java.util.Arrays.asList(
                R.drawable.ratargul2,
                R.drawable.ratargul3,
                R.drawable.ratargul4,
                R.drawable.ratargul6
        ));
        spots.add(ratargul);

        // 4. Jaflong
        TouristSpot jaflong = new TouristSpot(
                "jaflong", "sylhet_dist",
                "Jaflong", "জাফলং",
                "Where the Piyain river meets India — stone collection beaches, Khasi villages, and stunning mountain views.",
                R.drawable.spot_jaflong, TouristSpot.SpotCategory.NATURAL
        );
        jaflong.setFullDescription("Jaflong is one of the most popular tourist destinations in Sylhet, located at the zero point of the Bangladesh-India border in Gowainghat Upazila. The area is famous for its stone-collection activities along the crystal-clear Piyain River, the indigenous Khasi tribal community, betel leaf gardens, and breathtaking views of the Meghalaya hills in India. The Dauki waterfall is visible across the border during monsoon.");
        jaflong.setHowToReach("From Sylhet city: Buses from Kadamtali or Ambarkhana bus stand go directly to Jaflong (45-50 km, ~1.5-2 hours). Alternatively hire a CNG or microbus. The route passes through beautiful countryside.");
        jaflong.setTransportOptions("Direct bus from Ambarkhana | CNG (private hire) | Microbus/Leguna | Rental car");
        jaflong.setApproxCostFromDhaka("Total trip cost: ৳2000-5000 (2-day trip including transport, hotel, food)");
        jaflong.setApproxCostFromSylhet("Bus: ৳80-100 per person | CNG (private): ৳800-1200 one way");
        jaflong.setEntranceFee("No official entry fee (though locals may try to collect unofficial fees — not mandatory)");
        jaflong.setBestTimeToVisit("October to April (winter). Monsoon offers dramatic scenery but roads can be slippery.");
        jaflong.setOpeningHours("Accessible during daylight hours");
        jaflong.setNearbyHotels("Limited hotels near Jaflong; most tourists stay in Sylhet city or Srimangal area.");
        jaflong.setLocalFood("Fresh fish from Piyain river, Khichuri, local rice hotels near the ghat area.");
        jaflong.setTravelTips("Bargain for stone-picking fees. Visit the Khasi village (Panjipara) for authentic cultural experience. Take sunrise trip if possible. Combine with Ratargul in same day trip.");
        jaflong.setWarnings("⚠️ HIGH RISK of overcharging by local transport and 'guides'. Fix the price BEFORE starting. ⚠️ Multiple fake 'entry fees' are collected at checkpoints — only pay official government receipted fees. ⚠️ Be careful on slippery river stones. ⚠️ Photography of border area may have restrictions.");
        jaflong.setLatitude(25.1671);
        jaflong.setLongitude(92.0313);
        jaflong.setYoutubeVideoId("QHhGeWyxntI");
        spots.add(jaflong);

        // 5. Lalakhal
        TouristSpot lalakhal = new TouristSpot(
                "lalakhal", "sylhet_dist",
                "Lalakhal", "লালাখাল",
                "Crystal turquoise river flowing from the Meghalaya hills — one of Bangladesh's most beautiful waterways.",
                R.drawable.spot_lalakhal, TouristSpot.SpotCategory.NATURAL
        );
        lalakhal.setFullDescription("Lalakhal is a mesmerising river channel in Jaintiapur Upazila. The name 'Lalakhal' (Red Canal) is deceptive — the water is an incredibly vivid turquoise-green colour due to minerals from the hills of Meghalaya, India. The contrast between the turquoise water, green hills, and blue sky makes it one of the most photogenic spots in Bangladesh. Boat rides on this river are magical.");
        lalakhal.setHowToReach("From Sylhet city: Take Jaintiapur bus from Kadamtali bus stand (~30 km, 45 min). Alight at Sarighat and hire a boat. Or take a CNG directly to Lalakhal Ghat.");
        lalakhal.setTransportOptions("Bus to Jaintiapur | CNG to Sarighat Ghat | Boat on the canal");
        lalakhal.setApproxCostFromSylhet("Bus: ৳40-60 | CNG: ৳400-600 | Boat: ৳500-800 per boat/hour");
        lalakhal.setEntranceFee("Boat hire: ৳500-1000 depending on duration");
        lalakhal.setBestTimeToVisit("October to March. The turquoise color is most vivid in dry season when water is clearer.");
        lalakhal.setTravelTips("Morning light is perfect for photography. Combine with Jaflong as they are in the same direction. Negotiating boat fares is necessary — start lower than the asking price.");
        lalakhal.setWarnings("⚠️ Boat owners may try to charge 3-4x the fair rate. The fair rate for a 1-hour boat ride is ৳500-800. Ask other tourists who have just finished their rides. ⚠️ Swimming is NOT recommended — the current can be deceptive.");
        lalakhal.setLatitude(25.1020);
        lalakhal.setLongitude(92.0764);
        lalakhal.setYoutubeVideoId("-FT5qgbxAUY");
        spots.add(lalakhal);

        // 6. Bichanakandi
        TouristSpot bichanakandi = new TouristSpot(
                "bichanakandi", "sylhet_dist",
                "Bichanakandi", "বিছনাকান্দি",
                "A rocky riverbed at the foot of Meghalaya hills where mountain streams pour down crystal waterfalls.",
                R.drawable.spot_bichanakandi, TouristSpot.SpotCategory.NATURAL
        );
        bichanakandi.setFullDescription("Bichanakandi is a stunning natural spot located in Gowainghat Upazila at the foot of the Khasi Hills (Meghalaya). The Piyain river flows over a wide rocky bed with water cascading down from the hills of India. During monsoon, the sight of multiple waterfalls pouring down from the mountains with the rocky foreground is truly spectacular. It's accessible by boat from Hadaynagar Ghat.");
        bichanakandi.setHowToReach("From Sylhet city (Ambarkhana): CNG to Hadaynagar (~22 km, 30-40 min). From Hadaynagar, hire a boat to Bichanakandi (30-45 min boat ride each way).");
        bichanakandi.setTransportOptions("CNG to Hadaynagar Ghat | Local boat");
        bichanakandi.setApproxCostFromSylhet("CNG to Hadaynagar: ৳300-400 | Boat: ৳600-1000 per boat (round trip)");
        bichanakandi.setEntranceFee("No entry fee. Boat hire required.");
        bichanakandi.setBestTimeToVisit("June to October (monsoon) for dramatic waterfall views. October-November for clear water with reduced crowd.");
        bichanakandi.setTravelTips("Visit on a clear morning. Pack lunch as there are very limited food options. Wear waterproof footwear. Excellent photography spot — especially when clouds hang low over the hills.");
        bichanakandi.setWarnings("⚠️ Boat operators frequently overcharge tourists. Negotiate FIRMLY before boarding. Standard round-trip boat fare: ৳600-800 per boat (not per person). ⚠️ The rocky surface is slippery — exercise caution. ⚠️ Can be crowded on weekends.");
        bichanakandi.setLatitude(25.0873);
        bichanakandi.setLongitude(91.9671);
        bichanakandi.setYoutubeVideoId("yJWh34w-05U");
        spots.add(bichanakandi);

        // 7. Dibir Haor
        TouristSpot dibirHaor = new TouristSpot(
                "dibir_haor", "sylhet_dist",
                "Dibir Haor", "ডিবির হাওর",
                "A serene wetland lake with floating villages, migratory birds, and the 'floating garden' experience.",
                R.drawable.spot_dibir_haor, TouristSpot.SpotCategory.HAOR
        );
        dibirHaor.setFullDescription("Dibir Haor (also called Tekerghat Haor area) in Sunamganj district is a vast freshwater wetland that transforms dramatically between seasons. During monsoon, it becomes a vast inland sea. In winter, it reveals green paddy fields, floating houses, and thousands of migratory birds. The nearby Tekerghat white stone area and the azure blue lake area are particularly breathtaking.");
        dibirHaor.setHowToReach("From Sylhet city: Bus or CNG to Sunamganj town (75 km, ~2 hrs). Then hire boat or local transport to Tekerghat/Dibir Haor area (another 40-50 km). A full-day trip or overnight is recommended.");
        dibirHaor.setTransportOptions("Bus to Sunamganj | Local boat | Engine boat");
        dibirHaor.setApproxCostFromSylhet("Bus to Sunamganj: ৳120-180 | Boat hire from Sunamganj: ৳1500-2500 for a day");
        dibirHaor.setEntranceFee("No official entry fee");
        dibirHaor.setBestTimeToVisit("December to February (winter) for migratory birds and clear skies. Monsoon (July-September) for the vast water experience.");
        dibirHaor.setTravelTips("An overnight trip is strongly recommended to experience both sunrise and sunset over the haor. Pack warm clothes for winter visits. Rent an engine boat for the day for maximum coverage. Carry your own food and water.");
        dibirHaor.setWarnings("⚠️ Plan your trip with a reliable local guide. Navigation on haors can be complex and disorienting. ⚠️ During monsoon, weather can change rapidly — check forecast before visiting. ⚠️ Do not book random 'tour operators' who approach you at Sylhet bus stands — they often overcharge significantly.");
        dibirHaor.setLatitude(25.0833);
        dibirHaor.setLongitude(91.4167);
        dibirHaor.setYoutubeVideoId("43_EH6k4Mug");
        spots.add(dibirHaor);

        // 8. Lakkatura Tea Garden
        TouristSpot lakkatura = new TouristSpot(
                "lakkatura", "sylhet_dist",
                "Lakkatura Tea Garden", "লাক্কাতুরা চা বাগান",
                "One of Sylhet city's oldest tea gardens — rolling hills of green tea within the city limits.",
                R.drawable.spot_lakkatura, TouristSpot.SpotCategory.TEA_GARDEN
        );
        lakkatura.setFullDescription("Lakkatura Tea Garden is one of the oldest tea estates in Sylhet, located very close to Sylhet city center (near M.A.G Osmani Airport). The garden offers rolling green hills of tea bushes, shade trees, and a peaceful atmosphere easily accessible from the city. Tea pickers in colorful saris are a common and photogenic sight in the mornings.");
        lakkatura.setHowToReach("Located near Sylhet Airport. From Sylhet city center (Zindabazar): CNG (~3 km, ৳60-80) or Rickshaw.");
        lakkatura.setTransportOptions("CNG | Rickshaw | Walking from airport area");
        lakkatura.setApproxCostFromSylhet("৳60-80 by CNG");
        lakkatura.setEntranceFee("Free entry (it is a working tea garden — please respect the workers)");
        lakkatura.setBestTimeToVisit("Year-round. October to March has clear skies. Early morning is best for photography.");
        lakkatura.setTravelTips("Visit in the early morning when tea pickers are working for authentic photography. This is a great spot to combine with airport transfers. Stay on designated paths — do not disturb the tea plants.");
        lakkatura.setWarnings("⚠️ This is a private working tea estate. Entering restricted plantation areas without permission is prohibited. Be respectful to workers.");
        lakkatura.setLatitude(24.9630);
        lakkatura.setLongitude(91.8930);
        lakkatura.setYoutubeVideoId("NzCQK_y9nuk");
        spots.add(lakkatura);

        dist.setSpots(spots);
        return dist;
    }

    // ── Moulvibazar District ──────────────────────────────────────────────────

    private District buildMoulvibazarDistrict() {
        District dist = new District(
                "moulvibazar_dist", "sylhet",
                "Moulvibazar", "মৌলভীবাজার",
                "Home to Srimangal — the tea capital of Bangladesh — with lush gardens, wildlife, and stunning waterfalls.",
                R.drawable.dist_moulvibazar
        );

        List<TouristSpot> spots = new ArrayList<>();

        // 1. Madhabkund Waterfall
        TouristSpot madhabkund = new TouristSpot(
                "madhabkund", "moulvibazar_dist",
                "Madhabkund Waterfall", "মাধবকুণ্ড জলপ্রপাত",
                "Bangladesh's largest natural waterfall — a 162-foot cascade in a lush eco-park setting.",
                R.drawable.madhobkundo1, TouristSpot.SpotCategory.WATERFALL
        );
        madhabkund.setFullDescription("Madhabkund is the largest natural waterfall in Bangladesh, located in Barlekha Upazila of Moulvibazar. The waterfall plunges approximately 162 feet from a rocky cliff into a pool below. It is located within a Government Eco-Park, surrounded by dense tropical forest. The area has significant religious importance for the Hindu community (Shiva temple nearby). On rainy days, additional smaller waterfalls appear around the main fall.");
        madhabkund.setHowToReach("From Sylhet city: Bus or CNG to Moulvibazar town (50 km, ~1.5 hrs), then another bus/CNG to Barlekha/Madhabkund (40 km, ~1 hr). From Dhaka: Direct bus to Moulvibazar via Srimangal.");
        madhabkund.setTransportOptions("Bus (Sylhet-Moulvibazar) | CNG/Microbus | Rental vehicle");
        madhabkund.setApproxCostFromDhaka("Bus: ৳400-600 (Dhaka to Moulvibazar via Srimangal)");
        madhabkund.setApproxCostFromSylhet("Bus: ৳80-100 to Moulvibazar + ৳120-150 to Madhabkund. Total: ~৳200-250");
        madhabkund.setEntranceFee("Eco-Park entry: ৳20 per person");
        madhabkund.setBestTimeToVisit("June to October (monsoon) when the waterfall is at its fullest. Avoid dry season (March-May) as water flow reduces significantly.");
        madhabkund.setOpeningHours("9:00 AM – 5:00 PM");
        madhabkund.setLocalFood("Local restaurants near the park entry serve Bengali meals (rice, fish, dal).");
        madhabkund.setTravelTips("The path to the waterfall is moderately easy (~1 km walk from entrance). Wear non-slip footwear. Bring raincoat during monsoon. The Shiva temple nearby is also worth visiting.");
        madhabkund.setWarnings("⚠️ Swimming near the waterfall base is dangerous due to strong currents — there have been drowning accidents. Stay behind safety barriers. ⚠️ Local transport from Barlekha to the park may try to overcharge — fix fare beforehand.");
        madhabkund.setLatitude(24.5083);
        madhabkund.setLongitude(92.1833);
        madhabkund.setYoutubeVideoId("WJaa_1cJD_w");
        madhabkund.setGalleryImages(java.util.Arrays.asList(
                R.drawable.madhobkundo1,
                R.drawable.madhobkundo2,
                R.drawable.madhobkundo3,
                R.drawable.madhobkundo4
        ));
        spots.add(madhabkund);

        // 2. Lawachara National Park
        TouristSpot lawachara = new TouristSpot(
                "lawachara", "moulvibazar_dist",
                "Lawachara National Park", "লাউয়াছড়া জাতীয় উদ্যান",
                "Primary tropical rainforest — habitat of the endangered western hoolock gibbon and diverse wildlife.",
                R.drawable.lawachara1, TouristSpot.SpotCategory.FOREST
        );
        lawachara.setFullDescription("Lawachara National Park is a protected tropical rainforest in Kamalganj Upazila of Moulvibazar. Covering about 1,250 hectares, it is one of Bangladesh's most biodiverse national parks. The forest is home to the critically endangered western hoolock gibbon — the only ape species in Bangladesh — along with leopard cats, olive-backed sunbirds, and over 240 bird species. The park has well-maintained walking trails through primary forest.");
        lawachara.setHowToReach("Located 3 km from Srimangal town. From Srimangal Railway Station or bus stand: CNG or auto (~৳50-80). From Dhaka: Upaban Express or Parabat Express train to Srimangal (direct, ~3.5 hrs).");
        lawachara.setTransportOptions("Train to Srimangal (from Dhaka) | CNG from Srimangal");
        lawachara.setApproxCostFromDhaka("Train (Upaban Express): ৳150-800 depending on class | Bus: ৳300-500");
        lawachara.setApproxCostFromSylhet("Bus or CNG to Srimangal: ৳100-200 | CNG to park: ৳50-80");
        lawachara.setEntranceFee("৳50 per person (Bangladeshi) | ৳150 per person (Foreign tourist)");
        lawachara.setBestTimeToVisit("November to March (cool, dry). Hoolock gibbons are most active in early morning and late afternoon.");
        lawachara.setOpeningHours("8:00 AM – 5:00 PM");
        lawachara.setNearbyHotels("Srimangal has many hotels: Grand Sultan Tea Resort (luxury), Nishorgo Eco Cottage, Tea Resort (budget)");
        lawachara.setLocalFood("Srimangal is famous for 7-layer tea (সাত রঙের চা) — a must try! Available at Nil Kantha Tea Cabin.");
        lawachara.setTravelTips("MUST hire an official guide from the Forest Department at the gate (৳300-500). They know the best spots to see gibbons. Go very early morning (7-8 AM) for best wildlife sightings. Silence is essential inside the forest.");
        lawachara.setWarnings("⚠️ Do not hire unofficial 'guides' who approach you before reaching the park gate. Use only Forest Department certified guides. ⚠️ Maintain complete silence inside — noise disturbs wildlife. ⚠️ Do not litter — heavy fines apply.");
        lawachara.setLatitude(24.3275);
        lawachara.setLongitude(91.7856);
        lawachara.setYoutubeVideoId("pG1Zx6sL-Ks");
        lawachara.setGalleryImages(java.util.Arrays.asList(
                R.drawable.lawachara1,
                R.drawable.lawachara2,
                R.drawable.lawachara3
        ));
        spots.add(lawachara);

        // 3. Madhobpur Lake
        TouristSpot madhobpur = new TouristSpot(
                "madhobpur_lake", "moulvibazar_dist",
                "Madhobpur Lake", "মাধবপুর লেক",
                "A hauntingly beautiful lake surrounded by a tea estate — a jewel hidden in Moulvibazar.",
                R.drawable.madhobpur_lake1, TouristSpot.SpotCategory.NATURAL
        );
        madhobpur.setFullDescription("Madhobpur Lake is a stunning artificial lake within the Madhobpur Tea Estate in Chunarughat Upazila. The lake's still dark waters, bordered by tea gardens and tropical forest, create an atmospheric and photogenic landscape unlike anything else in Bangladesh. A walking trail around the lake takes about 30-45 minutes. The reflecting trees in the dark water make for remarkable photographs.");
        madhobpur.setHowToReach("From Srimangal: CNG/Auto to Chunarughat (~20 km) then local directions to Madhobpur. From Habiganj: about 40 km via Chunarughat.");
        madhobpur.setTransportOptions("CNG/Microbus | Rental vehicle (recommended for easier access)");
        madhobpur.setApproxCostFromSylhet("৳300-500 by CNG from Srimangal area");
        madhobpur.setEntranceFee("Small charge may apply at tea estate gate (~৳20-50)");
        madhobpur.setBestTimeToVisit("October to March. Early morning with mist is magical.");
        madhobpur.setTravelTips("Combine this with a Lawachara visit in the same day (they are in the same region). The lake is especially stunning in early morning mist. Bring your own snacks as food options are very limited.");
        madhobpur.setWarnings("⚠️ This is within a private tea estate — follow estate rules. Photography of factory equipment may be restricted.");
        madhobpur.setLatitude(24.3667);
        madhobpur.setLongitude(91.8500);
        madhobpur.setYoutubeVideoId("NzCQK_y9nuk");
        madhobpur.setGalleryImages(java.util.Arrays.asList(
                R.drawable.madhobpur_lake1,
                R.drawable.madhobpur_lake2,
                R.drawable.madhobpur_lake3,
                R.drawable.madhobpur_lake4
        ));
        spots.add(madhobpur);

        // 4. Hamham Waterfall
        TouristSpot hamham = new TouristSpot(
                "hamham", "moulvibazar_dist",
                "Hamham Waterfall", "হামহাম জলপ্রপাত",
                "A remote, recently discovered double-tiered waterfall deep in the Rajkandi forest reserve.",
                R.drawable.ham_ham1, TouristSpot.SpotCategory.WATERFALL
        );
        hamham.setFullDescription("Hamham (also spelled Hum Hum) is a spectacular double-tiered waterfall discovered as recently as 2010, hidden deep within the Rajkandi Reserve Forest in Kamalganj Upazila. To reach it requires a 4-6 hour trek through dense forest, crossing streams multiple times. The remoteness and pristine natural beauty make the difficult journey extremely rewarding. The waterfall drops about 135 feet in two stages.");
        hamham.setHowToReach("From Srimangal: CNG to Kalapur Bazar (~15 km, 30 min). From Kalapur, hire local guide and start the 9-12 km forest trek (takes 4-6 hours one way). Starting the journey at dawn (5-6 AM) is essential.");
        hamham.setTransportOptions("CNG to Kalapur | Trek on foot (mandatory)");
        hamham.setApproxCostFromSylhet("CNG to Kalapur: ৳300-400 | Guide fee: ৳500-800 | Total day trip: ৳1500-2500 per person");
        hamham.setEntranceFee("No official entry fee. Guide fee required.");
        hamham.setBestTimeToVisit("October to April (avoid monsoon due to slippery forest trails and flash floods)");
        hamham.setTravelTips("This is a STRENUOUS trek. Only attempt if you are physically fit. MANDATORY: hire a local guide from Kalapur village (do not attempt without one). Start at 6 AM to complete in daylight. Wear proper trekking shoes. Carry adequate water and food for a full day. Cross the streams carefully.");
        hamham.setWarnings("⚠️ CRITICAL: Do NOT attempt this trek without a local guide — people have gotten lost in this forest. ⚠️ Do NOT visit during monsoon — risk of flash floods and very slippery trail. ⚠️ Tell someone your itinerary and expected return time. ⚠️ There is NO mobile network inside the forest.");
        hamham.setLatitude(24.3000);
        hamham.setLongitude(91.8333);
        hamham.setYoutubeVideoId("WJaa_1cJD_w");
        hamham.setGalleryImages(java.util.Arrays.asList(
                R.drawable.ham_ham1,
                R.drawable.ham_ham2,
                R.drawable.ham_ham3,
                R.drawable.ham_ham4
        ));
        spots.add(hamham);

        // 5. Baika Beel
        TouristSpot baikaBeel = new TouristSpot(
                "baika_beel", "moulvibazar_dist",
                "Baika Beel Bird Sanctuary", "বাইক্কা বিল",
                "A protected wetland bird sanctuary — winter home to thousands of migratory birds.",
                R.drawable.baikka_bil1, TouristSpot.SpotCategory.HAOR
        );
        baikaBeel.setFullDescription("Baika Beel is a permanently protected wetland sanctuary in the Hail Haor, Srimangal. The beel is a Community Conservation Area managed by the local fishing community with government support. It is famous for its enormous population of migratory birds in winter — thousands of ducks, teals, and wading birds arrive between October and March. A watchtower provides excellent bird-watching opportunities.");
        baikaBeel.setHowToReach("From Srimangal town: CNG or auto to Baika Beel (~10 km, 20 min).");
        baikaBeel.setTransportOptions("CNG/Auto from Srimangal");
        baikaBeel.setApproxCostFromSylhet("Bus to Srimangal: ৳100-150 | CNG to beel: ৳100-150");
        baikaBeel.setEntranceFee("৳20 per person");
        baikaBeel.setBestTimeToVisit("November to February for migratory birds. Dawn and dusk are best viewing times.");
        baikaBeel.setOpeningHours("6:00 AM – 5:30 PM");
        baikaBeel.setTravelTips("Bring binoculars if you have them. The watchtower provides an excellent panoramic view. Early morning arrival is recommended for the best bird activity. This is an excellent photography location.");
        baikaBeel.setWarnings("⚠️ This is a protected sanctuary — fishing is prohibited within the sanctuary boundary. Respect wildlife. ⚠️ Keep noise to minimum to avoid disturbing birds.");
        baikaBeel.setLatitude(24.3194);
        baikaBeel.setLongitude(91.7917);
        baikaBeel.setYoutubeVideoId("yJWh34w-05U");
        baikaBeel.setGalleryImages(java.util.Arrays.asList(
                R.drawable.baikka_bil1,
                R.drawable.baikka_bil2,
                R.drawable.baikka_bil3
        ));
        spots.add(baikaBeel);

        dist.setSpots(spots);
        return dist;
    }

    // ── Habiganj District ─────────────────────────────────────────────────────

    private District buildHabiganjDistrict() {
        District dist = new District(
                "habiganj_dist", "sylhet",
                "Habiganj", "হবিগঞ্জ",
                "Land of haors, tea gardens, and the historic Satchhari forest — gateway to northeastern Bangladesh.",
                R.drawable.dist_habiganj
        );

        List<TouristSpot> spots = new ArrayList<>();

        // 1. Satchhari National Park
        TouristSpot satchhari = new TouristSpot(
                "satchhari", "habiganj_dist",
                "Satchhari National Park", "সাতছড়ি জাতীয় উদ্যান",
                "Tropical rainforest home to the hoolock gibbon, where 7 streams meet in a pristine forest.",
                R.drawable.shatchori1, TouristSpot.SpotCategory.FOREST
        );
        satchhari.setFullDescription("Satchhari National Park (literally 'seven streams') is a protected tropical rainforest in Chunarughat Upazila of Habiganj. The park covers 243 hectares of mixed tropical forest and is named after the seven small streams (chhara) flowing through it. Like Lawachara, it is a habitat for the western hoolock gibbon, as well as leopard cats, various primates, and hundreds of bird species. The park has relatively shorter and easier trails than Lawachara.");
        satchhari.setHowToReach("From Habiganj town: CNG or bus to Chunarughat (~25 km), then local directions to Satchhari. From Sylhet: Bus toward Habiganj, alight at Chunarughat.");
        satchhari.setTransportOptions("Bus (Sylhet-Habiganj-Chunarughat) | CNG | Rental vehicle");
        satchhari.setApproxCostFromSylhet("Bus: ৳80-120 | CNG from Habiganj: ৳150-200");
        satchhari.setEntranceFee("৳20 per person (Bangladeshi)");
        satchhari.setBestTimeToVisit("November to March. Early morning is best for gibbons.");
        satchhari.setOpeningHours("8:00 AM – 5:00 PM");
        satchhari.setTravelTips("Hire a local guide at the entrance for the best wildlife experience. Shorter trails (1-2 hours) are available unlike Lawachara's longer routes. Good spot to combine with Madhobpur Lake in the same day.");
        satchhari.setWarnings("⚠️ Guide is mandatory. Do not venture off marked trails. Maintain silence for wildlife encounters.");
        satchhari.setLatitude(24.2961);
        satchhari.setLongitude(91.6408);
        satchhari.setYoutubeVideoId("iPHjZxqPdfc");
        satchhari.setGalleryImages(java.util.Arrays.asList(
                R.drawable.shatchori1,
                R.drawable.shatchori2,
                R.drawable.shatchori3,
                R.drawable.shatchori4
        ));
        spots.add(satchhari);

        dist.setSpots(spots);
        return dist;
    }

    // ── Sunamganj District ────────────────────────────────────────────────────

    private District buildSunamganjDistrict() {
        District dist = new District(
                "sunamganj_dist", "sylhet",
                "Sunamganj", "সুনামগঞ্জ",
                "The land of haors — vast wetlands, Tanguar Haor, and the legendary folk mystic Hason Raja.",
                R.drawable.dist_sunamganj
        );

        List<TouristSpot> spots = new ArrayList<>();

        // 1. Tanguar Haor
        TouristSpot tanguarHaor = new TouristSpot(
                "tanguar_haor", "sunamganj_dist",
                "Tanguar Haor", "টাঙ্গুয়ার হাওর",
                "A UNESCO Ramsar wetland site — one of Bangladesh's most pristine ecosystems and a biodiversity hotspot.",
                R.drawable.tanguarhaor1, TouristSpot.SpotCategory.HAOR
        );
        tanguarHaor.setFullDescription("Tanguar Haor is a wetland of international importance, recognized as a Ramsar site. Located in Tahirpur and Dharmapasha Upazilas of Sunamganj, this vast haor system covers about 100 sq km and is bordered by the Meghalaya hills of India. It is home to over 140 species of freshwater fish, 208 bird species (including 98 migratory species), and various other wildlife. Experiencing the haor on a boat — especially at sunrise and sunset — is a transcendent experience. A 2-3 day houseboat trip is the ideal way to explore.");
        tanguarHaor.setHowToReach("From Sylhet: Bus to Sunamganj town (75 km, ~2-2.5 hrs). From Sunamganj Sadar Ghat: Hire speedboat or engine boat to reach Tanguar Haor (1.5-2 hrs). Alternatively, go via Tahirpur Ghat which is closer.");
        tanguarHaor.setTransportOptions("Bus to Sunamganj | Speedboat or Engine Boat from Sunamganj Ghat");
        tanguarHaor.setApproxCostFromDhaka("Bus to Sunamganj: ৳500-700 | Total 2-day trip: ৳3000-8000 per person (depending on group size and boat type)");
        tanguarHaor.setApproxCostFromSylhet("Bus: ৳120-180 | Boat (speedboat, full day): ৳3000-5000 per boat | Houseboat (2 days): ৳8000-15000 per boat");
        tanguarHaor.setEntranceFee("No entry fee. Boat hire cost is the main expense.");
        tanguarHaor.setBestTimeToVisit("November to February (winter) for migratory birds and clear water. March-May for lotus flowers. Monsoon (July-September) for the vast flooded landscape.");
        tanguarHaor.setNearbyHotels("Sunamganj town has several hotels. For an authentic experience, arrange a houseboat stay on the haor.");
        tanguarHaor.setLocalFood("Fresh haor fish is exceptional — try grilled or curry preparations on the boat. Local hospitality includes simple rice meals in haor villages.");
        tanguarHaor.setTravelTips("A 2-day houseboat trip is STRONGLY recommended — you cannot experience the true magic of Tanguar Haor in a single day. Book through Sunamganj District Administration or reputable tour operators. Carry warm clothes for winter nights on the water. Bring binoculars for bird watching. Do not use single-use plastics on the haor.");
        tanguarHaor.setWarnings("⚠️ The haor can be dangerous during storms — do not travel in bad weather. ⚠️ Do NOT book random boat operators from the ghat without checking credentials — significant overcharging occurs. Get quotes from multiple operators. ⚠️ Do not purchase or consume illegally caught Hilsa fish or protected species. ⚠️ Some operators falsely claim 'official' status — verify with the District Administration Tourism office.");
        tanguarHaor.setLatitude(25.1167);
        tanguarHaor.setLongitude(91.0167);
        tanguarHaor.setYoutubeVideoId("yJWh34w-05U");
        tanguarHaor.setGalleryImages(java.util.Arrays.asList(
                R.drawable.tanguarhaor1,
                R.drawable.tanguarhaor2,
                R.drawable.tanguarhaor3,
                R.drawable.tanguarhaor4
        ));
        spots.add(tanguarHaor);

        // 2. Tekerghat
        TouristSpot tekerghat = new TouristSpot(
                "tekerghat", "sunamganj_dist",
                "Tekerghat (Syed Nazrul Islam Saetu & Blue Lake)", "টেকেরঘাট",
                "An azure blue lake in an abandoned limestone quarry — one of Bangladesh's hidden gems.",
                R.drawable.tekerghat1, TouristSpot.SpotCategory.NATURAL
        );
        tekerghat.setFullDescription("Tekerghat is a former lime stone (chinaclay) mining area in Tahirpur Upazila of Sunamganj. The abandoned quarries have filled with stunningly vivid turquoise-blue water, creating an otherworldly lake surrounded by white limestone cliffs. The area also overlooks Tanguar Haor and has views of the Meghalaya hills. A bridge (Syed Nazrul Islam Bridge) connects the area to the mainland. It's often combined with a Tanguar Haor trip.");
        tekerghat.setHowToReach("From Sunamganj: Speedboat or engine boat to Tahirpur, then to Tekerghat (2-3 hours total). Combined with Tanguar Haor trips.");
        tekerghat.setTransportOptions("Speedboat | Engine Boat (from Sunamganj Ghat)");
        tekerghat.setApproxCostFromSylhet("Combine with Tanguar Haor boat hire. Extra cost: ৳500-1000 to extend boat trip to Tekerghat.");
        tekerghat.setEntranceFee("No entry fee");
        tekerghat.setBestTimeToVisit("November to April (dry season when water level is lower and limestone structures are more visible)");
        tekerghat.setTravelTips("Best visited as part of a Tanguar Haor overnight trip. The blue water is most vivid in bright sunlight. Excellent photography opportunity from the hilltop viewpoint.");
        tekerghat.setWarnings("⚠️ SWIMMING IN THE QUARRY LAKE IS PROHIBITED due to deep, cold water and underwater hazards. ⚠️ Road access during monsoon is often flooded — boat is the primary transport.");
        tekerghat.setLatitude(25.1417);
        tekerghat.setLongitude(91.0000);
        tekerghat.setYoutubeVideoId("dyYwTMqIvj0");
        tekerghat.setGalleryImages(java.util.Arrays.asList(
                R.drawable.tekerghat1,
                R.drawable.tekerghat2,
                R.drawable.tekerghat3,
                R.drawable.tekerghat4
        ));
        spots.add(tekerghat);

        dist.setSpots(spots);
        return dist;
    }
}
