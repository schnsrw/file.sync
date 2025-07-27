package in.lazygod.security;

import in.lazygod.models.User;

public class SecurityContextHolderUtil {

    private static final ThreadLocal<String> currentUserName = new ThreadLocal<>();

    public static void setCurrentUserName(String username) {
        currentUserName.set(username);
    }

    public static String getCurrentUserName() {
        return currentUserName.get();
    }

    public static void clear() {
        currentUser.remove();
        currentUserName.remove();
    }

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

}