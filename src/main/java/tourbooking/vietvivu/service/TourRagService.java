package tourbooking.vietvivu.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.repository.ImageRepository;

@Service
@RequiredArgsConstructor
public class TourRagService {
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final ChatClient chatClient;

    private final ImageRepository imageRepository;

    public String indexTourData(List<Tour> tours) {
        List<Document> docs = tours.stream()
                .map(tour -> {
                    String content = tour.getTitle() + "\n"
                            + "Destination: " + tour.getDestination() + "\n"
                            + "Duration: " + tour.getDuration() + " ngày\n"
                            + "Giá người lớn: " + tour.getPriceAdult() + " vnđ\n"
                            + "Giá trẻ em: " + tour.getPriceChild() + " vnđ\n"
                            + tour.getDescription();

                    return new Document(
                            content,
                            Map.of(
                                    "tourId", tour.getTourId(),
                                    "title", tour.getTitle(),
                                    "destination", tour.getDestination(),
                                    "duration", tour.getDuration(),
                                    "priceAdult", tour.getPriceAdult(),
                                    "priceChild", tour.getPriceChild(),
                                    "imageUrls",
                                            String.join(
                                                    ",",
                                                    imageRepository.findImageUrlsByTour_TourId(tour.getTourId()))));
                })
                .toList();

        vectorStore.add(docs);
        return "Indexed " + docs.size() + " tours.";
    }
}
