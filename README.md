	# QR Demo Application

	The **QR Demo Application** is an Android app that allows users to generate, manage, and scan QR codes. It integrates with a backend API to fetch, update, and delete QR code data. The app supports two user modes: **Client** and **Admin**, each with different levels of access and functionality.

	---

	## Table of Contents

	- [Features](#features)
	- [Setup](#setup)
	  - [Prerequisites](#prerequisites)
	  - [Installation](#installation)
	- [Usage](#usage)
	  - [Login Screen](#login-screen)
	  - [Main Screen](#main-screen)
	  - [Add/Edit QR Code](#addedit-qr-code)
	  - [Scan QR Code](#scan-qr-code)
	- [Components](#components)
	  - [LoginActivity](#loginactivity)
	  - [MainActivity](#mainactivity)
	  - [AddAndEditQRActivity](#addandeditqractivity)
	  - [Adapter](#adapter)
	  - [ScanActivity](#scanactivity)
	- [API Integration](#api-integration)
	- [License](#license)

	---

	## Features

	- **QR Code Management**:
	  - Generate QR codes with customizable parameters (size, error correction, etc.).
	  - Edit and delete existing QR codes.
	  - View a list of all QR codes (for Admin) or client-specific QR codes (for Client).
	- **QR Code Scanning**:
	  - Scan QR codes using the device's camera or gallery.
	  - Validate scanned QR codes and update their status.
	- **User Modes**:
	  - **Admin**: Full access to all QR codes and operations.
	  - **Client**: Limited access to their own QR codes.
	- **Backend Integration**:
	  - Uses Retrofit for API communication.
	  - Supports authentication via API keys.

	---

	## Setup

	### Prerequisites

	- Android Studio (latest version recommended).
	- Android device or emulator with API level 21 or higher.
	- Backend API URL (configured in `Constants.BACKEND_URL`).

	### Installation

	1. **Clone the repository**:
	   ```bash
	   git clone https://github.com/barya8/QR-Demo-Application.git
	   cd qr-demo-application
	   ```

	2. **Open the project in Android Studio**:
	   - Open Android Studio and select `Open an Existing Project`.
	   - Navigate to the cloned repository and select the `build.gradle` file.

	3. **Configure the backend URL**:
	   - Open the `Constants.java` file.
	   - Replace `BACKEND_URL` with your backend API URL:
		 ```java
		 public static final String BACKEND_URL = "https://your-backend-api.com/";
		 ```

	4. **Build and run the app**:
	   - Connect an Android device or start an emulator.
	   - Click `Run` in Android Studio to build and install the app.

	---

	## Usage

	### Login Screen

	- The app starts with the `LoginActivity`.
	- Users can select their role (Client or Admin) and log in using predefined API keys.
	- Example API keys are stored in the `Constants` class (not in git):

	### Main Screen

	- After logging in, users are directed to the `MainActivity`.
	- The main screen displays a list of QR codes.
	- Users can:
	  - Add a new QR code.
	  - Edit or delete an existing QR code.
	  - Delete the entire view.
	  - Scan a QR code using the device's camera or files.
	  - Log out and return to the login screen.

	### Add/Edit QR Code

	- Users can add a new QR code or edit an existing one in the `AddAndEditQRActivity`.
	- Required fields:
	  - URL: The URL to encode in the QR code.
	  - Type: The type of QR code (e.g., one-time, multi-use).
	- Optional fields:
	  - Size: The size of the QR code in pixels.
	  - Error Correction: The error correction level (e.g., L, M, Q, H).
	  - Start Date/End Date: Validity period for the QR code.

	### Scan QR Code

	- Users can scan a QR code using the `ScanActivity`.
	- The app requests camera permission if not already granted.
	- Scanned QR codes are validated and processed.

	---

	## Components

	### LoginActivity

	- Handles user login and role selection.
	- Passes the API key and user mode to the `MainActivity`.

	### MainActivity

	- Displays a list of QR codes in a `RecyclerView`.
	- Provides buttons for adding, scanning, and deleting QR codes.
	- Integrates with the backend API to fetch and manage QR code data.

	### AddAndEditQRActivity

	- Allows users to add or edit QR codes.
	- Validates user input and sends data to the backend API.

	### Adapter

	- Manages the display of QR codes in the `RecyclerView`.
	- Handles user interactions (edit and delete) for individual QR codes.

	### ScanActivity

	- Handles QR code scanning using the device's camera or gallery.
	- Captures an image of the QR code and sends it to the backend API for processing.
	- Validates the scanned QR code and updates its status if necessary.

	#### Key Features of `ScanActivity`:
	- **Camera and Gallery Integration**:
	  - Users can choose to capture a new image using the camera or select an existing image from the gallery.
	  - The app requests necessary permissions (camera and storage) at runtime.
	- **Image Processing**:
	  - Captured images are converted to a byte array and sent to the backend API as a multipart request.
	- **QR Code Validation**:
	  - The scanned QR code is validated against the client's data.
	  - If the QR code is valid and matches the client's data, its status is updated in the backend.
	- **Error Handling**:
	  - Displays appropriate error messages if the QR code is invalid or if there are issues with the API call.

	---

	## API Integration

	The app uses Retrofit to communicate with the backend API. Key API endpoints include:

	- **Get All Data**: Fetch all QR codes (Admin only).
	- **Get Data by Client**: Fetch QR codes for a specific client.
	- **Generate QR Code**: Create a new QR code.
	- **Update QR Code**: Modify an existing QR code.
	- **Delete QR Code**: Remove a QR code by ID.
	- **Delete All**: Remove all QR codes for a specific client.
	- **Scan QR Code**: Validate and process a scanned QR code.

	---

	## License

	This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.

	---

	## Support

	For questions or issues, please contact [Your Name](mailto:bar.yaron@s.afeka.ac.il).

	---
