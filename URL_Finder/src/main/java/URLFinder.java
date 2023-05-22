import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class URLFinder {
    private static String initialURL; // args[0]
    private static int maxUrls; // args[1]
    private static int maxDepth; // args[2]
    private static boolean uniqueness; // args[3]
    private static String currentDirectory;
    private static HashMap<String, Boolean> uniqueUrlsMap;
    private static HashMap<String, Boolean> depthUrlsMap;

    public static void main(String[] args) throws Exception {
        // Validate input
        validateInput(args);

        // Get current directory
        currentDirectory = System.getProperty("user.dir");

        // create root for initialUrl
        URLTreeNode root = new URLTreeNode(initialURL, currentDirectory, 0);

        // Initialize urls maps
        if (uniqueness) {
            uniqueUrlsMap = new HashMap<>();
            uniqueUrlsMap.put(initialURL, true);
        }
        depthUrlsMap = new HashMap<>();
        Thread thread = new Thread(() -> fetchHTML(null, root));
        thread.start();
        thread.join();
        System.out.println(root);
    }

    /**
     * Fetches the HTML content of a given URL and performs subsequent operations.
     *
     * @param parent      The parent URLTreeNode.
     * @param urlTreeNode The URLTreeNode representing the current URL.
     */
    private static void fetchHTML(URLTreeNode parent, URLTreeNode urlTreeNode) {
        String url = urlTreeNode.getUrlString();
        try {
            // Get the Document with the HTML of the URL
            Document doc = Jsoup.connect(url).timeout(100000).get();

            // Create directory for URL if it does not exist
            createDirectory(urlTreeNode.getDirPath());

            // Create a valid file name with only allowed characters
            String fileName = generateValidFileNameFromUrl(url);

            // Save the HTML to file
            saveHtmlToFile(urlTreeNode.getDirPath(), fileName, doc.html());

            // Get additional URLs if the current depth is less than maxDepth
            if (urlTreeNode.getDepth() < maxDepth) {
                ArrayList<String> newUrls = getUrlsFromDocument(doc, uniqueness, String.valueOf(urlTreeNode.getDepth()));

                // run fetchHTML() for all urls asynchronously with multiple threads
                List<Thread> threads = new ArrayList<>();
                for (String newUrl : newUrls) {
                    URLTreeNode childUrlNode = new URLTreeNode(newUrl, currentDirectory, urlTreeNode.getDepth() + 1);
                    urlTreeNode.addChild(childUrlNode);
                    Thread thread = new Thread(() -> fetchHTML(urlTreeNode, childUrlNode));
                    threads.add(thread);
                    thread.start();
                }

                for (Thread thread : threads) {
                    thread.join();
                }
            }
        } catch (Exception e) {
            //System.out.println("There was an error with URL " + url + ": " + e.getMessage());

            // Remove child from its parent if there was an error with the URL
            if (parent != null) {
                parent.removeChild(urlTreeNode);
            }
        }
    }

    /**
     * Validates the input arguments and throws exceptions if any validation fails.
     *
     * @param args The input arguments passed to the program.
     * @throws IllegalArgumentException If the input size is less than 4, the arguments are invalid, or maxUrls and maxDepth are negative.
     */
    private static void validateInput(String[] args) {

        // Validate input size
        if (args.length < 4) {
            throw new IllegalArgumentException("Expected 4 arguments but received " + args.length);
        }

        // Validate input types
        try {
            initialURL = args[0];
            // Perform URL validation
            Jsoup.connect(initialURL).get();
            maxUrls = Integer.parseInt(args[1]);
            maxDepth = Integer.parseInt(args[2]);
            uniqueness = Boolean.parseBoolean(args[3]);
        } catch (Exception e) {
            throw new IllegalArgumentException("""
                    Invalid arguments.
                    Usage: java -cp build/libs/URLFinder.jar URLFinder <initialURL> <maxUrls> <maxDepth> <uniqueness>
                    Example: java -cp build/libs/URLFinder.jar URLFinder https://www.example.com 100 2 true""");
        }

        // Validate input values
        if (maxUrls < 0 || maxDepth < 0) {
            throw new IllegalArgumentException("max_urls and depth must be positive");
        }
    }

    /**
     * Generates a valid file name from a given URL string.
     *
     * @param urlString The URL string from which to generate the file name.
     * @return The generated valid file name.
     * @throws MalformedURLException If the URL string is malformed.
     */
    private static String generateValidFileNameFromUrl(String urlString) throws MalformedURLException {
        // Create a URL object from the string
        URL url = new URL(urlString);

        // Get the host (URL name) without the protocol
        String urlName = url.getHost() + url.getPath();

        // Replace characters not allowed in file names with underscores
        return urlName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * Saves the HTML content to a file.
     *
     * @param dirPath  The directory path where the file should be saved.
     * @param filename The file name (without extension) of the file.
     * @param html     The HTML content to be saved.
     * @throws IOException If an I/O error occurs while writing the file.
     */
    private static void saveHtmlToFile(String dirPath, String filename, String html) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(dirPath + "/" + filename + ".html"));
        writer.write(html);
        writer.close();
    }

    /**
     * Creates a directory if it does not exist.
     *
     * @param dirPath The directory path to be created.
     * @throws IOException If an I/O error occurs while creating the directory.
     */
    private static void createDirectory(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        // Create directory if it does not exist
        boolean directoryExists = Files.exists(path) && Files.isDirectory(path);
        if (!directoryExists) {
            // Create the directory
            Files.createDirectories(path);
        }
    }

    /**
     * Extracts URLs from the given HTML document.
     *
     * @param doc        The HTML document from which to extract the URLs.
     * @param uniqueness Flag indicating whether to ensure unique URLs.
     * @param depth      The depth of the current URL in the tree structure.
     * @return An ArrayList of URLs extracted from the document.
     */
    private static ArrayList<String> getUrlsFromDocument(Document doc, boolean uniqueness, String depth) {
        // Extract URLs
        Elements links = doc.select("a[href]");
        ArrayList<String> urls = new ArrayList<>();
        for (Element link : links) {
            if (urls.size() == maxUrls) break; // Stop if reached maximum urls
            String href = link.attr("href");
            // Check if the new URL protocol is a valid HTTP/HTTPS
            try {
                URL urlObj = new URL(href);
                if (urlObj.getProtocol().equals("http") || urlObj.getProtocol().equals("https")) {
                    if (uniqueness) {
                        // Adds the URL only if it has not been found yet
                        if (!uniqueUrlsMap.containsKey(href)) {
                            urls.add(href);
                            uniqueUrlsMap.put(href, true);
                        }
                    } else {
                        String fileName = generateValidFileNameFromUrl(href);
                        if (!depthUrlsMap.containsKey(depth + fileName)) { // Add URL if it does not exist in current depth
                            urls.add(href);
                            depthUrlsMap.put(depth + fileName, true);
                        }
                    }
                }
            } catch (Exception e) {
                //System.out.println(href + " has an invalid protocol for scraping HTML, therefore skipped");
            }
        }
        return urls;
    }
}
