## Zyneon Account Management System

Welcome to the Zyneon Account Management System! This system allows you to manage user accounts and access tokens for your applications securely.

### Accessing the System

To access the Zyneon Account Management System on the web, follow these steps:

1. **Ensure Java is Installed**: Make sure you have Java installed on your system. If not, you can download and install it from [Java's official website](https://www.java.com/en/download/).

2. **Download the Application**: Download the Zyneon Account Management System application files from [GitHub](https://github.com/officialPlocki/ZyneonAccounts).

3. **Set Up Configuration**: Configure the system settings by modifying the `config/zyneon_config.json` file according to your requirements. You can specify parameters like port number, maximum access per minute, JSON data size limits, etc.

4. **Run the Application**: Open a terminal or command prompt, navigate to the directory containing the downloaded files, and run the application using the following command:

   ```
   java -jar ZyneonAccounts.jar
   ```

5. **Access the System via Browser**: Once the application is running, you can access it via any web browser using the specified host IP address and port number (default is `http://localhost:908`).

### Available Commands

Once you have accessed the system, you can use the following commands:

- **createAdminAppToken username**: Generate an access token for administrative purposes for the specified username.
- **createAppToken username**: Generate a regular access token for the specified username.
- **stop**: Stop the Zyneon Account Management System.

### Using the API

The Zyneon Account Management System provides an API for managing accounts and data. You can interact with the API by sending HTTP requests to the appropriate endpoints. Here are some of the available endpoints:

- **/login**: Authenticate a user and generate an authentication token.
- **/logout**: Invalidate an authentication token and log out the user.
- **/refresh**: Refresh an authentication token to extend its validity.
- **/api/application**: Perform various actions like viewing user data, modifying user data, deleting accounts, etc.
- **/account**: Perform account-related actions such as creating accounts, deleting accounts, updating user data, etc.
- **/information**: Retrieve information about a user account, including username, password (masked), and associated data.

### Important Notes

- Make sure to handle authentication tokens securely and protect them from unauthorized access.
- Ensure that sensitive data, such as passwords, is transmitted securely over HTTPS.
- Regularly update the configuration settings as per your application's requirements.
- Review the codebase and customize it according to your organization's security policies and guidelines.

### Feedback and Support

If you encounter any issues or have suggestions for improvement, please feel free to [report them on GitHub](https://github.com/officialPlocki/ZyneonAccounts/issues). For additional support or inquiries, you can contact the developers directly.

Thank you for using the Zyneon Account Management System! We hope it helps streamline your account management process and enhances the security of your applications.

### Examples of JSON Requests and Responses

#### 1. **Login**
- **Request:** 
  ```json
  {
    "username": "example_user",
    "password": "example_password"
  }
  ```
- **Response (Success):**
  ```json
  {
    "authToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImV4YW1wbGVfdXNlciIsImV4cCI6MTY0OTg1OTk5MiwiaWF0IjoxNjQ5ODU5OTkyfQ.IzglRVfcdpDz_LJSM-jQzJdmufok7Yt_FzWs6cKLkI8",
    "uuid": "example_uuid"
  }
  ```
- **Response (Error):**
  ```json
  {
    "error": "Invalid credentials"
  }
  ```

#### 2. **Logout**
- **Request:**
  ```json
  {
    "authToken": "example_auth_token"
  }
  ```
- **Response (Success):**
  ```json
  {
    "message": "Logout successful"
  }
  ```
- **Response (Error):**
  ```json
  {
    "error": "Token invalid"
  }
  ```

#### 3. **Refresh Token**
- **Request:**
  ```json
  {
    "authToken": "example_auth_token"
  }
  ```
- **Response (Success):**
  ```json
  {
    "authToken": "new_auth_token",
    "uuid": "example_uuid",
    "username": "example_user"
  }
  ```
- **Response (Error):**
  ```json
  {
    "error": "Invalid or missing authentication token"
  }
  ```

#### 4. **Application API**
- **Request:**
  ```json
  {
    "appToken": "example_app_token",
    "action": "viewUserData",
    "username": "example_user"
  }
  ```
- **Response (Success):**
  ```json
  {
    "data": {
      "user_data": "example_data"
    }
  }
  ```
- **Response (Error):**
  ```json
  {
    "error": "Token expired"
  }
  ```

#### 5. **Account Management**
- **Request:**
  ```json
  {
    "action": "createAccount",
    "username": "new_user",
    "password": "new_password"
  }
  ```
- **Response (Success):**
  ```json
  {
    "success": true
  }
  ```
- **Response (Error):**
  ```json
  {
    "error": "Username already in use"
  }
  ```

#### 6. **Get All Information**
- **Request:**
  ```json
  {
    "appToken": "example_app_token",
    "username": "example_user"
  }
  ```
- **Response (Success):**
  ```json
  {
    "username": "example_user",
    "password": "********",
    "data": {
      "user_data": "example_data"
    }
  }
  ```
- **Response (Error):**
  ```json
  {
    "error": "Token isn't admin token"
  }
  ```

These examples demonstrate the format of JSON requests and responses for various actions within the Zyneon Account Management System API. Make sure to replace the placeholder values with actual data when making requests.