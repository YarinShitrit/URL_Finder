# URLFinder

URLFinder is a Java program that retrieves the HTML content of a given URL and recursively extracts URLs from the HTML. The program utilizes the Jsoup library for HTML parsing.

## Prerequisites

Make sure you have the following software installed on your system:

- Java Development Kit (JDK)
- Gradle build tool

## Getting Started

Follow the steps below to compile and run the program using Gradle.

1. Clone the repository or download the source code files.

2. Open a terminal or command prompt and navigate to the project's root directory.

3. Compile the Java files and build the project using Gradle:
   ```shell
   gradle build
   ```

4. Run the program with the desired command-line arguments. The available command-line arguments are as follows:
   - **URL**: The initial URL from which to start retrieving HTML and extracting URLs.
   - **maxUrls**: The maximum number of URLs to retrieve and process.
   - **maxDepth**: The maximum depth of the recursive URL retrieval.
   - **uniqueness**: A boolean value indicating whether to enforce unique URLs.

   Example command to run the program:
   ```shell
   java -cp build/libs/URLFinder.jar URLFinder https://www.example.com 100 2 true
   ```

   Adjust the values of the command-line arguments as per your requirements.

## Program Explanation

The program starts by validating the input arguments and checking the initial URL's validity. It then creates a root node for the initial URL and initializes the URLs maps.

The `fetchHTML` method is responsible for retrieving the HTML content of a URL, saving it to a file, and extracting additional URLs. It uses the Jsoup library to connect to the URL and retrieve the HTML document. The HTML content is saved to a file in a directory specific to the URL. If the current depth is less than the maximum depth, additional URLs are extracted from the HTML and processed recursively.

The `validateInput` method validates the input arguments to ensure they are of the correct type and within valid ranges.

The `generateValidFileNameFromUrl` method generates a valid file name for a given URL by replacing characters not allowed in file names with underscores.

The `saveHtmlToFile` method saves the HTML content to a file.

The `createDirectory` method creates a directory if it does not already exist.

The `getUrlsFromDocument` method extracts URLs from an HTML document. It checks the URLs' protocols to ensure they are valid HTTP or HTTPS URLs. If uniqueness is enforced, it adds URLs that have not been found yet. Otherwise, it adds URLs based on the current depth and ensures they do not already exist at that depth.

## License

This project is licensed under the [MIT License](LICENSE).