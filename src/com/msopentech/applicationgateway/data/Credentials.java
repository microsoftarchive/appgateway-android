/*
 *  Copyright (c) Microsoft Open Technologies
 *  All rights reserved. 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0  
 *  THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT 
 *  LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE, 
 *  MERCHANTABLITY OR NON-INFRINGEMENT. 
 *  See the Apache Version 2.0 License for specific language governing permissions and limitations under the License.
 */
package com.msopentech.applicationgateway.data;

/**
 * Represents the user's credentials.
 */
public class Credentials {
    /**
     * Stored username.
     */
    private String mUsername;

    /**
     * Stored password.
     */
    private String mPassword;

    /**
     * Class constructor.
     * 
     * @param username Username to store.
     * @param password Password to store.
     */
    public Credentials(String username, String password) {
        mUsername = username;
        mPassword = password;
    }

    /**
     * Returns username.
     * 
     * @return Stored username.
     */
    public String getUsername() {
        return mUsername;
    }
    /**
     * Returns password.
     * @return Stored password.
     */
    public String getPassword() {
        return mPassword;
    }
}
