// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.JsonObject;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  /**
   * Returns the nickname of the user with id,
   * or empty String if the user is not found in datastore.
   */
  public static String getUserNickname(String id) throws EntityNotFoundException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey("UserInfo", id);
    try {
      Entity entity = datastore.get(key);
      String nickname = (String) entity.getProperty("nickname");
      return nickname;
    } catch (EntityNotFoundException e) {
        throw e;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    JsonObject jsonResponse = new JsonObject();
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      jsonResponse.addProperty("loggedIn", false);
      // After login redirect user to nickname-input page
      String loginUrl = userService.createLoginURL("/nickname-input.html");
      jsonResponse.addProperty("loginUrl", loginUrl);
    } else {
      jsonResponse.addProperty("loggedIn", true);
      String logoutUrl = userService.createLogoutURL("/");
      jsonResponse.addProperty("logoutUrl", logoutUrl);
      User user = userService.getCurrentUser();
      String userId = user.getUserId();
      jsonResponse.addProperty("userId", userId);
      try {
        String nickname = getUserNickname(userId);
        jsonResponse.addProperty("nickname", nickname);
      } catch (EntityNotFoundException e) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    }

    response.setContentType("application/json;");
    response.getWriter().println(jsonResponse.toString());
  }
}
