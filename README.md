# TourMate BD 🗺️

**ভ্রমণ গাইড · বাংলাদেশ** — Your Smart Travel Guide to Bangladesh

## About

TourMate BD is a free, offline-first Android travel guide for tourists visiting Bangladesh — designed to provide honest, detailed information to help travelers navigate destinations independently without falling prey to local syndicates, overcharging, or misinformation.

## Current Status

✅ **Sylhet Division** — Fully implemented with:
- 4 Districts: Sylhet, Moulvibazar, Habiganj, Sunamganj
- 15+ Tourist Spots with comprehensive information

🔜 **Coming Soon:** Dhaka, Chattogram, Rajshahi, Khulna, Barishal, Rangpur, Mymensingh

## App Structure

```
TourMate BD
├── Home (8 Divisions Grid)
├── Division → Districts
├── District → Tourist Spots
└── Spot Detail
    ├── Full description
    ├── How to reach (step by step)
    ├── Transport options
    ├── Approximate costs (from Dhaka / from Sylhet)
    ├── Entrance fees
    ├── Best time to visit
    ├── Accommodation options
    ├── Local food recommendations
    ├── Travel tips
    ├── ⚠️ Warnings (syndicates, overcharging alerts)
    └── Google Maps integration
```

## Tech Stack

- **Language:** Java
- **Min SDK:** API 35 (Android 15 - Vanilla Ice Cream)
- **UI:** XML Layouts + Material Components
- **Architecture:** Activity-based with Repository pattern
- **Font:** Hind Siliguri (Google Fonts — Bengali + Latin support)

## Project Structure

```
app/src/main/
├── java/com/tourmate/bd/
│   ├── activities/         # SplashActivity, MainActivity, DistrictListActivity,
│   │                         SpotListActivity, SpotDetailActivity, AboutActivity
│   ├── adapters/           # DivisionAdapter, DistrictAdapter, SpotAdapter
│   ├── data/               # DataRepository (singleton, all travel data)
│   ├── models/             # Division, District, TouristSpot
│   └── utils/              # (utilities, reserved for future use)
└── res/
    ├── layout/             # Activity & item layouts
    ├── drawable/           # Icons, gradients, backgrounds
    ├── values/             # Colors, strings, themes, dimens
    ├── font/               # Hind Siliguri (Regular, SemiBold, Bold)
    └── anim/               # Slide transition animations
```

## How to Open

1. Open Android Studio
2. **File → Open** → Select this folder
3. Wait for Gradle sync to complete
4. Run on emulator or device (API 35+)

---
*Built with ❤️ to empower tourists in Bangladesh*
