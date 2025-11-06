package tourbooking.vietvivu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import tourbooking.vietvivu.entity.Tour;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourRagService {
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final ChatClient chatClient;

    public String indexTourData(List<Tour> tours){
        List<Document> docs = tours.stream()
                .map(tour -> new Document(
                        tour.getTitle() + "\n" + tour.getDescription(),
                        Map.of("id", tour.getTourId(),"title", tour.getTitle())
                ))
                .toList();

        vectorStore.add(docs);
        return "Indexed " + docs.size() + " tours.";
    }

    public Flux<String> streamSuggestTour(String query) {
        List<Document> similarDocs = vectorStore.similaritySearch(query);

        String context = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        String prompt = """
            Bạn là hướng dẫn viên du lịch thông minh.
            Dưới đây là các tour gợi ý tương tự:
            %s
            Dựa trên yêu cầu khách hàng: "%s"
            Hãy tư vấn tour phù hợp và mô tả chi tiết.
            """.formatted(context, query);

        return chatClient.prompt()
                .user(prompt)
                .stream()      // <--- BẬT STREAM Ở ĐÂY
                .content();    // Trả ra từng token theo thời gian thực
    }



}
