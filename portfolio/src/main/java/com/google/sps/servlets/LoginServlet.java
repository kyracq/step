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

import com.google.appengine.api.datastore.*;
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
  public static String getUserNickname(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return "";
    }
    String nickname = (String) entity.getProperty("nickname");
    return nickname;
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
      jsonResponse.addProperty("nickname", getUserNickname(user.getUserId()));
    }

    response.setContentType("application/json;");
    response.getWriter().println(jsonResponse.toString());
  }
}
