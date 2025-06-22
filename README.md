# CENG453_20242_Group16_backend
Backend Repository for CENG453 Term Project. UNO GAME.

Render URL: https://ceng453-20242-group16-backend.onrender.com/

## How to Use the API

### 1. Register a New User

Start by registering a user using the `POST /register` endpoint.

You need to provide:
- A valid email address
- A unique username
- A password with at least 6 characters

If the username already exists or the inputs are invalid, the API will return a `400 Bad Request` response.

---

### 2. Login

Use the `POST /login` endpoint with your username and password.

If the credentials are valid, you will receive:
- A JWT token
- User information

Keep the JWT token for future use with protected endpoints such as the leaderboard.

---

### 3. Reset Password

To test the password reset functionality:

1. Use the `POST /reset-password` endpoint with the email address you used during registration.
2. You will receive a reset token in your email.
   - **Note:** The email might be sent to your spam/junk folder.
3. Since the frontend is not implemented yet, the email will contain only the token, not a clickable link.
4. Copy the token and use it in the `POST /set-new-password` endpoint along with your new password.

---

### 4. Authorize with Token

To access protected endpoints like the leaderboard, you must authorize:

1. In the Swagger UI, click the **Authorize** button in the top-right corner.
2. Paste the JWT token received from the login endpoint.

---

### 5. Leaderboard controller

 Once authorized, you can access the following endpoints:
   - `GET /weekly-leaderboard`
   - `GET /monthly-leaderboard`
   - `GET /all-time-leaderboard`
 If you try to access these endpoints without authorization, you will receive a `401 Unauthorized` error.

---

You don't need to use validate-reset-token and get user endpoints. 


