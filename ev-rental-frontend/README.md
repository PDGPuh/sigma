# EV Rental Frontend

## Project Setup

This is a complete ReactJS frontend for an Electric Vehicle (EV) rental system. The project structure is designed to follow modern development practices and includes a clean and elegant design suitable for a luxury car rental service. 

### Directory Structure

```
/ev-rental-frontend
  ├── /public
  │   ├── index.html
  │   └── favicon.ico
  ├── /src
  │   ├── /components
  │   │   ├── Header.js
  │   │   ├── Hero.js
  │   │   ├── VehicleSearch.js
  │   │   ├── VehicleGrid.js
  │   │   ├── VehicleDetails.js
  │   │   ├── BookingForm.js
  │   │   ├── UserDashboard.js
  │   │   └── AdminPanel.js
  │   ├── /pages
  │   │   ├── Home.js
  │   │   ├── Vehicles.js
  │   │   ├── Booking.js
  │   │   ├── About.js
  │   │   ├── Contact.js
  │   │   ├── SignIn.js
  │   │   └── Register.js
  │   ├── /styles
  │   │   └── App.css
  │   ├── App.js
  │   ├── index.js
  │   └── AppRouter.js
  ├── package.json
  └── README.md
```

### Dependencies

- **React 18**: For building the user interface.
- **React Router**: For routing between different pages.
- **Axios**: For making HTTP requests to the backend.
- **Material-UI**: For modern UI components and styling.

## Installation Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/PDGPuh/sigma.git
   cd sigma/ev-rental-frontend
   ```

2. Install the dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

## Component Descriptions

- **Header**: Contains navigation links to HOME, VEHICLES, BOOKING, ABOUT, CONTACT, SIGN IN, REGISTER.
- **Hero**: Showcases electric vehicles with a beautiful hero section.
- **VehicleSearch**: Provides filters for searching vehicles by Make, Model, Battery Level, Location, and Date Range.
- **VehicleGrid**: Displays a grid of available vehicles.
- **VehicleDetails**: Shows detailed information about a selected vehicle.
- **BookingForm**: Allows users to book a vehicle.
- **UserDashboard**: Displays user-specific information and bookings.
- **AdminPanel**: For managing vehicles and bookings.

## Styling

- The design is modern and professional, utilizing a green/electric theme to reflect the EV focus.
- Responsive design for a mobile-first approach.

## Integration

This frontend is designed to be integrated with a Java Spring Boot backend for handling user authentication and vehicle data management.

---

### Note
This is a placeholder README for the EV Rental Frontend project. Further details will be added as the project progresses.