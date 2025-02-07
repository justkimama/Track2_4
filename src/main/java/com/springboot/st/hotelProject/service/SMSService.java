package com.springboot.st.hotelProject.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.springboot.st.hotelProject.domain.Hotel_Reservation;
import lombok.*;
import net.nurigo.java_sdk.api.Image;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;


@Getter
@Service
@RequiredArgsConstructor
public class SMSService {

    @Value("${sms.key}")
    private String key;

    @Value("${sms.secretkey}")
    private String secretKey;

    @Value("${sms.phonenum}")
    private String phoneNum;

    @Value("${naver.service.id}")
    private String naverAccessKey;

    @Value("${naver.service.secret}")
    private String naverAccessSecretKey;

    @Value("${naver.sms.name}")
    private String naverServiceId;

    @Value("${naver.sms.secret}")
    private String naverSmsSecretKey;

    public void sms_Send() {
        Message message = new Message(key, secretKey);
        System.out.println(key);
        System.out.println(phoneNum);
        System.out.println(secretKey);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("to", phoneNum);
        hashMap.put("from", phoneNum);
        hashMap.put("type", "SMS");

        hashMap.put("text", "vv");
        hashMap.put("app_version", "test app 1.2");

        try {
            System.out.println("try in");
            JSONObject obj = (JSONObject) message.send(hashMap);
            System.out.println(obj.toString());
        } catch (CoolsmsException e) {
            System.out.println("error-----------------");
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
            System.out.println("---------------");
        }

    }

    //sms_send logic
    public void sms_Send(Hotel_Reservation hotel_reservation) {
        Message message = new Message(key, secretKey);
        System.out.println(key);
        System.out.println(phoneNum);
        System.out.println(secretKey);
        System.out.println(hotel_reservation.getPhoneNum());


        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("to", hotel_reservation.getPhoneNum());
        hashMap.put("from", phoneNum);
        hashMap.put("type", "SMS");

        String name = null;
        if (hotel_reservation.getUser() != null) {
            name = hotel_reservation.getUser().getName();
        } else {
            name = hotel_reservation.getAnotherUser();
        }

        String text_send = "Name : " + name + "\n" +
                "Num : " + hotel_reservation.getId() + "\n" +
                "CheckIn : " + hotel_reservation.getStartDay() + "\n" +
                "CheckOut : " + hotel_reservation.getFinishDay() + "\n" +
                "People : " + hotel_reservation.getPeople() + "\n" +
                "Fee : " + "$ " + hotel_reservation.getPayment().getPayPrice();
//        hashMap.put("image","https://rbwsn-s3-image.s3.ap-northeast-2.amazonaws.com/710b6ad1-dbda-438f-af5d-86db8151b0a5executive_hotel5.jpg");
        hashMap.put("text", text_send);
        hashMap.put("app_version", "test app 1.2");

        try {
            System.out.println("try in");
            JSONObject obj = (JSONObject) message.send(hashMap);
            System.out.println(obj.toString());
        } catch (CoolsmsException e) {
            System.out.println("error-----------------");
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
            System.out.println("---------------");
        }

    }

    public void naverSmsSendService(Hotel_Reservation hotel_reservation) {
        String host = "https://sens.apigw.ntruss.com";
        String requestUrl = "/sms/v2/services/";
        String serviceId = naverServiceId;
        String accessKey = naverAccessKey;
        String accessSecretKey = naverAccessSecretKey;
        String secretKey = naverSmsSecretKey;
        String method = "POST";
        String timestamp = Long.toString(System.currentTimeMillis());
        requestUrl += serviceId + "/messages";
        String urlFull = host + requestUrl;

        String name = null;
        if (hotel_reservation.getUser() != null) {
            name = hotel_reservation.getUser().getName();
        } else {
            name = hotel_reservation.getAnotherUser();
        }

        String contents =
                "Num : " + hotel_reservation.getId() +
                "\nName : " + name +
                "\nRoom : " + hotel_reservation.getPayment().getRoomName()+
                "\nCheckIn : " + hotel_reservation.getStartDay() +
                "\nCheckOut : " + hotel_reservation.getFinishDay() +
                "\nPeople : " + hotel_reservation.getPeople()+
                "\nPee : " + "$ "+hotel_reservation.getPayment().getPayPrice();



        JSONObject naverJSON = new JSONObject();
        JSONObject messages = new JSONObject();
        JSONArray messagesArray = new JSONArray();
        JSONObject file = new JSONObject();
        try {
            naverJSON.put("type", "LMS");
            naverJSON.put("contentType", "COMM");
            naverJSON.put("from", "01023364961");
            naverJSON.put("countryCode", "82");
            naverJSON.put("content", "test");




            messages.put("to", hotel_reservation.getPhoneNum());
//            messages.put("subject",
//                    hotel_reservation.getReHotelRoom().getRoomName() + "의 예약 정보");
            messages.put("content", contents);

            messagesArray.add(messages);
            naverJSON.put("messages", messagesArray);

        } catch (Exception e) {
            e.getStackTrace();
        }

        System.out.println(urlFull);
        System.out.println(naverJSON);

        URL url = null;

        try {
            url = new URL(urlFull);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("content-type", "application/json");
            con.setRequestProperty("x-ncp-apigw-timestamp", timestamp);
            con.setRequestProperty("x-ncp-iam-access-key", accessKey);

            String space = " ";                    // one space
            String newLine = "\n";                    // new line

            String me = new StringBuilder()
                    .append(method)
                    .append(space)
                    .append(requestUrl)
                    .append(newLine)
                    .append(timestamp)
                    .append(newLine)
                    .append(accessKey)
                    .toString();

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec singKey = new SecretKeySpec(accessSecretKey.getBytes("UTF-8"), "HmacSHA256");
            mac.init(singKey);
            byte[] hash = mac.doFinal(me.getBytes("UTF-8"));
            String encode = Base64.getEncoder().encodeToString(hash);

            System.out.println(encode);

            con.setRequestProperty("x-ncp-apigw-signature-v2", encode);
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(naverJSON.toString().getBytes());
            wr.flush();
            wr.close();

            int recode = con.getResponseCode();
            BufferedReader br;
            System.out.println(recode);
            if (recode == 202) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputline;
            StringBuffer response = new StringBuffer();
            while ((inputline = br.readLine()) != null) {
                response.append(inputline);
            }
            br.close();
            System.out.println(response.toString());


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

    }

    private File generateQRCodeImage(String reservation_url)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(reservation_url, BarcodeFormat.QR_CODE, 250, 250);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        File output = new File("QRCode.jpg");

        ImageIO.write(image, "jpg", output);




        return output;
    }

    private String bas(String reservation_url) throws IOException, WriterException {

        byte[] binary = getFileBinary(generateQRCodeImage(reservation_url).toString());
        // base64의 라이브러리에서 encodeToString를 이용해서 byte[] 형식을 String 형식으로 변환합니다.

        String base64data = Base64.getEncoder().encodeToString(binary);

        // 콘솔에 결과 출력
        System.out.println(base64data);

        return base64data;
    }


    // 파일 읽어드리는 함수
    private static byte[] getFileBinary(String filepath) {
        File file = new File(filepath);
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream stream = new FileInputStream(file)) {
            stream.read(data, 0, data.length);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return data;
    }


}
