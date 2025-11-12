package tourbooking.vietvivu.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.entity.Tour;

@Service
@RequiredArgsConstructor
public class TourRagService {
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final ChatClient chatClient;


    public String indexTourData(List<Tour> tours) {
        List<Document> docs = tours.stream()
                .map(tour -> new Document(
                        tour.getTitle() + "\n" + tour.getDescription(),
                        Map.of("id", tour.getTourId(), "title", tour.getTitle())))
                .toList();

        vectorStore.add(docs);
        return "Indexed " + docs.size() + " tours.";
    }



}
