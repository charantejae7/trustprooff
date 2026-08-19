package Jar.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QrCodeService {

    // This method takes our secure text and turns it into a 300x300 pixel QR image
    public byte[] generateQrCodeImage(String payloadText) throws Exception {

        // 1. Initialize the Google ZXing QR Engine
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        // 2. Encode the text into a 2D matrix (300x300 pixels)
        BitMatrix bitMatrix = qrCodeWriter.encode(payloadText, BarcodeFormat.QR_CODE, 300, 300);

        // 3. Convert the matrix into a PNG image format in memory
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        // 4. Return the raw image data so we can stamp it on a PDF later
        return pngOutputStream.toByteArray();
    }
}