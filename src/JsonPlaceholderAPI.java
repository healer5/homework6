import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JSONPlaceholderApiClient {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    private final CloseableHttpClient httpClient;

    public JSONPlaceholderApiClient() {
        this.httpClient = HttpClients.createDefault();
    }

    public JsonObject createNewUser(JsonObject user) throws IOException {
        HttpPost request = new HttpPost(BASE_URL + "/users");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(user.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            return JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        }
    }

    public JsonObject updateUser(JsonObject user) throws IOException {
        HttpPut request = new HttpPut(BASE_URL + "/users/" + user.get("id"));
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(user.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            return JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        }
    }

    public int deleteUser(int userId) throws IOException {
        HttpDelete request = new HttpDelete(BASE_URL + "/users/" + userId);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return response.getStatusLine().getStatusCode();
        }
    }

    public JsonArray getAllUsers() throws IOException {
        HttpGet request = new HttpGet(BASE_URL + "/users");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            return JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonArray();
        }
    }

    public JsonObject getUserById(int userId) throws IOException {
        HttpGet request = new HttpGet(BASE_URL + "/users/" + userId);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            return JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        }
    }

    public JsonObject getUserByUsername(String username) throws IOException {
        HttpGet request = new HttpGet(BASE_URL + "/users?username=" + username);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            JsonArray jsonArray = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonArray();
            if (jsonArray.size() > 0) {
                return jsonArray.get(0).getAsJsonObject();
            } else {
                return null;
            }
        }
    }

    public JsonArray getCommentsForLastPost(int userId) throws IOException {
        HttpGet userPostsRequest = new HttpGet(BASE_URL + "/users/" + userId + "/posts");
        try (CloseableHttpResponse response = httpClient.execute(userPostsRequest)) {
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            JsonArray userPosts = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonArray();
            if (userPosts.size() > 0) {
                JsonObject lastPost = userPosts.get(userPosts.size() - 1).getAsJsonObject();
                int postId = lastPost.get("id").getAsInt();
                HttpGet postCommentsRequest = new HttpGet(BASE_URL + "/posts/" + postId + "/comments");
                try (CloseableHttpResponse commentsResponse = httpClient.execute(postCommentsRequest)) {
                    HttpEntity commentsEntity = commentsResponse.getEntity();
                    InputStream commentsInputStream = commentsEntity.getContent();
                    return JsonParser.parseReader(new InputStreamReader(commentsInputStream)).getAsJsonArray();
                }
            } else {
                return new JsonArray();
            }
        }
    }

    public JsonArray getOpenTasksForUser(int userId) throws IOException {
        HttpGet request = new HttpGet(BASE_URL + "/users/" + userId + "/todos");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            JsonArray todos = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonArray();

            JsonArray openTasks = new JsonArray();
            for (int i = 0; i < todos.size(); i++) {
                JsonObject todo = todos.get(i).getAsJsonObject();
                if (!todo.get("completed").getAsBoolean()) {
                    openTasks.add(todo);
                }
            }

            return openTasks;
        }
    }

    public void writeCommentsToFile(JsonArray comments, int userId, int postId) throws IOException {
        String filename = "user-" + userId + "-post-" + postId + "-comments.json";
        try (FileWriter fileWriter = new FileWriter(filename)) {
            fileWriter.write(comments.toString());
        }
    }

    public static void main(String[] args) {
        JSONPlaceholderApiClient apiClient = new JSONPlaceholderApiClient();

        try {
            JsonObject newUser = new JsonObject();
            newUser.addProperty("name", "Jackie Chan");
            newUser.addProperty("username", "mastertaekwondo");
            newUser.addProperty("email", "mastertaekwondo@example.com");
            newUser.addProperty("phone", "666-666-6666");
            newUser.addProperty("website", "https://mastertaekwondo.com");

            JsonObject createdUser = apiClient.createNewUser(newUser);
            System.out.println("Created User: " + createdUser);

            createdUser.addProperty("website", "https://newmaster.com");
            JsonObject updatedUser = apiClient.updateUser(createdUser);
            System.out.println("Updated User: " + updatedUser);

            int deleteStatusCode = apiClient.deleteUser(updatedUser.get("id").getAsInt());
            System.out.println("Delete Status Code: " + deleteStatusCode);

            JsonArray allUsers = apiClient.getAllUsers();
            System.out.println("All Users: " + allUsers);

            JsonObject userById = apiClient.getUserById(1);
            System.out.println("User by ID: " + userById);

            JsonObject userByUsername = apiClient.getUserByUsername("Bret");
            System.out.println("User by Username: " + userByUsername);

            JsonArray commentsForLastPost = apiClient.getCommentsForLastPost(1);
            System.out.println("Comments for Last Post: " + commentsForLastPost);

            JsonArray openTasksForUser = apiClient.getOpenTasksForUser(1);
            System.out.println("Open Tasks for User: " + openTasksForUser);

            apiClient.writeCommentsToFile(commentsForLastPost, 1, 1);
            System.out.println("Comments written to file");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                apiClient.httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}