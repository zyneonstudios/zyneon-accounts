## Account System API Documentation

### Endpoints

#### `/login`

This endpoint is used to authenticate a user and obtain an authentication token.

- **Request:**

  ```json
  {
    "username": "example_username",
    "password": "base64_encoded_password",
    "nonExpiringToken": false
  }
  ```

- **Response:**

  ```json
  {
    "authToken": "example_token",
    "uuid": "example_user_uuid"
  }
  ```
  
#### `/logout`

This endpoint is used to invalidate an authentication token.

- **Request:**

  ```json
  {
    "authToken": "example_token"
  }
  ```

- **Response:**

  ```json
  {
    "message": "Logout successful"
  }
  ```

#### `/refresh`

This endpoint is used to refresh an authentication token.

- **Request:**

  ```json
  {
    "authToken": "example_token"
  }
  ```

- **Response:**

  ```json
  {
    "authToken": "refreshed_token",
    "uuid": "example_uuid",
    "username": "example_username"
  }
  ```

#### `/api/application`

This endpoint provides functionalities using an Application Token. Both Admin and Non-Admin Tokens can use this.

- **View User Data**
  - **Request:**

    ```json
    {
      "appToken": "example_app_token",
      "action": "viewUserData",
      "username": "example_username"
    }
    ```

  - **Response:**

    ```json
    {
      "data": {
        "email": "example_email@example.com"
      }
    }
    ```

- **Modify User Data**
  - **Request:**

    ```json
    {
      "appToken": "example_app_token",
      "action": "modifyUserData",
      "username": "example_username",
      "dataKey": "credentials",
      "modifiedData": {
        "email": "new@example.com"
        /* Additional modified data */
      }
    }
    ```

  - **Response:**

    ```json
    {
      "success": true
    }
    ```

- **Delete Account**
  - **Request:**

    ```json
    {
      "appToken": "example_app_token",
      "action": "deleteAccount",
      "username": "example_username"
    }
    ```

  - **Response:**

    ```json
    {
      "success": true
    }
    ```

- **Change Password**
  - **Request:**

    ```json
    {
      "appToken": "example_app_token",
      "action": "changePassword",
      "data": {
        "username": "example_username",
        "newPassword": "new_example_base64_encoded_password"
      }
    }
    ```

  - **Response:**

    ```json
    {
      "success": true
    }
    ```

- **Change Username**
  - **Request:**

    ```json
    {
      "appToken": "example_app_token",
      "action": "changeUsername",
      "data": {
        "username": "example_username",
        "newUsername": "new_example_username"
      }
    }
    ```

  - **Response:**

    ```json
    {
      "success": true
    }
    ```

#### `/account`

This endpoint provides account-related functionalities using an Authorization Token.

- **Create Account**
  - **Request:**

    ```json
    {
      "action": "createAccount",
      "username": "example_username",
      "password": "base64_encoded_password"
    }
    ```

  - **Response:**

    ```json
    {
      "success": true /* request token by login */
    }
    ```
    
- **Delete Account**
  - **Request:**

    ```json
    {
      "authToken": "example_auth_token",
      "action": "deleteAccount",
      "data": {
        "username": "example_username"
      }
    }
    ```

  - **Response:**

    ```json
    {
      "success": true
    }
    ```

- **Update User Data**
  - **Request:**

    ```json
    {
      "authToken": "example_auth_token",
      "action": "updateUserData",
      "data": {
        "username": "example_username",
        "dataKey": "credentials",
        "modifiedData": { /* max 1024 bytes, overwrites full key */
          "email": "new@example.com"
          /* Additional modified data */
        }
      }
    }
    ```

  - **Response:**

    ```json
    {
      "success": true
    }
    ```

- **Get User Data**
  - **Request:**

    ```json
    {
      "appToken": "example_token",
      "action": "getUserData",
      "data": {
        "username": "example_username",
        "dataKey": "example_key" /* optionally, otherways it will respond all saved data */
      }
    }
    ```

  - **Response:**

    ```json
    {
      "userData": {
        "email": "example_email@example.com"
      },
      "uuid": "example_user_uuid"
    }
    ```

- **Change Password**
  - **Request:**

    ```json
    {
      "authToken": "example_auth_token",
      "action": "changePassword",
      "data": {
        "username": "example_username",
        "newPassword": "base64_encoded_new_password"
      }
    }
    ```

  - **Response:**

    ```json
    {
      "success": true
    }
    ```

- **Change Username**
  - **Request:**

    ```json
    {
      "authToken": "example_auth_token",
      "action": "changeUsername",
      "data": {
        "username": "example_username",
        "newUsername": "new_example_username"
      }
    }
    ```

  - **Response:**

    ```json
    {
      "success": true
    }
    ```
    
#### `/information`

This endpoint provides general information.

- **Request:**

  ```json
  {
    "appToken": "example_app_token", /* must be admin token */
    "username": "example_username"
  }
  ```

- **Response:**

  ```json
  {
    "username": "example_username",
    "password": "****************",
    "data": {
      "example_key": {
        "example_value": "example_string"
      }
    }
  }
  ```

### Token Types

- **Admin Token:**
  - Used to perform administrative actions.
  - Example:
    ```json
    {
      "token": "example_admin_token"
    }
    ```

- **Non-Admin Token:**
  - Used for standard user actions.
  - Example:
    ```json
    {
      "token": "example_non_admin_token"
    }
    ```

### Example Usage in a Game

In a game, you can use these endpoints for user authentication, saving game progress, and loading user data.

#### Authentication

1. The game collects the user's username and password (base64 encoded).
2. The game sends a request to `/login` to authenticate the user and obtain an authentication token.
3. The game stores the authentication token securely for future requests.

#### Saving Game Progress

1. The game collects the user's game progress data.
2. The game sends a request to `/account/updateUserData` with the appropriate authentication token to update the user's game progress data. It is recommended to ALWAYS use a JSONObject as modifiedData.

#### Loading User Data

1. The game sends a request to `/api/application` with the appropriate application token and action `viewUserData` to retrieve the user's data.
2. The game uses the received user data to personalize the gaming experience.

### API Usage Summary

- `/login`: Authenticate and obtain an authentication token.
- `/logout`: Invalidate an authentication token.
- `/refresh`: Refresh an authentication token.
- `/api/application`: Perform actions using an Application Token (view user data, modify user data, delete account).
- `/account`: Perform account-related actions using an Authorization Token (create account, delete account, update user data).

Note: Passwords must be Base64 encoded when included in the request payloads to ensure secure transmission and storage. Failure to encode passwords may result in them being saved as-is, compromising security.
