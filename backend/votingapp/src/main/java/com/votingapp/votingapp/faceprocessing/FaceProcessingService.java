package com.votingapp.votingapp.faceprocessing;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class FaceProcessingService {
    public byte[] serializeEmbedding(List<Double> embedding) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(embedding.stream().mapToDouble(Double::doubleValue).toArray());
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize embedding", e);
        }
    }

    public double[] deserializeEmbedding(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (double[]) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize embedding", e);
        }
    }

    public double calculateCosineSimilarity(double[] vec1, double[] vec2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
