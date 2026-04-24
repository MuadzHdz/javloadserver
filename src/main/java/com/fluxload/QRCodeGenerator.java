package com.fluxload;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.WriterException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class QRCodeGenerator {

    public static void displayQRCode(String url) throws WriterException, IOException {
        // Even smaller QR code size for compact display
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            url, 
            BarcodeFormat.QR_CODE, 
            15, 
            15
        );

        System.out.println("\nScan the QR code to connect:");
        
        String asciiQR = convertToASCII(bitMatrix);
        System.out.print(asciiQR);
    }

    private static String convertToASCII(BitMatrix bitMatrix) {
        StringBuilder sb = new StringBuilder();
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        
        // Center the QR code by calculating padding
        int terminalWidth = 80; // Standard terminal width
        int qrWidth = width * 2; // Each block is 2 characters
        int padding = Math.max(0, (terminalWidth - qrWidth) / 2);
        String paddingStr = " ".repeat(padding);
        
        for (int y = 0; y < height; y++) {
            sb.append(paddingStr); // Add left padding for centering
            for (int x = 0; x < width; x++) {
                sb.append(bitMatrix.get(x, y) ? "██" : "  ");
            }
            sb.append("\n");
        }
        
        // No extra padding at bottom - keep it tight to the text above
        
        return sb.toString();
    }
}