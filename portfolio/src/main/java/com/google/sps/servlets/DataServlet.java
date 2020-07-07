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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that gets and posts comments. */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {

  private List<Comment> queryComments(int maxComments) {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(maxComments))) {
      long id = entity.getKey().getId();
      String name = (String) entity.getProperty("name");
      String text = (String) entity.getProperty("text");
      long timestamp = (long) entity.getProperty("timestamp");
      double sentimentScore = (double) entity.getProperty("sentimentScore");

      Comment comment = new Comment(id, name, text, timestamp, sentimentScore);
      comments.add(comment);
    }
    return comments;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    JsonObject jsonResponse = new JsonObject();
    String maxCommentsString = request.getParameter("max-comments");

    try {
      int maxComments = Integer.parseInt(maxCommentsString);
      List<Comment> comments = queryComments(maxComments);

      Gson gson = new Gson();

      JsonParser parser = new JsonParser();
      JsonElement commentsJson = parser.parse(gson.toJson(comments));

      jsonResponse.add("comments", commentsJson);
      response.setStatus(HttpServletResponse.SC_OK);
      } catch (NumberFormatException nfe) {
        String errorMessage = "NumberFormatException: Invalid input string for "
            + "parameter max-comments: " + "'" + maxCommentsString + "'";
        jsonResponse.addProperty("errorMessage", errorMessage);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }

    response.setContentType("application/json;");
    response.getWriter().println(jsonResponse.toString());
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("comment-input");
    String name = request.getParameter("name-input");
    long timestamp = System.currentTimeMillis();

    Document commentDocument = Document.newBuilder().setContent(comment)
        .setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(commentDocument)
        .getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("text", comment);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("sentimentScore", score);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/index.html#comment-section");
  }
}
