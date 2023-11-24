import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
class WordCounter
{
    private final Map<String, Integer> wordCount;
    public WordCounter() {
        this.wordCount = new ConcurrentHashMap<>();
    }

    public void countWords(String[] words) {
        for (String word : words) {
            wordCount.compute(word, (key, oldValue) -> (oldValue == null) ? 1 : oldValue + 1);
        }
    }

    public Map<String, Integer> getWordCount() {
        return wordCount;
    }
}
class FileProcessor implements Runnable {
    private final WordCounter wordCounter;
    private final String filePath;
    private final int chunkSize;

    public FileProcessor(WordCounter wordCounter, String filePath, int chunkSize) {
        this.wordCounter = wordCounter;
        this.filePath = filePath;
        this.chunkSize = chunkSize;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(" ");
            }
            String[] words = sb.toString().split("\\s+");
            int start = 0;
            while (start < words.length) {
                int end = Math.min(start + chunkSize, words.length);
                String[] chunk = new String[end - start];
                System.arraycopy(words, start, chunk, 0, end - start);
                synchronized (wordCounter) {
                    wordCounter.countWords(chunk);
                }
                start = end;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




public class Main
{
    public static void main(String[] args) {
        WordCounter wordCounter = new WordCounter();
        String filePath = "/Users/rahimabbas/Semester 5/SCD/Labs/Lab 9/Lab 9/src/data.txt";
        int numberOfThreads = 4;
        FileProcessor[] fileProcessors = new FileProcessor[numberOfThreads];
        Thread[] threads = new Thread[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            fileProcessors[i] = new FileProcessor(wordCounter, filePath, 100);
            threads[i] = new Thread(fileProcessors[i]);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        wordCounter.getWordCount().forEach((word, count) -> System.out.println(word + ": " + count));
    }
}