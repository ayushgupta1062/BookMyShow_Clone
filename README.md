# 🎬 BookMyShow Clone - Premium Cinema Booking Experience

A full-stack, feature-rich cinema booking application built with **Spring Boot**, **React.js**, and **MySQL**. This project features a premium glassmorphism UI, real-time seat recommendations, an AI-powered chatbot, and a comprehensive admin dashboard.

---

## ✨ Key Features

### 👤 For Users
- **Premium UI**: Modern dark-mode aesthetic with smooth animations and glassmorphism.
- **Seat Selection**: Interactive seat map with real-time availability.
- **Smart Recommendations**: AI-driven seat suggestions based on your group size.
- **AI Chatbot**: Get instant help with movie details and booking assistance.
- **Booking History**: Track all your past and upcoming movie tickets in a beautiful profile view.
- **Secure Auth**: JWT-based authentication for a safe experience.

### 🔐 For Admins
- **Dynamic Movie Management**: Add, update, or remove movies easily.
- **Show Scheduling**: Manage show timings, screens, and theaters.
- **Real-time Monitoring**: Oversee bookings and theater utilization.

---

## 🛠️ Technology Stack

| Layer | Tech |
| :--- | :--- |
| **Frontend** | React 18, Vite, CSS3 (Vanilla), Axios, Lucide Icons |
| **Backend** | Java 17, Spring Boot 3, Spring Security (JWT), Hibernate/JPA |
| **Database** | MySQL 8.0 |
| **AI** | Custom Logic for Seat Recommendations & Chatbot Integration |

---

## 📸 Screenshots

<img width="1914" height="916" alt="Screenshot 2026-04-27 011001" src="https://github.com/user-attachments/assets/5caff43d-45fc-4454-871f-1cbdea27775d" />

*Modern Home Page with Trending Movies*

<img width="1907" height="917" alt="Screenshot 2026-04-27 011348" src="https://github.com/user-attachments/assets/48e0bfef-2335-4ff3-b785-81b6e3693763" />

*Interactive Seat Selection UI*

<img width="1914" height="924" alt="Screenshot 2026-04-27 011514" src="https://github.com/user-attachments/assets/400424c6-fba7-40a2-8a06-df1d58cf3375" />

*User Booking History & Profile*

---

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 1. Backend Setup
```bash
# Clone the repository
git clone https://github.com/ayushgupta1062/BookMyShow_Clone.git
cd BookMyShow_Clone

# Configure database in src/main/resources/application.properties
# spring.datasource.url=jdbc:mysql://localhost:3306/bms_db
# spring.datasource.username=your_username
# spring.datasource.password=your_password

# Run the application
mvn spring-boot:run
```

### 2. Frontend Setup
```bash
cd bms-frontend
npm install
npm run dev
```

---

## 📦 Deployment

### Frontend (Vercel)
The frontend is optimized for deployment on Vercel. Ensure you set the `VITE_API_BASE_URL` to your hosted backend URL.

### Backend (Render/Railway/Heroku)
The backend can be containerized using Docker or deployed directly as a JAR file.

---

## 🤝 Contributing
Feel free to fork this project, open issues, and submit PRs. Let's make this the best cinema booking experience together!

## 📄 License
This project is licensed under the MIT License.
