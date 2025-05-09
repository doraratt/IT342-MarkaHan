# 📖 MarkaHan

MarkaHan is a digital platform designed to help basic education teachers efficiently manage class records, track student progress, and monitor academic performance. It allows teachers to input and organize student grades by quarter and generate clear, accurate grade slips.

## 🌐 Live Demo

* Web Frontend: [https://it-342-marka-han-9yjc.vercel.app/](https://it-342-marka-han-9yjc.vercel.app/)
* Web Backend (API): [https://rendeer-ya43.onrender.com](https://rendeer-ya43.onrender.com)
* Mobile APK: [Markahan.apk](https://github.com/doraratt/IT342-MarkaHan/releases)

---

## 🚀 Getting Started

### 🔧 Prerequisites

* Node.js (v14+)
* Java JDK 17+
* MySQL
* Maven

Clone the project:

```bash
git clone https://github.com/doraratt/IT342-MarkaHan.git
```

---

## 🛠️ Installation & Setup

### 🗔 Backend Setup

1. Navigate to the backend directory:

   ```bash
   cd markahan
   ```

2. Configure your MySQL database in `src/main/resources/application.properties`:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/dbmarkahan
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. Run the Spring Boot application:

   ```bash
   mvn spring-boot:run
   ```

   or click the Run icon in Android Studio.

   The backend server will be available at: `http://localhost:8080`

### 🌐 Frontend Setup

1. Navigate to the frontend directory:

   ```bash
   cd Markahan Web Frontend
   ```

2. Install dependencies:

   ```bash
   npm install
   ```

3. Start the development server:

   ```bash
   npm run dev
   ```

   The frontend will be running at: `http://localhost:5173/IT342-MarkaHan`

### 📱 Mobile Setup

* Developed with Kotlin using Android Studio Koala (2024.1.1)
* `compileSdkVersion`: 35
* `targetSdkVersion`: 35

The mobile app is available as a pre-built APK and can be installed on Android devices directly. Source code is included in this repository.

To build the app from source:

1. Open the project in Android Studio.
2. Connect a device or emulator.
3. Click Run ▶️ or Build ➤ Build Bundle(s)/APK(s).

#### Mobile Libraries & Frameworks Used

* Android Jetpack (Core KTX, AppCompat, Lifecycle, ConstraintLayout, RecyclerView, etc.)
* Material Components for Android
* Retrofit & OkHttp for networking
* Gson for JSON parsing
* Kotlin Coroutines
* MPAndroidChart (for charting)
* AndroidX GridLayout and SwipeRefreshLayout
* Core library desugaring for Java 8+ support on lower Android versions

---

## 🧰 Built With

### Frontend

* React
* Material-UI
* React Router
* Axios

### Backend

* Spring Boot
* Spring Security
* MySQL
* Maven

### Mobile

* Kotlin
* Android SDK
* Jetpack Libraries
* Retrofit, OkHttp, Gson
* MPAndroidChart

---

## 👥 Development Team

A sub-unit of a capstone group, composed of members specializing in different areas of full-stack development.

### Mike Francis Alon

* Role: Backend Developer
* GitHub: [@makieu](https://github.com/makieu)
* Course/Year: BSIT 3

>  Hi, I'm Mike. I'm an easygoing and dependable person who enjoys playing mobile and ball games. Watching movies is one of my favorite ways to unwind in my free time. As an IT student, I'm passionate about technology and always eager to learn new skills. I'm excited to grow in the field and make an impact in the tech world.


### Josh Kyle Cervantes

* Role: Frontend Developer / UI/UX Designer
* GitHub: [@JaeNotFound](https://github.com/JaeNotFound)
* Course/Year: BSIT 3

>  Hi, I'm Josh. I'm a creative type of person who loves to think outside the box. I'm 21 and I'm passionate about exploring various fields in IT that spark my creativity. The people who guide me and inspire me are the ones I'm thankful for. I'm excited for the challenges this year will bring and the opportunities to innovate.


### Melena Rafaella Semilla

* Role: Mobile Developer
* GitHub: [@doraratt](https://github.com/doraratt)
* Course/Year: BSIT 3

> Hi I'm El. I'm actually an introvert but when necessary, I go out of my comfort zone. I'm 22 and I'm still on the verge of finding out what I really want, exploring various fields in IT that would pique my curiosity. The people who guide me and inspire me are the ones I'm thankful for. This year I'm looking forward to the challenges.

---
