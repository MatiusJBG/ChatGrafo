package app;

public class Main {
    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "swing");
        SocialNetworkApp app = new SocialNetworkApp();
        app.loadUsersFromCSV("src/data/nombres.csv");
        app.createCommunities(5);
        app.createRandomFriendships(2, 4);
        app.visualize();
    }
}
