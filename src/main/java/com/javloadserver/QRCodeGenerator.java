package com.javloadserver;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.WriterException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class QRCodeGenerator {

    public static void displayQRCode(String url) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            url, 
            BarcodeFormat.QR_CODE, 
            50, 
            50
        );

        System.out.println("\nScan the QR code to connect:");
        
        String asciiQR = convertToASCII(bitMatrix);
        System.out.println(asciiQR);
    }

    private static String convertToASCII(BitMatrix bitMatrix) {
        StringBuilder sb = new StringBuilder();
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(bitMatrix.get(x, y) ? "██" : "  ");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}